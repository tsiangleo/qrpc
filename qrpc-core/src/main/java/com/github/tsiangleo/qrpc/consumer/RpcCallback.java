package com.github.tsiangleo.qrpc.consumer;

/**
 * 方法调用结束后，根据结果回调相应的方法。
 * @author tsiangleo 2016年4月29日 下午12:04:26
 */
public interface RpcCallback {
	
	public void onResult(Object result);

	public void onException(Throwable throwable);
}
