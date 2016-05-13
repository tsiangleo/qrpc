package com.github.tsiangleo.qrpc.consumer;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tsiangleo.qrpc.exception.RpcException;
import com.github.tsiangleo.qrpc.proto.RpcRequest;

public class RpcInvocationHandler implements InvocationHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(RpcInvocationHandler.class);

	// 每个代理对象的特定数据，不应该保存在RpcCallContext中。
	private InetSocketAddress rpcServerAddress;
	private Class<?> serviceInterface;
	private String group;
	private String version;
	private boolean async;
	private long timeoutMills;
	
	private RpcCallback rpcCallback;
	private RpcInvokeHook rpcInvokeHook;
	
	public RpcInvocationHandler(InetSocketAddress rpcServerAddress,
			Class<?> serviceInterface, String group, String version,
			boolean async,long timeoutMills,RpcCallback rpcCallback,
			RpcInvokeHook rpcInvokeHook) {
		super();
		this.rpcServerAddress = rpcServerAddress;
		this.serviceInterface = serviceInterface;
		this.group = group;
		this.version = version;
		this.async = async;
		this.timeoutMills = timeoutMills;
		this.rpcCallback = rpcCallback;
		this.rpcInvokeHook = rpcInvokeHook;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		RpcCallContext.getContext().setMethod(method);
		RpcCallContext.getContext().setArguments(args);
		RpcCallContext.getContext().setParameterTypes(
				method.getParameterTypes());

		RpcCallbackFuture future = new RpcCallbackFuture();
		RpcCallback callback = RpcCallContext.getContext().getCallback();
		if(callback == null){	//优先使用RpcCallContext.getContext()中的callBack
			callback = rpcCallback;
		}
		future.setCallback(callback);
		RpcCallContext.getContext().setFuture(future);
		
		RpcRequest request = new RpcRequest();
		request.setArgs(args);
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setRequestId(UUID.randomUUID().toString());
		request.setServiceInterface(serviceInterface.getCanonicalName());

		Pack p = new Pack(request, rpcServerAddress, future);

		logger.debug("add {} to queue.", p);

		
		if (rpcInvokeHook != null)
			rpcInvokeHook.beforeInvoke(method.getName(), args);
		
//		RpcNIOHandler.queue(p);
		RpcBIOHandler.queue(p);
//		RpcNettyIOHandler.queue(p);

		if (async) { // 异步调用
			if (method.getReturnType() == byte.class) {
				return (byte) 0;
			} else if (method.getReturnType() == boolean.class) {
				return false;
			} else if (method.getReturnType() == char.class) {
				return '0';
			} else if (method.getReturnType() == short.class) {
				return (short) 0;
			} else if (method.getReturnType() == int.class) {
				return (int) 0;
			} else if (method.getReturnType() == long.class) {
				return (long) 0L;
			} else if (method.getReturnType() == float.class) {
				return (float) 0.0f;
			} else if (method.getReturnType() == double.class) {
				return (double) 0.0;
			} else {
				logger.debug("return null,because unknown returnType {} ",
						method.getReturnType());
				return null;
			}
		} else { // 同步调用
			Object result = null;
			try {
				if(timeoutMills > 0){
					result = future.get(timeoutMills, TimeUnit.MILLISECONDS);
				}
				else {
					result = future.get();
				}
			} catch (Exception e) {
				if(e instanceof TimeoutException){
					throw new RpcException(RpcException.TIMEOUT_EXCEPTION,e.getMessage());
				} else if(e instanceof InterruptedException) {
					throw e;
				} else if (e instanceof ExecutionException) {
					throw e.getCause();
				} else if (e instanceof RpcException){
					throw e;
				} else {
					throw new RuntimeException(e);
				}
			}finally{
				if (rpcInvokeHook != null)
					rpcInvokeHook.afterInvoke(method.getName(), args);
			}
			return result;
		}
	}
}
class Pack {
	private RpcRequest request;
	private InetSocketAddress rpcServerAddress;
	private RpcCallbackFuture rpcFuture;

	public Pack(RpcRequest request, InetSocketAddress rpcServerAddress,
			RpcCallbackFuture rpcFuture) {
		super();
		this.request = request;
		this.rpcServerAddress = rpcServerAddress;
		this.rpcFuture = rpcFuture;
	}

	public RpcRequest getRequest() {
		return request;
	}

	public void setRequest(RpcRequest request) {
		this.request = request;
	}

	public InetSocketAddress getRpcServerAddress() {
		return rpcServerAddress;
	}

	public void setRpcServerAddress(InetSocketAddress rpcServerAddress) {
		this.rpcServerAddress = rpcServerAddress;
	}

	public RpcCallbackFuture getRpcCallbackFuture() {
		return rpcFuture;
	}

	public void setRpcCallbackFuture(RpcCallbackFuture rpcFuture) {
		this.rpcFuture = rpcFuture;
	}

	@Override
	public String toString() {
		return "Pack [request=" + request + ", rpcServerAddress="
				+ rpcServerAddress + ", rpcFuture=" + rpcFuture + "]";
	}

}
