package com.qrpc.registry;


public class RegistryServerTest {

	public static void main(String[] args) {
		RegistryServer registryServer = new RegistryServer(7888);

		//RegistryServer服务器的一些配置
		//rpcServer.setXXX();
		registryServer.start();
	}

}
