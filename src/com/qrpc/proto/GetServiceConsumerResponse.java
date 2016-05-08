package com.qrpc.proto;

import java.io.Serializable;
import java.util.List;

/**
 * 返回某个服务的所有消费者的信息
 * @author tsiangleo 2016年4月28日 下午6:59:52
 */
public class GetServiceConsumerResponse implements Serializable {
	
	private static final long serialVersionUID = 8822866397241884021L;
	private List<String> consumerList;	//服务提供者信息
	public List<String> getConsumerList() {
		return consumerList;
	}
	public void setConsumerList(List<String> consumerList) {
		this.consumerList = consumerList;
	}
	
	
}
