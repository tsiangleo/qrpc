package com.github.tsiangleo.qrpc.provider;


import org.apache.log4j.Logger;

import com.github.tsiangleo.qrpc.consumer.RpcInvokeHook;
import com.github.tsiangleo.qrpc.service.Calculator;
import com.github.tsiangleo.qrpc.service.Hello;
import com.github.tsiangleo.qrpc.service.TypeService;
import com.github.tsiangleo.qrpc.service.provider.CalculatorImpl;
import com.github.tsiangleo.qrpc.service.provider.HelloImpl;
import com.github.tsiangleo.qrpc.service.provider.TypeServiceImpl;



/**
 * 模拟实际用户的使用场景
 * 
 * @author tsiangleo 2016年5月3日 下午3:43:19
 */
public class ServerTest {
	public static void main(String[] args) {
		RpcInvokeHook hook = new RpcInvokeHook() {
			public void beforeInvoke(String method, Object[] args) {
				System.out.println("before invoke " + method);
			}

			public void afterInvoke(String method, Object[] args) {
				System.out.println("after invoke " + method);
			}
		};

		// 创建一个rpc服务器
		RpcServer rpcServer = new RpcServer(9090,true,"localhost:2181", "/qrpc-test01");
		// 设置该rpc服务器提供的服务
		rpcServer.addServiceInterfaceAndProvider(Calculator.class,
				new CalculatorImpl());
		rpcServer.addServiceInterfaceAndProvider(Hello.class, new HelloImpl());
		rpcServer.addServiceInterfaceAndProvider(TypeService.class,
				new TypeServiceImpl());
		// rpc服务器的一些配置
		rpcServer.setHook(hook);

		rpcServer.start();
	}
}
