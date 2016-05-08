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
import com.qrpc.proto.RpcRequest;
import com.qrpc.proto.RpcResponse;
import com.qrpc.serialization.Serializer;

/**
 * read -> decode -> compute -> encode -> send
 * 划分的关键，就是看哪一步可能会阻塞或者花费的时间比较长，就排队。
 * Divide processing into small tasks
 * Each task performs an action without blocking
 * 
 * @author tsiangleo 2016年5月4日 上午10:46:42
 */
public class RpcServerRequestNIOWithPoolHandler extends RpcServerRequestHandler {
	private static final Logger logger = LoggerFactory.getLogger(RpcServerRequestNIOWithPoolHandler.class);
			
	private Map<String, Object> instanceMap;
	//服务接口名 -> 服务对象
	private Map<String, Class> classMap;
	//服务接口名 -> 服务Class对象
	private RpcInvokeHook rpcInvokeHook;
	private int port;
	
	private boolean stop = false;
	
	public RpcServerRequestNIOWithPoolHandler(Map<String, Object> instanceMap,
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
	
	
	public void handleCore() throws IOException {
		if(selector == null)
			selector = Selector.open();
		if(serverSocketChannel == null)
			serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while(!stop){
			selector.select();
			
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
			selector.close();
		}
		if(serverSocketChannel != null){
			serverSocketChannel.close();
		}
		
	}
	
	private void dispatch(SelectionKey key) throws IOException {
		if(key.isValid()){
			if(key.isAcceptable()){
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			
			if(key.isReadable()){
				readDealWriteTask(key);
			}
		}
		
	}

	private void readDealWriteTask(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(10240);
		int readBytes = sc.read(readBuffer);
		if(readBytes > 0){
			readBuffer.flip();
			byte[] bytes = new byte[readBuffer.remaining()];
			readBuffer.get(bytes);
			
			//由于只有一种请求类型，即RpcRequest，所以不用判断反序列化的类型。
			RpcRequest rpcRequest = Serializer.deserialize(bytes);
			logger.debug("received a RpcRequest: {} ",rpcRequest);
			RpcResponse response = callService(rpcRequest);
			
			doWrite(sc,response);
		}else if(readBytes < 0){
			key.cancel();
			sc.close();
		}
	}

	private void doWrite(SocketChannel sc, RpcResponse response) throws IOException {
		byte[] bytes = Serializer.serialize(response);
		ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		sc.write(writeBuffer);
	}

	private RpcResponse callService(RpcRequest request) {
		Object serviceInstance = instanceMap.get(request.getServiceInterface());
		Class serviceClass = classMap.get(request.getServiceInterface());
		Method serviceMethod = null;
		Object result = null;
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		try {
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
//			e.printStackTrace();
			if(e instanceof InvocationTargetException)
				e = e.getCause();
			response.setError(e);
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

	@Override
	public void handle() {
		try {
			handleCore();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}


