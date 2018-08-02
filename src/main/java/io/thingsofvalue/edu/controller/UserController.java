package io.thingsofvalue.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;


import io.thingsofvalue.edu.domain.User;
import io.thingsofvalue.edu.service.UserService;

@RestController
public class UserController {
	//
	@Autowired
	UserService userService;
	
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	public String createUser(@RequestBody User user) throws Exception {
		return userService.create(user);
	}
	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public String retrieveUser(@PathVariable String userId) {
		return userService.retrieve(userId);
	}
	

}
