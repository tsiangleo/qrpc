package com.github.tsiangleo.qrpc.monitor;

import java.util.concurrent.TimeUnit;

import com.github.tsiangleo.qrpc.monitor.ServiceMonitor;



/**
 * 监控中心测试
 */
public class ServiceMonitorTest {


	public static void main(String[] args) {

		ServiceMonitor monitor  = new ServiceMonitor("localhost:2181", "/qrpc-test01");
		
		while (true) {
			//每隔2秒打印一下服务中心的即时服务统计信息
			monitor.sumerize();
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
