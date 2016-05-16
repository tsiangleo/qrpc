package com.github.tsiangleo.qrpc.consumer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tsiangleo.qrpc.service.Calculator;
import com.github.tsiangleo.qrpc.service.Hello;
import com.github.tsiangleo.qrpc.service.UserInfo;
import com.github.tsiangleo.qrpc.service.UserService;
import com.github.tsiangleo.qrpc.consumer.RpcCallContext;
import com.github.tsiangleo.qrpc.consumer.RpcConsumerProxy;


public class ClientTest {
	private static final Logger logger = LoggerFactory
			.getLogger(ClientTest.class);

	public static void main(String[] args) {
		syncTest();
		System.out.println("--------异步测试开始---------");
		asyncTest();
	}
	
	/**
	 * 同步测试-zk版
	 */
	public static void zk_syncTest() {
		 
		//服务接口1测试
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.zk("localhost:2181", "/qrpc-test01")
				.async(false)
				.create();
		
		System.out.println(hello.sayHi());
		System.out.println(hello.sayHello(Arrays.asList("liuq","jack","rose")));
		System.out.println(hello.sayHi("lqiu"));
		
		//服务接口2测试
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class)
				.version("1.0").group("124")
				.async(false)
				.create();
		
		int result = calculator.add(2, 3);
		System.out.println("calculator.add(2, 3):"+result);
		
