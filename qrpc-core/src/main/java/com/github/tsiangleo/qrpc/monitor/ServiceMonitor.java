package com.github.tsiangleo.qrpc.monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import com.github.tsiangleo.qrpc.util.ZKConfig;

public class ServiceMonitor {

	//服务名->服务地址之间的映射
	private  Map<String, List<String>> serviceProviderMap = new ConcurrentHashMap<String, List<String>>();
	//哪些服务被哪些客户端调用了。
	private  Map<String, List<String>> serviceConsumerMap = new ConcurrentHashMap<String, List<String>>();
	private  List<String> serviceList; //所有的服务列表
	private  ZkClient zkClient;
	private  String zkServerList;
	private  String zNodeRootPath;
	
	public ServiceMonitor(String zkServerList,String zNodeRootPath)
	{
		this.zkServerList = zkServerList;
		this.zNodeRootPath = zNodeRootPath;
		initServiceList();
		for(String service: serviceList){
			initConsumerList(service);
			initProviderList(service);
		}
	}
	
	private  void initServiceList(){
		if(zkClient == null){
			zkClient = new ZkClient(zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+zNodeRootPath);
		}
		
		List<String> list = zkClient.getChildren(zNodeRootPath);
		if(list != null && !list.isEmpty()){
			serviceList = list;
		}
		
		zkClient.subscribeChildChanges(zNodeRootPath, new IZkChildListener(){
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceList = zkClient.getChildren(zNodeRootPath);
				onServiceListChange(parentPath,childrens);
			}

			});
	}
	
	private  void initConsumerList(final String serviceName) {
		if(zkClient == null){
			zkClient = new ZkClient(zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+zNodeRootPath);
		}
		
		String consumerPath = zNodeRootPath+"/"+serviceName+"/consumer";
		if(!zkClient.exists(consumerPath)){
			return;
		}
		
		List<String> serviceList = zkClient.getChildren(consumerPath);
		if(serviceList != null && !serviceList.isEmpty()){
			serviceConsumerMap.put(serviceName, serviceList);
		}
		
		zkClient.subscribeChildChanges(consumerPath, new IZkChildListener(){
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceConsumerMap.put(serviceName, childrens);
				onConsumerListChange(serviceName,parentPath,childrens);
			}});
		
	}
	
	private  void  initProviderList(final String serviceName) {
		if(zkClient == null){
			zkClient = new ZkClient(zkServerList);
			System.out.println("ServiceMonitor zkclient going to connect to "+zNodeRootPath);
		}
		
		String providerPath = zNodeRootPath+"/"+serviceName+"/provider";
		if(!zkClient.exists(providerPath)){
			return;
		}
		
		List<String> serviceList = zkClient.getChildren(providerPath);
		if(serviceList != null && !serviceList.isEmpty()){
			serviceProviderMap.put(serviceName, serviceList);
		}
		
		zkClient.subscribeChildChanges(providerPath, new IZkChildListener(){
			public void handleChildChange(String parentPath, List<String> childrens)
					throws Exception {
				serviceProviderMap.put(serviceName, childrens);
				onProviderListChange(serviceName,parentPath,childrens);
			}});
		
	}
	
	public  void onServiceListChange(String parentPath,List<String> childrens) {
		System.out.println("ServiceList has Changed to "+serviceList);
	}
	
	public  void onConsumerListChange(String serviceName,String parentPath,List<String> childrens) {
		System.out.println("the consumer of "+parentPath+" has Changed to "+serviceConsumerMap.get(serviceName));
	}
	
	public  void onProviderListChange(String serviceName,String parentPath,List<String> childrens) {
		System.out.println("the provider of "+parentPath+" has Changed to "+serviceProviderMap.get(serviceName));
		
	}
	
	public  List<String> getServiceList() {
		return serviceList;
	}
	
	public  List<String> getConsumerList(String serviceName) {
		return serviceConsumerMap.get(serviceName);
	}
	
	public  List<String> getProviderList(String serviceName) {
		return serviceProviderMap.get(serviceName);
	}
	
	public  void  sumerize() {
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
