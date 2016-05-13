package com.github.tsiangleo.qrpc.consumer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tsiangleo.qrpc.proto.RpcRequest;
import com.github.tsiangleo.qrpc.proto.RpcResponse;
import com.github.tsiangleo.qrpc.serialization.Serializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
public class RpcNettyIOHandler {
	private static BlockingQueue<Pack> packQueue = new LinkedBlockingQueue<Pack>();
	
	private static Thread handlerThread = new Thread(new RpcNettyIOHandlerTask(packQueue),"nettyIOhandlerThread");
	
	static{
		handlerThread.start();
	}
	
	public static void queue(Pack pack){
		packQueue.offer(pack);
	}
}

class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest>{

	@Override
	protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out)
			throws Exception {
		out.writeBytes(Serializer.serialize(msg));
	}
}

class RpcResponseDecoder extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		RpcResponse response = Serializer.deserialize(in.array());
		out.add(response);
	}
}

class RpcNettyIOHandlerTask implements Runnable{
	private static final Logger logger = LoggerFactory
			.getLogger(RpcNettyIOHandler.class);	
	private  Bootstrap bootstrap;
	private  EventLoopGroup workerGroup;
	private  Map<InetSocketAddress,Channel> channels;
	private  Map<String, RpcCallbackFuture> rpcCallbackFutureMap;
	private  Map<Channel, Queue<RpcRequest>> channelRpcRequestQueueMap;
	
	private BlockingQueue<Pack> packQueue;
	
	@Override
	public  void run(){
		while(!Thread.interrupted()){
			Pack pack = null;
			try {
				pack = packQueue.take();
				connect(pack);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public RpcNettyIOHandlerTask(BlockingQueue<Pack> packQueue) {
		this.packQueue = packQueue;
		workerGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap(); // (1)
		bootstrap.group(workerGroup); // (2)
		bootstrap.channel(NioSocketChannel.class); // (3)
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new RpcResponseDecoder());
				ch.pipeline().addLast(new RpcClientInboundHandler());
				ch.pipeline().addLast(new RpcRequestEncoder());
			}
		});
		channels = new HashMap<InetSocketAddress, Channel>();
		rpcCallbackFutureMap = new ConcurrentHashMap<String, RpcCallbackFuture>();
		channelRpcRequestQueueMap = new ConcurrentHashMap<Channel, Queue<RpcRequest>>();
	}
	
	public void connect(Pack pack){
		logger.debug("queue {}",pack);
		InetSocketAddress remoteAddress = pack.getRpcServerAddress();
		if(channels.containsKey(remoteAddress)){
			Channel channel = channels.get(remoteAddress);
			logger.debug("found channel,add to dataQueue",channel.remoteAddress());
			synchronized (channelRpcRequestQueueMap) {
				channelRpcRequestQueueMap.get(channel).add(pack.getRequest());
			}
			
		}else{
			//新建一个channel并缓存到map中
			Channel channel = null;
			
			channel = bootstrap.connect(remoteAddress).channel();
			logger.debug("connect to {}", remoteAddress.toString());
			
			System.out.println("isWritable:"+channel.isWritable());
			//发送数据
			try {
				channel.writeAndFlush(pack.getRequest()).sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//将channel并缓存到map中
			channels.put(remoteAddress,channel);
			logger.debug("create a new channel,cached in map");

			//新建一个该channel的待发送数据队列
			Queue<RpcRequest> queue = new ConcurrentLinkedQueue<RpcRequest>();
			synchronized (channelRpcRequestQueueMap) {
				channelRpcRequestQueueMap.put(channel, queue);
			}
			logger.debug("create a new dataQueue for channel,cached in map");
			
			//保存该RpcRequest的future对象
			rpcCallbackFutureMap.put(pack.getRequest().getRequestId(),
					pack.getRpcCallbackFuture());
		}
	}

	public  void close(){
		workerGroup.shutdownGracefully();
    }
	
	
	 class RpcClientInboundHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			logger.debug("channelActive {} ",ctx.channel().remoteAddress());
			super.channelInactive(ctx);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			logger.debug("channelInactive {} ",ctx.channel().remoteAddress());
			super.channelInactive(ctx);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			logger.debug("channelRead {} msg {}",ctx.channel().remoteAddress(),msg);
			
			RpcResponse response = (RpcResponse) msg;
			
			logger.debug("received a RpcResponse: {}", response);
			
			RpcCallbackFuture future = rpcCallbackFutureMap.get(response.getRequestId());
			
			// 设置rpcFuture的结果
			if (response.getError() != null) {
				future.setThrowable(response.getError());
			} else {
				future.setResult(response.getResult());
				logger.debug("set Result to {} of Future {}", response.getResult(),	future);
			}
			
			rpcCallbackFutureMap.remove(response.getRequestId());
			super.channelRead(ctx, msg);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			logger.debug("channelReadComplete {} ",ctx.channel().remoteAddress());
			
			if(ctx.channel() != null){
				//查看该channel是否还有待发送的数据
				synchronized (channelRpcRequestQueueMap) {
					Queue<RpcRequest> queue = channelRpcRequestQueueMap.get(ctx.channel());
					if(queue != null && !queue.isEmpty()){
						RpcRequest request = queue.poll();
						ctx.writeAndFlush(request);
					}
				}
				
			}
			
			super.channelReadComplete(ctx);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			logger.debug("exceptionCaught {} ",ctx.channel().remoteAddress());
			super.exceptionCaught(ctx, cause);
		}

	}
}
