package com.github.tsiangleo.qrpc.consumer;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RpcConsumerProxy {
	private static final Logger logger = LoggerFactory.getLogger(RpcConsumerProxy.class);
	
	//这里设置为非static
	private RpcInvokeHook rpcInvokeHook;
	private InetSocketAddress rpcServerAddress;
	private Class<?> serviceInterface;  
	private String group;
	private String version;
	private boolean async;
	private long timeoutMills;	//方法调用的超时时间（单位：毫秒）
	

	private RpcCallback rpcCallback;
	
	private static String zkServerList;
	private static String zNodeRootPath;
	
	
	/**
	 * 设置服务接口
	 * @param serviceInterface
	 * @return
	 */
	public RpcConsumerProxy serviceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
		return this;
	}

	/**
	 * 设置该服务接口的回调对象。注意：一旦设置到该服务接口，那么调用该服务的所有方法后都会调用这个回调对象。
	 * 也就是说该回调对象对该服务的所有方法有效。如果要单独为每个方法调用设置回调对象，则
	 * 应该在每次方法调用前，设置该次调用的callBack,如下：RpcCallContext.getContext().setCallback(rpcCallback);
	 * 
	 * @param rpcCallback
	 * @return
	 */
	public RpcConsumerProxy callBack(RpcCallback rpcCallback) {
		this.rpcCallback = rpcCallback;
		return this;
	}
	
	/**
	 * 设置钩子，从设置以后对该服务的每个方法调用前后都会调用钩子方法。
	 * @param rpcInvokeHook
	 * @return
	 */
	public RpcConsumerProxy hook(RpcInvokeHook rpcInvokeHook) {
		this.rpcInvokeHook = rpcInvokeHook;
		return this;
	}
	
	
	/**
	 * 设置钩子，从设置以后对该服务的每个方法调用前后都会调用钩子方法。
	 * @param rpcInvokeHook
	 * @return
	 */
	
	/**
	 * 
	 * @param zkServerList - 格式如下: "192.168.1.106:2181";
	 * @param zNodeRootPath - 格式如下:"/qrpc-test01"
	 * @return
	 */
	public RpcConsumerProxy zk(String zkServerList,String zNodeRootPath) {
		this.zkServerList = zkServerList;
		this.zNodeRootPath = zNodeRootPath;
		return this;
	}
	
	/**
	 * 设置服务的版本号
	 * @param version
	 * @return
	 */
	public RpcConsumerProxy version(String version) {
		this.version  = version;
		return this;
	}

	/**
	 * 设置服务的组号
	 * @param group
	 * @return
	 */
	public RpcConsumerProxy group(String group) {
		this.group = group;
		return this;
	}
	
	/**
	 * 设置同步调用模式下的超时时间，单位（毫秒）；对异步调用无效。
	 * @param timeoutMills
	 * @return
	 */
	public RpcConsumerProxy timeout(long timeoutMills) {
		this.timeoutMills = timeoutMills;
		return this;
	}
	
	
	/**
	 * 设置是否为异步模式。
	 * @param async
	 * @return
	 */
	public RpcConsumerProxy async(boolean async) {
		this.async = async;
		return this;
	}
	
	/**
	 * 设置远程RpcServer的主机地址和端口号
	 * @param host
	 * @param port
	 * @return
	 */
	public RpcConsumerProxy bind(String host,int port) {
		this.rpcServerAddress = new InetSocketAddress(host,port);
		return this;
	}
	
	/**
	 * 创建服务接口的一个代理
	 * @return
	 */
	public Object create() {
		//在返回代理之前，要寻址路由。
		if(rpcServerAddress == null){	//用户没设置rpc服务器的地址，则去服务注册查找中心查找。
			 rpcServerAddress = RpcServiceRouter.instance(zNodeRootPath,zkServerList).
					 getAddressByServiceName(serviceInterface.getCanonicalName());
		}
		
		//返回代理对象
		return Proxy.newProxyInstance(serviceInterface.getClassLoader(), 
				new Class<?>[]{serviceInterface},
				new RpcInvocationHandler(rpcServerAddress,serviceInterface,
						group,version,async,timeoutMills,rpcCallback,rpcInvokeHook));  
	}







}