		try {
			result = calculator.minus(4, 3);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("calculator.minus(4, 3):"+result);
		result = calculator.randdom();
		System.out.println("calculator.randdom() :"+result);
		
		
		//服务接口3测试
		UserService userService = (UserService) new RpcConsumerProxy()
		.serviceInterface(UserService.class)
		.version("1.0").group("124")
		.async(false)
		.create();
		
		UserInfo uerinfo = userService.getUserInfo(0);
		System.out.println("userService.getUserInfo(0):"+uerinfo);
		
		List<UserInfo> uerinfoList = userService.getUserinfos();
		System.out.println("userService.getUserinfos():"+uerinfoList);
		
		List<UserInfo> newuserinfos = new ArrayList<UserInfo>();
		newuserinfos.add(new UserInfo("new liuq2",22));
		newuserinfos.add(new UserInfo("new liuq3",23));
		newuserinfos.add(new UserInfo("new liuq4",24));
		newuserinfos.add(new UserInfo("new liuq5",25));
		userService.setUserinfos(newuserinfos);
		
		 uerinfoList = userService.getUserinfos();
		System.out.println("after update, userService.getUserinfos():"+uerinfoList);
		
	}
	
	
	/**
	 * 异步测试-zk版
	 */
	public static void zk_asyncTest() {
		 
		//服务接口1测试
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.zk("localhost:2181", "/qrpc-test01")
				.async(true)
				.create();
		
		System.out.println(hello.sayHi());
		Future<String> hellofuture1 = RpcCallContext.getContext().getFuture();
		
		System.out.println(hello.sayHello(Arrays.asList("liuq","jack","rose")));
		Future<List<String>> hellofuture2 = RpcCallContext.getContext().getFuture();
		
		System.out.println(hello.sayHi("lqiu"));
		Future<String> hellofuture3 = RpcCallContext.getContext().getFuture();
		
		
		try {
			System.out.println("hellofuture1.get():"+hellofuture1.get());
			System.out.println("hellofuture2.get():"+hellofuture2.get());
			System.out.println("hellofuture3.get():"+hellofuture3.get());
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		//服务接口2测试
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class)
				.version("1.0").group("124")
				.async(true)
				.create();
		
		int result = calculator.add(2, 3);
		System.out.println("calculator.add(2, 3):"+result);
		Future<Integer> calculatorFuture1 = RpcCallContext.getContext().getFuture();
		
		Future<Integer> calculatorFuture2  = null;
		try {
			result = calculator.minus(4, 3);
			System.out.println("calculator.minus(4, 3):"+result);
			calculatorFuture2 = RpcCallContext.getContext().getFuture();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	
		
		result = calculator.randdom();
		Future<Integer> calculatorFuture3 = RpcCallContext.getContext().getFuture();
		System.out.println("calculator.randdom() :"+result);
		
		
		//服务接口3测试
		UserService userService = (UserService) new RpcConsumerProxy()
		.serviceInterface(UserService.class)
		.version("1.0").group("124")
		.async(true)
		.create();
		
		UserInfo uerinfo = userService.getUserInfo(0);
		System.out.println("userService.getUserInfo(0):"+uerinfo);
		Future<UserInfo> userServiceFuture1 = RpcCallContext.getContext().getFuture();
		
		
		List<UserInfo> uerinfoList = userService.getUserinfos();
		System.out.println("userService.getUserinfos():"+uerinfoList);
		Future<List<UserInfo>> userServiceFuture2 = RpcCallContext.getContext().getFuture();
		
		
		List<UserInfo> newuserinfos = new ArrayList<UserInfo>();
		newuserinfos.add(new UserInfo("new liuq2",22));
		newuserinfos.add(new UserInfo("new liuq3",23));
		newuserinfos.add(new UserInfo("new liuq4",24));
		newuserinfos.add(new UserInfo("new liuq5",25));
		userService.setUserinfos(newuserinfos);
		Future userServiceFuture3 = RpcCallContext.getContext().getFuture();
		
		 uerinfoList = userService.getUserinfos();
		System.out.println("after update, userService.getUserinfos():"+uerinfoList);
		Future<List<UserInfo>> userServiceFuture4 = RpcCallContext.getContext().getFuture();
		
		
		
		
		
		
		try {
			
			System.out.println("calculatorFuture1.get():"+calculatorFuture1.get());
			System.out.println("calculatorFuture2.get():"+calculatorFuture2.get());
			System.out.println("calculatorFuture3.get():"+calculatorFuture3.get());
			
			
			System.out.println("hellofuture1.get():"+hellofuture1.get());
			System.out.println("hellofuture2.get():"+hellofuture2.get());
			System.out.println("hellofuture3.get():"+hellofuture3.get());

			
			System.out.println("userServiceFuture1.get():"+userServiceFuture1.get());
			System.out.println("userServiceFuture2.get():"+userServiceFuture2.get());
			System.out.println("userServiceFuture3.get():"+userServiceFuture3.get());
			System.out.println("userServiceFuture4.get():"+userServiceFuture4.get());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	/**
	 * 同步测试
	 */
	public static void syncTest() {
		 
		//服务接口1测试
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.async(false)
				.zk("localhost:2181", "/qrpc-test01")
				.bind("localhost", 9090)
				.create();
		
		System.out.println(hello.sayHi());
		System.out.println(hello.sayHello(Arrays.asList("liuq","jack","rose")));
		System.out.println(hello.sayHi("lqiu"));
		
		//服务接口2测试
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class)
				.version("1.0").group("124")
				.bind("localhost", 9090)
				.async(false)
				.create();
		
		int result = calculator.add(2, 3);
		System.out.println("calculator.add(2, 3):"+result);
		
		try {
			result = calculator.minus(1, 3);
		} catch (FileNotFoundException e) {
			System.out.println("exception:"+e);;
		}
		System.out.println("calculator.minus(4, 3):"+result);
		result = calculator.randdom();
		System.out.println("calculator.randdom() :"+result);
		
		
		//服务接口3测试
		UserService userService = (UserService) new RpcConsumerProxy()
		.serviceInterface(UserService.class)
		.version("1.0").group("124")
		.async(false)
		.bind("localhost", 8080)
		.create();
		
		UserInfo uerinfo = userService.getUserInfo(0);
		System.out.println("userService.getUserInfo(0):"+uerinfo);
		
		List<UserInfo> uerinfoList = userService.getUserinfos();
		System.out.println("userService.getUserinfos():"+uerinfoList);
		
		List<UserInfo> newuserinfos = new ArrayList<UserInfo>();
		newuserinfos.add(new UserInfo("new liuq2",22));
		newuserinfos.add(new UserInfo("new liuq3",23));
		newuserinfos.add(new UserInfo("new liuq4",24));
		newuserinfos.add(new UserInfo("new liuq5",25));
		userService.setUserinfos(newuserinfos);
		
		 uerinfoList = userService.getUserinfos();
		System.out.println("after update, userService.getUserinfos():"+uerinfoList);
		
	}
	
	
	/**
	 * 异步测试
	 */
	public static void asyncTest() {
		 
		//服务接口1测试
		Hello hello = (Hello) new RpcConsumerProxy()
				.serviceInterface(Hello.class).version("1.0").group("123")
				.async(true)
				.zk("localhost:2181", "/qrpc-test01")
				.bind("localhost", 9090)
				.create();
		
		System.out.println(hello.sayHi());
		Future<String> hellofuture1 = RpcCallContext.getContext().getFuture();
		
		System.out.println(hello.sayHello(Arrays.asList("liuq","jack","rose")));
		Future<List<String>> hellofuture2 = RpcCallContext.getContext().getFuture();
		
		System.out.println(hello.sayHi("lqiu"));
		Future<String> hellofuture3 = RpcCallContext.getContext().getFuture();
		
		
		try {
			System.out.println("hellofuture1.get():"+hellofuture1.get());
			System.out.println("hellofuture2.get():"+hellofuture2.get());
			System.out.println("hellofuture3.get():"+hellofuture3.get());
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		//服务接口2测试
		Calculator calculator = (Calculator) new RpcConsumerProxy()
				.serviceInterface(Calculator.class)
				.version("1.0").group("124")
				.bind("localhost", 9090)
				.async(true)
				.create();
		
		int result = calculator.add(2, 3);
		System.out.println("calculator.add(2, 3):"+result);
		Future<Integer> calculatorFuture1 = RpcCallContext.getContext().getFuture();
		
		Future<Integer> calculatorFuture2  = null;
		try {
			result = calculator.minus(4, 3);
			System.out.println("calculator.minus(4, 3):"+result);
			calculatorFuture2 = RpcCallContext.getContext().getFuture();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	
		
		result = calculator.randdom();
		Future<Integer> calculatorFuture3 = RpcCallContext.getContext().getFuture();
		System.out.println("calculator.randdom() :"+result);
		
		
		//服务接口3测试
		UserService userService = (UserService) new RpcConsumerProxy()
		.serviceInterface(UserService.class)
		.version("1.0").group("124")
		.async(true)
		.bind("localhost", 8080)
		.create();
		
		UserInfo uerinfo = userService.getUserInfo(0);
		System.out.println("userService.getUserInfo(0):"+uerinfo);
		Future<UserInfo> userServiceFuture1 = RpcCallContext.getContext().getFuture();
		
		
		List<UserInfo> uerinfoList = userService.getUserinfos();
		System.out.println("userService.getUserinfos():"+uerinfoList);
		Future<List<UserInfo>> userServiceFuture2 = RpcCallContext.getContext().getFuture();
		
		
		List<UserInfo> newuserinfos = new ArrayList<UserInfo>();
		newuserinfos.add(new UserInfo("new liuq2",22));
		newuserinfos.add(new UserInfo("new liuq3",23));
		newuserinfos.add(new UserInfo("new liuq4",24));
		newuserinfos.add(new UserInfo("new liuq5",25));
		userService.setUserinfos(newuserinfos);
		Future userServiceFuture3 = RpcCallContext.getContext().getFuture();
		
		 uerinfoList = userService.getUserinfos();
		System.out.println("after update, userService.getUserinfos():"+uerinfoList);
		Future<List<UserInfo>> userServiceFuture4 = RpcCallContext.getContext().getFuture();
		
		
		
		
		
		
		try {
			System.out.println("hellofuture1.get():"+hellofuture1.get());
			System.out.println("hellofuture2.get():"+hellofuture2.get());
			System.out.println("hellofuture3.get():"+hellofuture3.get());
			
			System.out.println("calculatorFuture1.get():"+calculatorFuture1.get());
			System.out.println("calculatorFuture2.get():"+calculatorFuture2.get());
			System.out.println("calculatorFuture3.get():"+calculatorFuture3.get());
			
			System.out.println("userServiceFuture1.get():"+userServiceFuture1.get());
			System.out.println("userServiceFuture2.get():"+userServiceFuture2.get());
			System.out.println("userServiceFuture3.get():"+userServiceFuture3.get());
			System.out.println("userServiceFuture4.get():"+userServiceFuture4.get());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
