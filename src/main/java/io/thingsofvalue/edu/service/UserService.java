package io.thingsofvalue.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.thingsofvalue.edu.da.mybatis.UserStore;
import io.thingsofvalue.edu.domain.User;

@Service
public class UserService {
	//
	@Autowired
	UserStore userStore;
	
	public String create(User user) {
		return userStore.create(user).toJson();
	}
	
	public String retrieve(String userId) {
		return userStore.retrieve(userId).toJson();
	}

}
