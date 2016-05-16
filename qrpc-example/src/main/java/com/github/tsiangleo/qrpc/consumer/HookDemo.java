package com.github.tsiangleo.qrpc.consumer;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tsiangleo.qrpc.service.Calculator;
import com.github.tsiangleo.qrpc.service.Hello;
import com.github.tsiangleo.qrpc.service.TypeService;
import com.github.tsiangleo.qrpc.service.UserInfo;
import com.github.tsiangleo.qrpc.service.UserService;
import com.github.tsiangleo.qrpc.consumer.RpcConsumerProxy;
import com.github.tsiangleo.qrpc.consumer.RpcInvokeHook;
import com.github.tsiangleo.qrpc.exception.RpcException;

/**
 * 该例子展示了Hook的用法
 * @author tsiangleo 2016年5月7日 下午11:14:35
 */
public class HookDemo {
	private static final Logger logger = LoggerFactory
			.getLogger(HookDemo.class);

	public static void main(String[] args) {
		 
		testHook01();
	}
	
	
	
	public static void testHook01() {
		
		//创建钩子对象
		RpcInvokeHook hook = new RpcInvokeHook() {
			@Override
			public void beforeInvoke(String methodName, Object[] args) {
				System.out.println("method "+methodName+"() will be invoke with args "+args);
			}
			@Override
			public void afterInvoke(String methodName, Object[] args) {
				System.out.println("method "+methodName+"() has been invoked with args "+args);
			}
		};
		
		//获取服务接口
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.async(false) //同步模式
				.bind("localhost", 9090)
				.zk("localhost:2181", "/qrpc-test01")
				.hook(hook)
				.create();

		//同步调用方法
		String result = hello.sayHi("lee"); 
		System.out.println(result);

		//同步调用方法
		result = hello.sayHi();
		System.out.println(result);
		
		//同步调用方法
		List<String> resultList = hello.sayHello(Arrays.asList("liuq","jack","rose"));
		System.out.println(resultList);
		
		try {
			//模拟请求线程正在执行其他操作...
			Thread.currentThread().sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
