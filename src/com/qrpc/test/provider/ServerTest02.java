package com.qrpc.test.provider;

import com.qrpc.consumer.RpcInvokeHook;
import com.qrpc.provider.RpcServer;
import com.qrpc.test.service.Calculator;
import com.qrpc.test.service.Hello;
import com.qrpc.test.service.UserService;
import com.qrpc.test.service.provider.CalculatorImpl;
import com.qrpc.test.service.provider.HelloImpl;
import com.qrpc.test.service.provider.UserServiceImpl;

/**
 * 模拟实际用户的使用场景
 * @author tsiangleo 2016年5月3日 下午3:43:19
 */
public class ServerTest02 {
	public static void main(String[] args) {
		
		RpcInvokeHook hook = new RpcInvokeHook()   
		{             
		    public void beforeInvoke(String method, Object[] args)   
		    {  
		        System.out.println("before invoke " + method);  
		    }  
		      
		    public void afterInvoke(String method, Object[] args)   
		    {  
		        System.out.println("after invoke " + method);  
		    }  
		};  
		
		
		//创建一个rpc服务器
		RpcServer rpcServer = new RpcServer(8080,false);
		
		//设置该rpc服务器提供的服务
		rpcServer.addServiceInterfaceAndProvider(UserService.class,new UserServiceImpl());
		//rpc服务器的一些配置
		rpcServer.setHook(hook);
		
		rpcServer.start();
	}
}

