package com.petya136900.raccoonvpn.exceptions;
public class DeviceAlreadyConnectedException extends RaccoonException {
	private static final long serialVersionUID = 8642971073734418744L;
	@SuppressWarnings("unused")
	private final String message = "This device already connected to server";
}
