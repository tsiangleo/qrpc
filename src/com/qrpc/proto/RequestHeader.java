package com.qrpc.proto;

import java.io.Serializable;

/**
 * 请求头
 * @author tsiangleo 2016年4月28日 下午6:48:57
 */
public class RequestHeader implements Serializable {
	private static final long serialVersionUID = 2455123674144931457L;
	
	private int rid; // 请求ID
	private int type; // 请求类型

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
