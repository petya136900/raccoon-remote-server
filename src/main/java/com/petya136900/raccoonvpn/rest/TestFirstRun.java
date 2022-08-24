package com.petya136900.raccoonvpn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.petya136900.raccoonvpn.services.UserService;

@Component
@Scope
public class TestFirstRun {
	
	@Autowired
	private UserService userService;
	
	public void testMethod() {
		System.out.println("Hello from test Metho!");
		System.out.println(userService);
	}
}
