package com.qrpc.test.monitor;

import java.util.concurrent.TimeUnit;

import com.qrpc.monitor.ServiceMonitor;


/**
 * 监控中心测试
 */
public class ServiceMonitorTest {


	public static void main(String[] args) {

		while (true) {
			//每隔2秒打印一下服务中心的即时服务统计信息
			ServiceMonitor.sumerize();
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
