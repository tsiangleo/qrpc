package com.qrpc.consumer;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.exception.RpcException;

/**
 * 负责和zk服务器同步服务信息
 * 
 * @author tsiangleo 2016年5月4日 下午12:27:40
 */
public class RpcConsumerZKSynchronizer{
	private static final Logger logger = LoggerFactory.getLogger(RpcConsumerZKSynchronizer.class);
	
	private List<String> allService; //所有的服务名列表
	private Map<String, List<InetSocketAddress>> serviceProviderMap;
	private ZkClient zkClient;
	private String zNodeRootPath;	//	"/test"	
	
	/**
	 * 
	 * @param zNodePath
	 * @param zkServerList 格式如下："192.168.1.103:8080,192.168.2.202:9090"
	 */
	public RpcConsumerZKSynchronizer(String zNodePath, String zkServerList,
			List<String> allService,Map<String, List<InetSocketAddress>> serviceProviderMap) {
		this.zNodeRootPath = zNodePath;
		zkClient = new ZkClient(zkServerList);
		this.allService = allService;
		this.serviceProviderMap = serviceProviderMap;
	}
	
	/**
	 * 获取所有服务名至allService中。
	 */
	public void getServiceList() {
		
		if(!zkClient.exists(zNodeRootPath)){
			throw new RuntimeException("Znode "+zNodeRootPath +" is not exists");
		}
		
		//获取服务名
		List<String> list = zkClient.getChildren(zNodeRootPath);
		if(list != null && !list.isEmpty())
			allService = list;

		//添加监听器。若服务提供者列表信息有变化，则回调下面的方法。
		zkClient.subscribeChildChanges(zNodeRootPath, new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				if(childrens != null && !childrens.isEmpty())
					allService = childrens;
			}});
	}

	
	/**
	 * 获取服务提供者信息至serviceProviderMap中。
	 * @param serviceName
	 */
	public void getServiceProviderList(final String serviceName) {
		
		String providerPath = zNodeRootPath+"/"+serviceName+"/provider";
		if(!zkClient.exists(providerPath)){
			throw new RpcException("Znode "+providerPath +" is not exists");
		}
		
		//获取 服务提供者的ip和端口信息
		List<String> serviceList = zkClient.getChildren(providerPath);
		updateProviderList(serviceName, serviceList);

		//添加监听器。若服务提供者列表信息有变化，则回调下面的方法。
		zkClient.subscribeChildChanges(providerPath, new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				updateProviderList(serviceName, childrens);
			}});
	}

	/**
	 * 更新serviceProviderMap中的服务提供者的信息
	 * @param serviceName 服务名
	 * @param serviceList 列表中的每个元素 应该是如下格式："192.168.1.1:9090"，即"ip地址:端口号"。
	 */
	private void updateProviderList(String serviceName, List<String> serviceList) {
		if(serviceList != null && !serviceList.isEmpty()){
			List<InetSocketAddress> socketAddrList = new ArrayList<InetSocketAddress>();
			for(String ipAndport:serviceList){
				String[] strings = ipAndport.split(":");
				if(strings.length == 2){
					socketAddrList.add(new InetSocketAddress(strings[0], Integer.parseInt(strings[1])));
				}
				else{
					logger.warn("Bad format serviceProviderInfo: {}",ipAndport);
				}
			}
			if(!socketAddrList.isEmpty()){
				if(serviceProviderMap == null || serviceProviderMap.get(serviceName) == null){
					logger.debug("get serviceProvider of {} with value {}",serviceName,socketAddrList);
				}
				else{
					logger.debug("update serviceProvider of {} from {} to {}",
							serviceProviderMap.get(serviceName),socketAddrList);
				}
				serviceProviderMap.put(serviceName, socketAddrList);
				
			}
		}
	}

	/**
	 * 向zk注册消费信息
	 * @param serviceName
	 */
	public void registerConsumer(String serviceName) {

		String path = zNodeRootPath+"/"+serviceName+"/consumer";
		
		if(!path.startsWith("/")){
			throw new RpcException("Bad Path: '"+path+"',Path must start with / character.");
		}
		if(path.endsWith("/")){
			throw new RpcException("Bad Path: '"+path+"',Path must not end with / character.");
		}
		
		if(!zkClient.exists(path)){
			//依次创建path路径上的各个Znode
			String[] nodePath = path.split("/"); //path是如下格式的字符串"/abc/def"
			String p = "";
			for(int i = 1;i<nodePath.length;i++){
				p = p+"/"+nodePath[i];
				if(!zkClient.exists(p)){
					zkClient.createPersistent(p);
					logger.debug("creating Znode {} ",p);
				}
			}
		}
		
		String childPath = path+"/"+getIpAndJVMStrings();
		zkClient.createEphemeral(childPath);
		logger.debug("creating Znode {} ",childPath);
	}
	
	/**
	 * @param port
	 * @return
	 */
	private String getIpAndJVMStrings(){
		String jvmidString = ManagementFactory.getRuntimeMXBean().getName();	//6616@michael-PC
		String ipString = null;
		try {
			ipString = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ipString+":"+jvmidString;
	}
	
	private String getIp(){

		String ipString = null;
		try {
			ipString = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ipString;
	}
}
