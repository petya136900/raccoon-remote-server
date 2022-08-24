package com.petya136900.raccoonvpn.exceptions;
public class SocketWasRejectedException extends RaccoonException {
	private static final long serialVersionUID = 4698213588346872900L;
	@SuppressWarnings("unused")
	private final String message = "The agent was unable to connect to the target";
}
