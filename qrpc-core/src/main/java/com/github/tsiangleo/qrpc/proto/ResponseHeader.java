package com.github.tsiangleo.qrpc.proto;

import java.io.Serializable;

/**
 * 响应头
 * @author tsiangleo 2016年4月28日 下午6:48:45
 */
public class ResponseHeader implements Serializable {
	private static final long serialVersionUID = -6394485711415716632L;
	private int rid; // 对应于请求id
	private int err; // 错误码
	public int getRid() {
		return rid;
	}
	public void setRid(int rid) {
		this.rid = rid;
	}
	public int getErr() {
		return err;
	}
	public void setErr(int err) {
		this.err = err;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}