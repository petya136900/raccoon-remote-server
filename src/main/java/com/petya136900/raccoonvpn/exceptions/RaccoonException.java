package com.petya136900.raccoonvpn.exceptions;
abstract public class RaccoonException extends Exception {
	private static final long serialVersionUID = 7066382642991690771L;
	private final String message = ""; 
	@Override
	public String getMessage() {
		return message;
	}
	@Override
	public String getLocalizedMessage() {
		return message;
	}
}
