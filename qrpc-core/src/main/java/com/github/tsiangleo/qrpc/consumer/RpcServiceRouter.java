package com.github.tsiangleo.qrpc.consumer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tsiangleo.qrpc.exception.RpcException;
import com.github.tsiangleo.qrpc.util.ZKConfig;

/**
 * rpc服务地址列表管理器
 * @author tsiangleo 2016年4月29日 下午1:21:44
 */
public class RpcServiceRouter {
	private  List<String> allService; //所有的服务名列表
	private  Map<String, List<InetSocketAddress>> serviceProviderMap;
	private  RpcConsumerZKSynchronizer rpcClientZKSynchronizer;
	private static RpcServiceRouter instance;

	public static RpcServiceRouter instance(String zNodeRootPath,String zkServerList){
		if(instance == null) {
			instance  = new RpcServiceRouter(zNodeRootPath,zkServerList);
		}
		return instance;
	}
	
	private RpcServiceRouter(String zNodeRootPath,String zkServerList){
		allService = new ArrayList<String>();
		serviceProviderMap = new ConcurrentHashMap<String, List<InetSocketAddress>>();
		rpcClientZKSynchronizer = new RpcConsumerZKSynchronizer(
				zNodeRootPath,zkServerList,allService,serviceProviderMap);
	}
	
	/**
	 * 负载均衡
	 * @param serviceInterface
	 * @return
	 */
	public  InetSocketAddress getAddressByServiceName(String serviceInterface) {
		List<InetSocketAddress> list = serviceProviderMap.get(serviceInterface);
		if(list == null || list.isEmpty()){
			rpcClientZKSynchronizer.getServiceProviderList(serviceInterface);
			list = serviceProviderMap.get(serviceInterface);
			if(list == null || list.isEmpty())
				throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,"Current no serviceProvider for service:"+serviceInterface);
			else {
				rpcClientZKSynchronizer.registerConsumer(serviceInterface);
				return list.get(new Random().nextInt(list.size()));
			}
		}else {
			return list.get(new Random().nextInt(list.size()));
		}
	}

}
