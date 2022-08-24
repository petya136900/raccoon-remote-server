package com.petya136900.raccoonvpn.forward;
import java.net.Socket;
import com.petya136900.raccoonvpn.entitys.Device;
public class ConnectedDevice {
	private Long id;
	private String deviceId;
	private String name;
	private boolean local;
	private String ip;
	private String pubIp;
	private Long userId;
	private Socket deviceSocket;
	public ConnectedDevice(Device device, String localIp) {
		this.userId=device.getUser().getId();
		this.id=device.getId();
		this.deviceId=device.getDeviceId();
		this.name=device.getName();
		this.local=device.isLocal();
		this.ip=localIp;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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
	public String getIp() {
		return ip;
	}
	public void setIp(String localIp) {
		this.ip = localIp;
	}
	public String getPubIp() {
		return pubIp;
	}
	public void setPubIp(String pubIp) {
		this.pubIp = pubIp;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Socket getDeviceSocket() {
		return deviceSocket;
	}
	public void setDeviceSocket(Socket deviceSocket) {
		this.deviceSocket = deviceSocket;
	}
}
