package com.github.tsiangleo.qrpc.proto;

import java.io.Serializable;
import java.util.List;

/**
 * 返回所有的服务名
 * @author tsiangleo 2016年4月28日 下午6:59:52
 */
public class GetAllServiceResponse implements Serializable {
	private static final long serialVersionUID = -920141627439684108L;
	private List<String> serviceList;	//服务名
	public List<String> getServiceList() {
		return serviceList;
	}
	public void setServiceList(List<String> serviceList) {
		this.serviceList = serviceList;
	}
}
