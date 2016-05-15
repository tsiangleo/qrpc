package com.github.tsiangleo.qrpc.service;

import java.util.List;

public interface UserService {

	public List<UserInfo> getUserinfos();

	public void setUserinfos(List<UserInfo> userinfos);
	
	public UserInfo getUserInfo(int index);
}
