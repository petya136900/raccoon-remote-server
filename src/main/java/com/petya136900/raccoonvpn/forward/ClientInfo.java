package com.petya136900.raccoonvpn.forward;
import java.net.InetAddress;
public class ClientInfo {
	private String serverNameIndication;
	private InetAddress remoteAddress;
	public ClientInfo(String sni, InetAddress remoteAddress) {
		this.serverNameIndication=sni;
		this.remoteAddress=remoteAddress;
	}
	public String getServerNameIndication() {
		return serverNameIndication;
	}
	public void setServerNameIndication(String serverNameIndication) {
		this.serverNameIndication = serverNameIndication;
	}
	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
}
