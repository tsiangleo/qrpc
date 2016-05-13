package com.github.tsiangleo.qrpc.service.provider;

import java.util.ArrayList;
import java.util.List;

import com.github.tsiangleo.qrpc.service.Hello;

public class HelloImpl implements Hello {

	@Override
	public String sayHi() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "hi, everybody!";
	}

	@Override
	public String sayHi(String name) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "hi,"+name+"!";
	}

	@Override
	public List<String> sayHello(List<String> names) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> resultList = new ArrayList<String>();
		for(String name:names){
			resultList.add("hi,"+name+"!");
		}
		return resultList;
	}
}
