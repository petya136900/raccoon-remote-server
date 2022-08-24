package com.petya136900.raccoonvpn.rest;

import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.exceptions.ApiException;
import com.petya136900.raccoonvpn.tools.Response;

public interface HandlerInterface {
	public Response handle(Authorized auth) throws ApiException;
}
