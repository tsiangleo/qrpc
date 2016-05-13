package com.github.tsiangleo.qrpc.consumer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tsiangleo.qrpc.proto.RpcRequest;
import com.github.tsiangleo.qrpc.proto.RpcResponse;
import com.github.tsiangleo.qrpc.serialization.Serializer;

public class RpcNIOHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(RpcNIOHandler.class);
	private static Queue<Pack> packQueue = new LinkedBlockingQueue<Pack>();

	private static Reactor task;
	static {
		try {
			task = new Reactor(packQueue);
			Thread thread = new Thread(task, "RpcClient IO Handler");
			thread.setDaemon(true);
			thread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void queue(Pack p) {
		packQueue.add(p);
		task.selector.wakeup();
	}

	static class Reactor implements Runnable {
		private Queue<Pack> packQueue;
		private Map<InetSocketAddress, SocketChannel> connetedChannels;
		// 每个通道的待发送数据
		private Map<SocketChannel, Queue<RpcRequest>> channelRpcRequestQueue;
		private Map<String, RpcCallbackFuture> rpcCallbackFutureMap;

		public final Selector selector;

		Reactor(Queue<Pack> packQueue) throws IOException {
			this.packQueue = packQueue;
			connetedChannels = new HashMap<InetSocketAddress, SocketChannel>();
			selector = Selector.open();
			channelRpcRequestQueue = new HashMap<SocketChannel, Queue<RpcRequest>>();
			rpcCallbackFutureMap = new HashMap<String, RpcCallbackFuture>();
		}

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					Pack pack = packQueue.poll();
					logger.debug("poll {} from queue", pack);
					if (pack != null) {
						// 已经有一条到给定目的rpc服务器的socketChannel了
						if (connetedChannels.containsKey(pack
								.getRpcServerAddress())) {
							SocketChannel socketChannel = connetedChannels
									.get(pack.getRpcServerAddress());
							Queue<RpcRequest> queue = channelRpcRequestQueue
									.get(socketChannel);
							queue.add(pack.getRequest());
							rpcCallbackFutureMap.put(pack.getRequest()
									.getRequestId(), pack
									.getRpcCallbackFuture());
							logger.debug(
									"add a new pack {} to the queue of socketChannel {}",
									pack, socketChannel);
						} else {
							logger.debug(
									"create a new SocketChannel to address {}",
									pack.getRpcServerAddress());

							SocketChannel socketChannel = SocketChannel.open();
							socketChannel.configureBlocking(false);
							// 客户端连接服务器,其实方法执行并没有实现连接，需要
							// 用channel.finishConnect();才能完成连接
							socketChannel.connect(pack.getRpcServerAddress());
							socketChannel.register(selector,
									SelectionKey.OP_CONNECT);

							connetedChannels.put(pack.getRpcServerAddress(),
									socketChannel);

							Queue<RpcRequest> q = new LinkedList<RpcRequest>();
							q.add(pack.getRequest());
							channelRpcRequestQueue.put(socketChannel, q);
							logger.debug("create a new channel queue");
							rpcCallbackFutureMap.put(pack.getRequest()
									.getRequestId(), pack
									.getRpcCallbackFuture());
						}
					}

					int ok = selector.select();
					logger.debug("selector.select() return {}", ok);
					if (ok == 0)
						continue;

					Set<SelectionKey> selected = selector.selectedKeys();
					Iterator<SelectionKey> it = selected.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						dispatch(key);
						it.remove();
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private void dispatch(SelectionKey key) {
			if (key.isConnectable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				// 如果正在连接，则完成连接
				if (sc.isConnectionPending()) {
					logger.debug("a connection operation is in progress ");
					try {
						sc.finishConnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					logger.debug("a connection operation is not in progress ");
				}

				try {
					logger.debug("key is isConnectable");
					sc.register(selector, SelectionKey.OP_WRITE);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
			}
			if (key.isWritable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				Queue<RpcRequest> queue = channelRpcRequestQueue.get(sc);

				RpcRequest req = queue.poll();
				if (req != null) {
					try {
						logger.debug("key is isWritable, RpcRequest {}", req);
						sc.write(ByteBuffer.wrap(Serializer.serialize(req)));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				try {
					sc.register(selector, SelectionKey.OP_READ);
				} catch (ClosedChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 还有待发送的数据
				if (!queue.isEmpty())
					try {
						sc.register(selector, SelectionKey.OP_WRITE);
					} catch (ClosedChannelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(10240);
				int readBytes = 0;
				try {
					logger.debug("key is isReadable");
					readBytes = sc.read(readBuffer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					sc.register(selector, SelectionKey.OP_WRITE);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}

				if (readBytes > 0) {
					logger.debug("read {} bytes from SocketChannel {}",
							readBytes, sc);
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);

					// 由于只有一种响应类型，即RpcResponse，所以不用判断反序列化的类型。
					RpcResponse response = Serializer.deserialize(bytes);
					logger.debug("received a RpcResponse: {}", response);

					RpcCallbackFuture future = rpcCallbackFutureMap
							.get(response.getRequestId());

					// 设置rpcFuture的结果
					if (response.getError() != null) {
						future.setThrowable(response.getError());
					} else {
						future.setResult(response.getResult());
						logger.debug("set Result to {} of Future {}",
								response.getResult(), future);
					}

				} else if (readBytes < 0) {

					logger.debug("read {} bytes from SocketChannel {}",
							readBytes, sc);
					
				} else if (readBytes == 0) {
					logger.debug("read nothing from SocketChannel {} ", sc);
				}

				try {
					sc.register(selector, SelectionKey.OP_WRITE);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
