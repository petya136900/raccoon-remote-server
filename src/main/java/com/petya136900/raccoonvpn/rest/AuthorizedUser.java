package com.petya136900.raccoonvpn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.exceptions.ApiException;
import com.petya136900.raccoonvpn.exceptions.AuthorizedNotFoundException;
import com.petya136900.raccoonvpn.rest.v1.codes.ResponseCodes;
import com.petya136900.raccoonvpn.services.AuthorizedService;
import com.petya136900.raccoonvpn.tools.Tools;

@Component
@Scope
public class AuthorizedUser implements AuthorizedChecker {
	
	@Autowired
	private AuthorizedService authorizedService;
	
	@Override
	public Authorized check(String token, String ip) throws ApiException {
		Authorized auth;
		try {
			auth =  authorizedService.getAuthorizedByToken(token);
		} catch (AuthorizedNotFoundException e) {
			throw new ApiException(ResponseCodes.TOKEN_NOT_FOUND);
		}
		if(auth.isAgent()) 
			throw new ApiException(ResponseCodes.AGENT_TOKEN_NOT_ALLOWED);
		if(Tools.checkExpire(auth.getExpires_in())) {
			throw new ApiException(ResponseCodes.TOKEN_EXPIRED);
		} else {
			if(ip.equals(auth.getIp())) {
				return auth;
			}
			throw new ApiException(ResponseCodes.IP_CHANGED);
		}
	}

}
