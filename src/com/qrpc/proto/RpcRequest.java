package com.qrpc.proto;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRequest implements Serializable
{
	private static final long serialVersionUID = -5197585095067479371L;
	
	private String requestId;
	private String serviceInterface;
	private String methodName;
	private Class<?>[] parameterTypes;
	private Object[] args;
	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	
	public String getServiceInterface() {
		return serviceInterface;
	}
	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}
	@Override
	public String toString() {
		return "RpcRequest [requestId=" + requestId + ", serviceInterface="
				+ serviceInterface + ", methodName=" + methodName
				+ ", parameterTypes=" + Arrays.toString(parameterTypes)
				+ ", args=" + Arrays.toString(args) + "]";
	}
}	
