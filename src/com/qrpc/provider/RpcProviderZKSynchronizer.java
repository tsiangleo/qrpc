package com.qrpc.provider;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.exception.RpcException;

/**
 * 负责和zk服务器同步服务信息
 */
public class RpcProviderZKSynchronizer{
	private static final Logger logger = LoggerFactory.getLogger(RpcProviderZKSynchronizer.class);
	private ZkClient zkClient;
	private String zNodeRootPath;	//	"/test"	
	
	/**
	 * 
	 * @param zNodePath
	 * @param zkServerList 格式如下："192.168.1.103:8080,192.168.2.202:9090"
	 */
	public RpcProviderZKSynchronizer(String zNodePath, String zkServerList) {
		this.zNodeRootPath = zNodePath;
		zkClient = new ZkClient(zkServerList);
	}

	/**
	 * 向zk注册服务
	 * @param serviceName 服务名
	 * @param port 提供服务的端口号
	 */
	public void registerService(String serviceName, int port) {
		String path = zNodeRootPath+"/"+serviceName+"/provider";
		
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
					logger.debug("creating Znode:{}",p);
				}
			}
		}
		
		String childPath = path+"/"+getIpAndPortStrings(port);
		zkClient.createEphemeral(childPath);
		logger.debug("creating Znode:{}",childPath);
	}
	
	/**
	 * 返回"主机ip:端口号"
	 * @param port
	 * @return
	 */
	private String getIpAndPortStrings(int port){
		//String jvmidString = ManagementFactory.getRuntimeMXBean().getName();	//6616@michael-PC
		String ipString = null;
		try {
			ipString = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ipString+":"+port;
	}
}
