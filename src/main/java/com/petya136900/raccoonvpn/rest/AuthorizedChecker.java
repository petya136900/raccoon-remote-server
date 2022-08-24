package com.petya136900.raccoonvpn.rest;

import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.exceptions.ApiException;

public interface AuthorizedChecker {
	public Authorized check(String token, String ip) throws ApiException;
}
