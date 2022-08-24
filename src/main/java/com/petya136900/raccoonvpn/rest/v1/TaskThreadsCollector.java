package com.petya136900.raccoonvpn.rest.v1;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TaskThreadsCollector extends ConcurrentHashMap<String, TaskThread>{
	private static final long serialVersionUID = -859533898884334186L;	
}
