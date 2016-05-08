package com.qrpc.provider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.consumer.RpcInvokeHook;
import com.qrpc.proto.RpcRequest;
import com.qrpc.proto.RpcResponse;
import com.qrpc.serialization.Serializer;

public class RpcServerRequestBIOHandler extends RpcServerRequestHandler {
	private static final Logger logger = LoggerFactory.getLogger(RpcServerRequestBIOHandler.class);
	private Map<String, Object> instanceMap;
	//服务接口名 -> 服务对象
	private Map<String, Class> classMap;
	//服务接口名 -> 服务Class对象
	private RpcInvokeHook rpcInvokeHook;
	private int port;
	
	private volatile boolean stop = false;
	private ServerSocket serverSocket = null;
	private ExecutorService executor = Executors.newCachedThreadPool();
	/**
	 * 该对象用于关闭executor线程池
	 */
	public final RpcServerRequestTask SHUTDOWN = new RpcServerRequestTask(null);
	
	
	public RpcServerRequestBIOHandler(Map<String, Object> instanceMap,
			Map<String, Class> classMap, int port,
			RpcInvokeHook rpcInvokeHook) {
		this.instanceMap = instanceMap;
		this.classMap = classMap;
		this.port = port;
		this.rpcInvokeHook = rpcInvokeHook;
	}

	@Override
	public void handle() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(!stop){
			Socket socket = null;
			try {
				logger.debug("RpcServer is waiting for a connection");
				socket = serverSocket.accept();
				executor.submit(new RpcServerRequestTask(socket));
				logger.debug("RpcServer accepted a connection");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stop() {
		//关闭主线程
		stop = true;
		try {
			serverSocket.close();	//因为主线程可能阻塞在serverSocket.accept()方法上，直接关闭底层资源。
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//关闭executor线程池
		if(!executor.isShutdown()){
			executor.submit(SHUTDOWN);
		}
	}
	
	class RpcServerRequestTask implements Runnable{
		private Socket socket;
		
		public RpcServerRequestTask(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			testShutDown();
			
			RpcRequest request = getRpcRequest();
			logger.debug("received a RpcRequest:{}",request);
			RpcResponse response = callService(request);
			sendRpcResponse(response);
			logger.debug("send a RpcResponse:{}",response);
		}
		/**
		 * 关闭executor线程池
		 */
		private void testShutDown(){
			if(this == SHUTDOWN && !executor.isShutdown()){
				logger.debug("shutdown ThreadPool");
				executor.shutdown();
			}
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
//				e.printStackTrace();
				if(e instanceof InvocationTargetException)
					e = e.getCause();
				response.setError(e);
			}
			return response;
		}
		private void sendRpcResponse(RpcResponse response) {
			try {
				socket.getOutputStream().write(Serializer.serialize(response));
				socket.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private RpcRequest getRpcRequest() {
			try {
				RpcRequest rep =  Serializer.deserialize(socket.getInputStream());
				return rep;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
}


