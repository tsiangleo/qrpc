package com.qrpc.consumer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.consumer.RpcRequestConnector;
import com.qrpc.exception.RpcException;
import com.qrpc.proto.RpcRequest;
import com.qrpc.proto.RpcResponse;

/**
 * @author tsiangleo 2016年4月29日 下午5:37:59
 */
public class RpcBIOHandler{
	private static final Logger logger = LoggerFactory.getLogger(RpcBIOHandler.class);
	
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1,new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			return thread;
		}
	});
	
	public static void queue(Pack p) {
		executor.submit(new RpcRequestTask(p.getRequest(),
				p.getRpcCallbackFuture(),p.getRpcServerAddress()));
	}
	
	static class RpcRequestTask implements Runnable{
		private RpcRequest rpcRequest;
		private RpcCallbackFuture rpcFuture;
		private InetSocketAddress socketAddress;
		
		private RpcRequestConnector rpcConnector;
		
		public RpcRequestTask(RpcRequest rpcRequest, RpcCallbackFuture rpcFuture,InetSocketAddress socketAddress) {
			super();
			logger.debug("Entering the constructor of RpcRequestTask("
					+ "rpcRequest={},rpcFuture={},socketAddress={})",new Object[]{rpcRequest,rpcFuture,socketAddress});
			this.rpcRequest = rpcRequest;
			this.rpcFuture = rpcFuture;
			this.socketAddress = socketAddress;
			this.rpcConnector = new RpcRequestConnector(socketAddress);
		}
		
		@Override
		public void run() {
			
			try {
				//获取网络连接
				if(!rpcConnector.isConnected())
						rpcConnector.connect(socketAddress);
				//发送请求
				rpcConnector.send(rpcRequest);
				//获得结果
				RpcResponse response = new RpcResponse();
				rpcConnector.receive(response);
				//设置rpcFuture的结果
				if(response.getError() != null){
					rpcFuture.setThrowable(response.getError());
				}else{
					rpcFuture.setResult(response.getResult());
				}
				
			}catch (IOException e) {
				rpcFuture.setThrowable(new RpcException(RpcException.NETWORK_EXCEPTION,e.getMessage()));
			}finally{
				rpcConnector.close();
			}
		}

	}

	
}
