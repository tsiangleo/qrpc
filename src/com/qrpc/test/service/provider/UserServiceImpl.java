package com.qrpc.test.service.provider;

import java.util.ArrayList;
import java.util.List;

import com.qrpc.test.service.UserInfo;
import com.qrpc.test.service.UserService;

public class UserServiceImpl implements UserService {
	private List<UserInfo> userinfos = new ArrayList<UserInfo>();

	public UserServiceImpl(){
		userinfos.add(new UserInfo("liuq2",12));
		userinfos.add(new UserInfo("liuq3",13));
		userinfos.add(new UserInfo("liuq4",14));
		userinfos.add(new UserInfo("liuq5",15));
	}
	
	public List<UserInfo> getUserinfos() {
		return userinfos;
	}

	public void setUserinfos(List<UserInfo> userinfos) {
		this.userinfos = userinfos;
	}
	
	public UserInfo getUserInfo(int index){
		if(userinfos == null || userinfos.isEmpty())
			throw new IllegalStateException("userinfoList is empty");
		if(index >= userinfos.size() || index < 0)
			throw new IllegalArgumentException("out of range");
		
		return userinfos.get(index);
	}

}
