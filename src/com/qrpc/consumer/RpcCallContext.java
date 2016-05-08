package com.qrpc.consumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * 
 * Thread local context. (API, ThreadLocal, ThreadSafe)
 * 
 * RpcCallContext保存了一次远程调用的所有信息。线程本地对象，每个请求线程发起调用都会有各自的副本。
 * 该类里面的方法应该由请求线程调用，而不应该由非请求线程（比如IO线程）调用。
 * 
 * 注意：RpcCallContext是一个临时状态记录器，当接收到RPC请求，或发起RPC请求时，RpcCallContext的状态都会变化。
 * 比如：A调B，B再调C，则B机器上，在B调C之前，RpcCallContext记录的是A调B的信息，在B调C之后，RpcCallContext记录的是B调C的信息。
 * 
 * 借鉴自dubbo的RpcContext类。
 * 
 * @author tsiangleo 2016年5月6日 下午8:26:26
 */
public class RpcCallContext {

	private static ThreadLocal<RpcCallContext> localCallContext = new ThreadLocal<RpcCallContext>() {
		@Override
		protected RpcCallContext initialValue() {
			return new RpcCallContext();
		}
	};

	public static RpcCallContext getContext() {
		return localCallContext.get();
	}

	private RpcCallContext() {
	}

	private Future future;

	private Method method;
	private Class<?>[] parameterTypes;
	private Object[] arguments;
	private RpcCallback rpcCallback;
	
	

	@SuppressWarnings("unchecked")
	public <T> Future<T> getFuture() {
		return (Future<T>)future;
	}

	/**
	 * 该方法由请求线程在RpcInvocationHandler的invoke方法调用。
	 * 
	 * @param future
	 */
	public void setFuture(RpcCallbackFuture future) {
		this.future = future;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	
	public RpcCallback getCallback() {
		return rpcCallback;
	}

	/**
	 * 设置方法调用完成后的Callback对象。
	 * @param rpcCallback
	 */
	public void setCallback(RpcCallback rpcCallback) {
		this.rpcCallback = rpcCallback;
	}

	@Override
	public String toString() {
		return "RpcCallContext [future=" + future + ", method=" + method
				+ ", parameterTypes=" + Arrays.toString(parameterTypes)
				+ ", arguments=" + Arrays.toString(arguments) + "]";
	}

}
