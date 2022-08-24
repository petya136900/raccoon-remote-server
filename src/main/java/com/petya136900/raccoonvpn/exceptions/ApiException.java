package com.petya136900.raccoonvpn.exceptions;
import com.petya136900.raccoonvpn.rest.v1.codes.ResponseCodes;
public class ApiException extends Exception {
	private static final long serialVersionUID = 1463912286184412579L;
	private ResponseCodes errorCode;
	public ApiException(ResponseCodes errorCode) {
		this.setErrorCode(errorCode);
	}
	public ResponseCodes getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(ResponseCodes errorCode) {
		this.errorCode = errorCode;
	}
}
