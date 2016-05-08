package com.qrpc.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.qrpc.exception.RpcException;

/**
 * 带有回调机制的Future。
 * 为什么不用JDK自带的FutureTask呢？因为无法添加回调RpcFutureCallback。http://outofmemory.cn/java/guava/concurrent/ListenableFuture
 * 
 */
public class RpcCallbackFuture implements Future{

	public final static int STATE_AWAIT = 0;  
    public final static int STATE_SUCCESS = 1;  
    public final static int STATE_EXCEPTION = 2;  
      
    private CountDownLatch countDownLatch;  
    private Object result;  
    private Throwable throwable;  
    private int state;  
    private RpcCallback rpcCallback = null;  
      
    public RpcCallbackFuture()  
    {  
        countDownLatch = new CountDownLatch(1);  
        state = STATE_AWAIT;  
    }  
    
    public void setResult(final Object result)   
    {  
        this.result = result;  
        state = STATE_SUCCESS;  
         
        //开启一个线程，执行回调方法
        if(rpcCallback != null){
        	new Thread(new Runnable() {
				@Override
				public void run() {
					rpcCallback.onResult(result);
				}
			}).start();
        } 
        countDownLatch.countDown();  
    }  

	public void setCallback(RpcCallback rpcCallback) {
		this.rpcCallback = rpcCallback;
	}

	
    public void setThrowable(final Throwable throwable)   
    {  
        this.throwable = throwable;  
        state = STATE_EXCEPTION;  
          
        if(rpcCallback != null){  
        	if(!(throwable instanceof RpcException))	//非框架异常，而是业务调用异常
        	{	
        		//开启一个线程，执行回调方法
        		new Thread(new Runnable() {
    				@Override
    				public void run() {
    					rpcCallback.onException(throwable);  
    				}
    			}).start();
        	}
        }
        countDownLatch.countDown();  
    }  
      
    public boolean isDone()  
    {  
        return state != STATE_AWAIT;  
    }  
      
    public void addRpcCallback(RpcCallback rpcCallback)   
    {  
        this.rpcCallback = rpcCallback;  
    }



	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}



	/**
	 * 异常说明：（1）用户接口抛出的异常封装在ExecutionException中，调用者应该调用ExecutionException.getCause()获取对应的异常。
	 *		（2）运行时可能抛出RpcException，代表框架的异常。
	 */
	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
        boolean awaitSuccess = true;  
        awaitSuccess = countDownLatch.await(timeout, TimeUnit.MILLISECONDS);  
        if(!awaitSuccess)  
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION,"Timeout");  
          
        if(state == STATE_SUCCESS)  
            return result;  
        else if(state == STATE_EXCEPTION) {
        	if(throwable instanceof RpcException)
        		throw (RpcException)throwable;
        	else 
        		throw new ExecutionException(throwable);	//ExecutionException中只包装业务异常
        }
        else //should not run to here!  
            throw new RpcException(RpcException.UNKNOWN_EXCEPTION,"RpcFuture Exception!");
	}



	/**
	 * 异常说明：（1）用户接口抛出的异常封装在ExecutionException中，调用者应该调用ExecutionException.getCause()获取对应的异常。
	 *		（2）运行时可能抛出RpcException，代表框架的异常。
	 */
	@Override
	public Object get() throws InterruptedException, ExecutionException {
        countDownLatch.await();  
        if(state == STATE_SUCCESS)  
            return result;  
        else if(state == STATE_EXCEPTION){
        	if(throwable instanceof RpcException)
        		throw (RpcException)throwable;
        	else 
        		throw new ExecutionException(throwable);	//ExecutionException中只包装业务异常
        }
        else //should not run to here!  
            throw new RpcException(RpcException.UNKNOWN_EXCEPTION,"RpcFuture Exception!");
	}
}
