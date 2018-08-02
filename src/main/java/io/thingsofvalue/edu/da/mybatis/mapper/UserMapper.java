package io.thingsofvalue.edu.da.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;

import io.thingsofvalue.edu.domain.User;

@Mapper
public interface UserMapper {
	void insert(User user);
	User select(String userId);

}
