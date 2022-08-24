package com.petya136900.raccoonvpn.forward;

import java.net.Socket;

public class SocketWrapper {

	private Socket socket;
	
	public SocketWrapper(Socket socket) {
		this.setSocket(socket);
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
