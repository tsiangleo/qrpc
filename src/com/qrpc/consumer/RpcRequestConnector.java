package com.qrpc.consumer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qrpc.proto.RpcRequest;
import com.qrpc.proto.RpcResponse;
import com.qrpc.serialization.Serializer;

//具体处理socket通信细节
public class RpcRequestConnector {
	private static final Logger logger = LoggerFactory.getLogger(RpcRequestConnector.class);
	private Socket socket;
	private InetSocketAddress remoteSocketAddress;
	
	public RpcRequestConnector(InetSocketAddress address) {
		this.remoteSocketAddress = address;
		this.socket = new Socket();	//此时并未连接
	}

	public void send(RpcRequest rpcRequest) throws IOException {
		socket.getOutputStream().write(Serializer.serialize(rpcRequest));
		socket.getOutputStream().flush();
	}

	public void receive(RpcResponse response) throws IOException {
		RpcResponse r =  Serializer.deserialize(socket.getInputStream());
		response.setResult(r.getResult());
		response.setRequestId(r.getRequestId());
		response.setError(r.getError());
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect(InetSocketAddress addr) throws IOException {
		logger.debug("Rpc client is going to connect to {}",addr);
		socket.connect(addr);
	}

	public boolean isConnected() {
		return socket.isConnected();
	}
	public SocketAddress getRemoteSocketAddress(){
		return remoteSocketAddress;
		
	}
	public SocketAddress getLocalSocketAddress(){
		return socket.getLocalSocketAddress();
	}
}
