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
import com.qrpc.consumer.RpcConsumerProxy;
import com.qrpc.exception.RpcException;
import com.qrpc.test.service.Calculator;
import com.qrpc.test.service.Hello;
import com.qrpc.test.service.TypeService;
import com.qrpc.test.service.UserInfo;
import com.qrpc.test.service.UserService;

/**
 * rpc客户端处理异常的示例
 * 
 * @author tsiangleo 2016年5月7日 下午9:59:55
 */
public class ExceptionDemoTest {
	private static final Logger logger = LoggerFactory
			.getLogger(ExceptionDemoTest.class);

	public static void main(String[] args) {
		synchronizeTest();
		asynchronizeTest();
	}

	/**
	 * 异步测试
	 */
	private static void asynchronizeTest() {
		//异步测试
		 Calculator calculator2 = (Calculator) new RpcConsumerProxy()
		 .serviceInterface(Calculator.class)
		 .version("1.0")
		 .group("123")
		 .async(true)
		 .bind("localhost", 9090)
		 .create();
		 
		 try {
			 calculator2.minus(4, 7);
		} catch (FileNotFoundException e1) {
			System.out.println("异步调用异常："+e1);
		}
		 
		Future<Integer> calcFutureResult  = RpcCallContext
					.getContext().getFuture();
		 try {
			System.out.println(calcFutureResult.get());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			System.out.println("异步测试，异常信息："+e1.getCause());
			System.out.println("异步测试，详细信息："+e1.getCause().getMessage());
		}
	}

	/**
	 * 同步测试
	 */
	private static void synchronizeTest() {
		//同步测试
		 Calculator calculator = (Calculator) new RpcConsumerProxy()
		 .serviceInterface(Calculator.class)
		 .version("1.0")
		 .group("123")
		 .async(false)
		 .bind("localhost", 9090)
		 .create();
		
		int result = 0;
		try {
			result = calculator.minus(4, 7);
		} catch (FileNotFoundException e1) {
			System.out.println("同步测试，异常信息："+e1.getMessage());
			System.out.println("同步测试，详细信息："+e1);
		}
		System.out.println(result);
	}
}
