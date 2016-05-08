package com.qrpc.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.qrpc.exception.RpcException;


public class Serializer {
	public static <T> byte[] serialize(T object) {
		if(!( object instanceof Serializable)){
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION,"对象没有实现Serializable接口，目前仅支持JDK自带的序列化功能...");
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(bos);
			os.writeObject(object);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] b = bos.toByteArray();
		return b;
	}
	
	public static <T> T deserialize( byte[] byteBuf) {
		ByteArrayInputStream bin = new ByteArrayInputStream(byteBuf);
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(bin);
			T obj = null;
			try {
				obj = (T) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T deserialize( InputStream is) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(is);
			T obj = null;
			try {
				obj = (T) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
