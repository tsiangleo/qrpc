package com.qrpc.test.service.provider;

import java.io.FileNotFoundException;

import com.qrpc.test.service.Calculator;

public class CalculatorImpl implements Calculator{

	@Override
	public int add(int a, int b) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return a + b;
	}

	@Override
	public int minus(Integer a, Integer b) throws FileNotFoundException{
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(a < b){
			throw new FileNotFoundException(" a is smaller than b");
		}
		
		return a - b;
	}

	@Override
	public int randdom() {
		return (int) (Math.random()*100);
	}

}
