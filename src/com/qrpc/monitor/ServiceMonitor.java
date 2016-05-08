package com.qrpc.monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import com.qrpc.util.ZKConfig;

public class ServiceMonitor {

	//服务名->服务地址之间的映射
	private static Map<String, List<String>> serviceProviderMap = new ConcurrentHashMap<String, List<String>>();
	//哪些服务被哪些客户端调用了。
	private static Map<String, List<String>> serviceConsumerMap = new ConcurrentHashMap<String, List<String>>();
	private static List<String> serviceList; //所有的服务列表
	private static ZkClient zkClient;


	static{
		initServiceList();
		for(String service: serviceList){
			initConsumerList(service);
			initProviderList(service);
		}
	}
	
	private static void initServiceList(){
		if(zkClient == null){
			zkClient = new ZkClient(ZKConfig.zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+ZKConfig.zNodeRootPath);
		}
		
		List<String> list = zkClient.getChildren(ZKConfig.zNodeRootPath);
		if(list != null && !list.isEmpty()){
			serviceList = list;
		}
		
		zkClient.subscribeChildChanges(ZKConfig.zNodeRootPath, new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceList = zkClient.getChildren(ZKConfig.zNodeRootPath);
				onServiceListChange(parentPath,childrens);
			}

			});
	}
	
	private static void initConsumerList(final String serviceName) {
		if(zkClient == null){
			zkClient = new ZkClient(ZKConfig.zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+ZKConfig.zNodeRootPath);
		}
		
		String consumerPath = ZKConfig.zNodeRootPath+"/"+serviceName+"/consumer";
		if(!zkClient.exists(consumerPath)){
			return;
		}
		
		List<String> serviceList = zkClient.getChildren(consumerPath);
		if(serviceList != null && !serviceList.isEmpty()){
			serviceConsumerMap.put(serviceName, serviceList);
		}
		
		zkClient.subscribeChildChanges(consumerPath, new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceConsumerMap.put(serviceName, childrens);
				onConsumerListChange(serviceName,parentPath,childrens);
			}});
		
	}
	
	private static void  initProviderList(final String serviceName) {
		if(zkClient == null){
			zkClient = new ZkClient(ZKConfig.zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+ZKConfig.zNodeRootPath);
		}
		
		String providerPath = ZKConfig.zNodeRootPath+"/"+serviceName+"/provider";
		if(!zkClient.exists(providerPath)){
			return;
		}
		
		List<String> serviceList = zkClient.getChildren(providerPath);
		if(serviceList != null && !serviceList.isEmpty()){
			serviceProviderMap.put(serviceName, serviceList);
		}
		
		zkClient.subscribeChildChanges(providerPath, new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceProviderMap.put(serviceName, childrens);
				onProviderListChange(serviceName,parentPath,childrens);
			}});
		
	}
	
	public static void onServiceListChange(String parentPath,List<String> childrens) {
		System.out.println("ServiceList has Changed to "+serviceList);
	}
	
	public static void onConsumerListChange(String serviceName,String parentPath,List<String> childrens) {
		System.out.println("the consumer of "+parentPath+" has Changed to "+serviceConsumerMap.get(serviceName));
	}
	
	public static void onProviderListChange(String serviceName,String parentPath,List<String> childrens) {
		System.out.println("the provider of "+parentPath+" has Changed to "+serviceProviderMap.get(serviceName));
		
	}
	
	public static List<String> getServiceList() {
		return serviceList;
	}
	
	public static List<String> getConsumerList(String serviceName) {
		return serviceConsumerMap.get(serviceName);
	}
	
	public static List<String> getProviderList(String serviceName) {
		return serviceProviderMap.get(serviceName);
	}
	
	public static void  sumerize() {
		System.out.println("===================== sumerize start =======================");
		System.out.println("ServiceList are:"+serviceList);
		for(String service: serviceList){
			System.out.println("service:"+service);
			System.out.println("\t provider:"+getProviderList(service));
			System.out.println("\t consumer:"+getConsumerList(service));
		}
		System.out.println("===================== sumerize  end  =======================");
		System.out.println();
	}
}
