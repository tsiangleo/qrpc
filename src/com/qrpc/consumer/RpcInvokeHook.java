package com.qrpc.consumer;


/**
 * 在调用每个方法的前后调用该接口的方法。
 * @author tsiangleo 2016年5月7日 下午10:46:12
 */
public interface RpcInvokeHook   
{  
    public void beforeInvoke(String methodName, Object[] args);  
    public void afterInvoke(String methodName, Object[] args);    
}  