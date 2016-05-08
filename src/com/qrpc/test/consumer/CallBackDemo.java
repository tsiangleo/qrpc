package com.qrpc.test.consumer;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.consumer.RpcCallContext;
import com.qrpc.consumer.RpcCallback;
import com.qrpc.consumer.RpcConsumerProxy;
import com.qrpc.exception.RpcException;
import com.qrpc.test.service.Calculator;
import com.qrpc.test.service.Hello;
import com.qrpc.test.service.TypeService;
import com.qrpc.test.service.UserInfo;
import com.qrpc.test.service.UserService;

/**
 * 该例子展示了CallBack的用法
 * @author tsiangleo 2016年5月7日 下午11:14:35
 */
public class CallBackDemo {
	private static final Logger logger = LoggerFactory
			.getLogger(CallBackDemo.class);

	public static void main(String[] args) {
		 
		testCallBack03();
	}
	
	/**
	 * 一个接口的所有方法共用一个callBack
	 */
	public static void testCallBack03() {
		
		//创建一个callBack对象
		RpcCallback rpcCallback = new RpcCallback() {
			@Override
			public void onResult(Object result) {
				System.out.println(" 方法调用成功！返回结果是："+result);
			}
			@Override
			public void onException(Throwable throwable) {
				System.out.println(" 方法调用失败！异常是："+throwable);
			}
		};
		
		//获取服务接口
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class).version("1.0").group("123")
				.async(true)//异步模式
				.bind("localhost", 9090)
				.callBack(rpcCallback)
				.create();
	
		//异步调用方法
		try {
			calculator.minus(5, 3);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} 
		//异步调用方法
		calculator.add(4, 3);
		//异步调用方法
		try {
			calculator.minus(2, 3);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} 
		
		try {
			//模拟请求线程正在执行其他操作...
			Thread.currentThread().sleep(29000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void testCallBack02() {
		//获取服务接口
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class).version("1.0").group("123")
				.async(true)//异步模式
				.bind("localhost", 9090).create();
		//创建一个callBack对象
		RpcCallback rpcCallback = new RpcCallback() {
			@Override
			public void onResult(Object result) {
				System.out.println("Calculator的方法调用成功！返回结果是："+result);
			}
			@Override
			public void onException(Throwable throwable) {
				System.out.println("Calculator的方法调用！异常是："+throwable);
			}
		};
		//调用某个方法前先设置callBack，注意：一定要在对应的方法调用之前设置。
		RpcCallContext.getContext().setCallback(rpcCallback);
		//异步调用方法
		try {
			calculator.minus(2, 3);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} 

		try {
			//模拟请求线程正在执行其他操作...
			Thread.currentThread().sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testCallBack01() {
		//获取服务接口
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.async(true) //异步模式
				.bind("localhost", 9090).create();
		//创建一个callBack对象
		RpcCallback rpcCallback = new RpcCallback() {
			@Override
			public void onResult(Object result) {
				System.out.println("hello.sayHi()方法调用成功！返回结果是："+result);
			}
			@Override
			public void onException(Throwable throwable) {
				System.out.println("hello.sayHi()方法调用！异常是："+throwable);
			}
		};
		//调用某个方法前先设置callBack，注意：一定要在对应的方法调用之前设置。
		RpcCallContext.getContext().setCallback(rpcCallback);
		//异步调用方法
		String result = hello.sayHi("lee"); 
		System.out.println(result);

		
		try {
			//模拟请求线程正在执行其他操作...
			Thread.currentThread().sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
