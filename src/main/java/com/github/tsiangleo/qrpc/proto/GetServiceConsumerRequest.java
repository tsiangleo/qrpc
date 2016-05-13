
package com.github.tsiangleo.qrpc.proto;

import java.io.Serializable;

/**
 * 获取某个服务的所有消费者的信息
 * @author tsiangleo 2016年4月28日 下午6:58:05
 */
public class GetServiceConsumerRequest implements Serializable {
	private static final long serialVersionUID = 8499248564706076614L;
	private String serviceName;
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	
}
