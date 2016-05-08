package com.qrpc.test.provider;

import com.qrpc.provider.RpcServer;
import com.qrpc.test.service.Calculator;
import com.qrpc.test.service.Hello;
import com.qrpc.test.service.TypeService;
import com.qrpc.test.service.UserService;
import com.qrpc.test.service.provider.CalculatorImpl;
import com.qrpc.test.service.provider.HelloImpl;
import com.qrpc.test.service.provider.TypeServiceImpl;
import com.qrpc.test.service.provider.UserServiceImpl;

/**
 * 模拟实际用户的使用场景
 * @author tsiangleo 2016年5月3日 下午3:43:19
 */
public class ServerTest03 {
	public static void main(String[] args) {
		
		if(args == null || args.length != 1){
			System.out.println("usag: java ServerTest03 <port>");
			System.exit(1);
		}

		int port  = Integer.parseInt(args[0]);
		
		//创建一个rpc服务器
		RpcServer rpcServer = new RpcServer(port,true);
		
		//设置该rpc服务器提供的服务
		rpcServer.addServiceInterfaceAndProvider(Calculator.class,new CalculatorImpl());
		rpcServer.addServiceInterfaceAndProvider(Hello.class, new HelloImpl());
		rpcServer.addServiceInterfaceAndProvider(UserService.class, new UserServiceImpl());
		rpcServer.addServiceInterfaceAndProvider(TypeService.class,new TypeServiceImpl());
		
		rpcServer.start();
	}
}

