package com.github.tsiangleo.qrpc.provider;


import com.github.tsiangleo.qrpc.service.Calculator;
import com.github.tsiangleo.qrpc.service.Hello;
import com.github.tsiangleo.qrpc.service.UserService;
import com.github.tsiangleo.qrpc.service.provider.CalculatorImpl;
import com.github.tsiangleo.qrpc.service.provider.HelloImpl;
import com.github.tsiangleo.qrpc.service.provider.UserServiceImpl;
import com.github.tsiangleo.qrpc.consumer.RpcInvokeHook;
import com.github.tsiangleo.qrpc.provider.RpcServer;

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

