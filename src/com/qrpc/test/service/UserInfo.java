package com.qrpc.test.service;

import java.io.Serializable;

public class UserInfo implements Serializable{
	private static final long serialVersionUID = 6232728326776296438L;
	private String username;
	private int age;
	
	public UserInfo(String username, int age) {
		super();
		this.username = username;
		this.age = age;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@Override
	public String toString() {
		return "UserInfo [username=" + username + ", age=" + age + "]";
	}
	
}
