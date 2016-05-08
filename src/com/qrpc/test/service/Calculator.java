package com.qrpc.test.service;

import java.io.FileNotFoundException;

/**
 * Calculator服务接口
 * @author tsiangleo 2016年4月25日 下午9:26:34
 */
public interface Calculator {
	int add(int a,int b);
	int minus(Integer a,Integer b) throws FileNotFoundException ;
	
	int randdom();
}
