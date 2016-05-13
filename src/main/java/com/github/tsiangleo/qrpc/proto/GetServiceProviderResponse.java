package com.github.tsiangleo.qrpc.proto;

import java.io.Serializable;
import java.util.List;

/**
 * 返回某个服务的所有提供者的信息
 * @author tsiangleo 2016年4月28日 下午6:59:52
 */
public class GetServiceProviderResponse implements Serializable {
	
	private static final long serialVersionUID = -8102884504148982053L;
	private List<String> providerList;	//服务提供者信息
	public List<String> getProviderList() {
		return providerList;
	}
	public void setProviderList(List<String> providerList) {
		this.providerList = providerList;
	}
	
	
}
