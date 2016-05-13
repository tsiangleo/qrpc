package com.github.tsiangleo.qrpc.service;

import java.util.List;

/**
 * Hello服务接口
 * @author tsiangleo
 */
public interface Hello {

	String sayHi();
	
	String sayHi(String name);
	
	List<String> sayHello(List<String> names);
}
