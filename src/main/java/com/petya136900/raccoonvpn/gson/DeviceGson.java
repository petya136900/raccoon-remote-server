package com.petya136900.raccoonvpn.gson;

import com.petya136900.raccoonvpn.entitys.Device;

public class DeviceGson {

	private Long id;
	private String name;
	private boolean local = false;
	private boolean connected = false;
	private String ip;
	
	public DeviceGson(Device device) {
		this.id=device.getId();
		this.name=device.getName();
		if(device.isLocal())
			setLocal(true);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
