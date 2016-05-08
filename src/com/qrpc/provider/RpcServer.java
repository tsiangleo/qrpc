package com.qrpc.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.consumer.RpcInvokeHook;
import com.qrpc.util.ZKConfig;

public class RpcServer {
	private static final Logger logger = LoggerFactory
			.getLogger(RpcServer.class);

	private Map<String, Object> instanceMap = new HashMap<String, Object>();
	// 服务接口名 -> 服务对象
	private Map<String, Class> classMap = new HashMap<String, Class>();
	// 服务接口名 -> 服务Class对象

	private int port;
	private RpcInvokeHook rpcInvokeHook;

	private RpcServerRequestHandler rpcServerRequestHandler;

	private RpcProviderZKSynchronizer rpcProviderZKSynchronizer;

	private boolean connetToRegistryCenter;

	/**
	 * 
	 * @param port 向外提供的服务端口号
	 * @param connectToZk 是否将提供的服务注册到服务注册查找中心。
	 */
	public RpcServer(int port,boolean connectToZk) {
		this.port = port;
		this.connetToRegistryCenter = connectToZk;
	}


	public void addServiceInterfaceAndProvider(Class serviceInterface,
			Object obj) {
		instanceMap.put(serviceInterface.getCanonicalName(), obj);
		classMap.put(serviceInterface.getCanonicalName(), serviceInterface);

		if (connetToRegistryCenter) {
			if (rpcProviderZKSynchronizer == null) {
				rpcProviderZKSynchronizer = new RpcProviderZKSynchronizer(
						ZKConfig.zNodeRootPath, ZKConfig.zkServerList);
			}
			rpcProviderZKSynchronizer.registerService(
					serviceInterface.getCanonicalName(), port);
		}
	}

	public void setHook(RpcInvokeHook hook) {
		rpcInvokeHook = hook;

	}

	public void start() {
		logger.info(
				"RpcServer started and listen on port {},provide services {}",
				port, classMap.entrySet());

		rpcServerRequestHandler = new RpcServerRequestNIOHandler(instanceMap,
				classMap, port, rpcInvokeHook);
		rpcServerRequestHandler.handle();
	}

	public void stop() {
		rpcServerRequestHandler.stop();
	}
}
