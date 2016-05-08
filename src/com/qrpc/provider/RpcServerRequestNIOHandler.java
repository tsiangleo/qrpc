package com.qrpc.provider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.consumer.RpcInvokeHook;
import com.qrpc.exception.RpcException;
import com.qrpc.proto.RpcRequest;
import com.qrpc.proto.RpcResponse;
import com.qrpc.serialization.Serializer;

public class RpcServerRequestNIOHandler extends RpcServerRequestHandler {
	private static final Logger logger = LoggerFactory.getLogger(RpcServerRequestNIOHandler.class);
	private Map<String, Object> instanceMap;
	//服务接口名 -> 服务对象
	private Map<String, Class> classMap;
	//服务接口名 -> 服务Class对象
	private RpcInvokeHook rpcInvokeHook;
	private int port;
	
	private boolean stop = false;
	
	public RpcServerRequestNIOHandler(Map<String, Object> instanceMap,
			Map<String, Class> classMap, int port,
			RpcInvokeHook rpcInvokeHook) {
		this.instanceMap = instanceMap;
		this.classMap = classMap;
		this.port = port;
		this.rpcInvokeHook = rpcInvokeHook;
	}

	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	
//	private LinkedBlockingQueue<RpcRequest> queue;
	
	
	@Override
	public void handle() {
		if(selector == null)
			try {
				selector = Selector.open();
			} catch (IOException e) {
				logger.warn("an IOException happend,when call Selector.open(),exception is:{}",e);
			}
		if(serverSocketChannel == null)
			try {
				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.socket().bind(new InetSocketAddress(port));
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				logger.warn("an IOException happend,when do ops on ServerSocketChannel,exception is:{}",e);
			}
		
		while(!stop){
			try {
				selector.select();
			} catch (IOException e) {
				logger.warn("an IOException happend,when call selector.select(),exception is:{}",e);
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = selectedKeys.iterator();
			SelectionKey key = null;
			while(it.hasNext()){
				key = it.next();
				dispatch(key);
			}
			selectedKeys.clear();
		}
		
		if(selector != null){
			try {
				selector.close();
			} catch (IOException e) {
				logger.warn("an IOException happend,when call selector.close(),exception is:{}",e);
			}
		}
		if(serverSocketChannel != null){
			try {
				serverSocketChannel.close();
			} catch (IOException e) {
				logger.warn("an IOException happend,when call serverSocketChannel.close(),exception is:{}",e);
			}
		}
		
	}
	
	private void dispatch(SelectionKey key){
			if(key.isValid() && key.isAcceptable()){
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = null;
				try {
					sc = ssc.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sc.configureBlocking(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sc.register(selector, SelectionKey.OP_READ);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
			}
			
			if(key.isValid() && key.isReadable()){
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(10240);
				int readBytes = 0;
				try {
					readBytes = sc.read(readBuffer);
				} catch (IOException e) {
					//服务端读数据的时候，客户端突然意外关闭。
					logger.warn("an IOException happend,when call socketChannel.read(),exception is:{}",e.getMessage());
					key.cancel();
					try {
						sc.socket().close();
						sc.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if(readBytes > 0){
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					
					//由于只有一种请求类型，即RpcRequest，所以不用判断反序列化的类型。
					RpcRequest rpcRequest = Serializer.deserialize(bytes);
					logger.debug("received a RpcRequest: {}",rpcRequest);
					RpcResponse response = callService(rpcRequest);
					
					doWrite(sc,response);
					logger.debug("send a RpcResponse: {}",response);
				}else if(readBytes < 0){
					key.cancel();
					try {
						sc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		
		
	}

	private void doWrite(SocketChannel sc, RpcResponse response){
		byte[] bytes = Serializer.serialize(response);
		ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		try {
			sc.write(writeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RpcResponse callService(RpcRequest request) {
		logger.debug("Entering callService(request={})",request);
		Method serviceMethod = null;
		Object result = null;
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		try {
			Object serviceInstance = instanceMap.get(request.getServiceInterface());
			Class serviceClass = classMap.get(request.getServiceInterface());
			if(serviceInstance == null || serviceClass == null)
				throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,"Bad request,no serviceProvider for service "
						+request.getServiceInterface()+" on this RpcServer");
			
			if(request.getParameterTypes() == null)
				serviceMethod = serviceClass.getMethod(request.getMethodName());
			else
				serviceMethod = serviceClass.getMethod(request.getMethodName(), request.getParameterTypes());
			if(rpcInvokeHook != null)  
                rpcInvokeHook.beforeInvoke(request.getMethodName(), request.getArgs());
			
			result = serviceMethod.invoke(serviceInstance, request.getArgs());
			response.setResult(result);
			
			if(rpcInvokeHook != null)  
                rpcInvokeHook.afterInvoke(request.getMethodName(), request.getArgs());
		} catch (Throwable e) {
			if(e instanceof InvocationTargetException)
				e = e.getCause();
			response.setError(e);
			logger.warn("an exception happend when call method {}.{}(args={}),exception is:{}",
					new Object[]{request.getServiceInterface(),request.getMethodName(),request.getArgs(),e});
		}
		return response;
	}
	
	@Override
	public void stop() {
		stop = true;
		
		//关闭selector，中断线程
		try {
			selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
}


