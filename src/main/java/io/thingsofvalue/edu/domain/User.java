package io.thingsofvalue.edu.domain;

import io.thingsofvalue.edu.util.JsonUtil;

public class User {
	//
	private String userId;
	private String name;
	
	public String toJson() {
		return JsonUtil.toJson(this);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
