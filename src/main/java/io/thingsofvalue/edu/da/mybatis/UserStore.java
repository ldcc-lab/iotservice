package io.thingsofvalue.edu.da.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.thingsofvalue.edu.da.mybatis.mapper.UserMapper;
import io.thingsofvalue.edu.domain.User;

@Repository
public class UserStore {
	//
	@Autowired
	UserMapper userMapper;
	
	public User create(User user) {
		userMapper.insert(user);
		return user;
	}
	
	public User retrieve(String userId) {
		return userMapper.select(userId);
	}

}
