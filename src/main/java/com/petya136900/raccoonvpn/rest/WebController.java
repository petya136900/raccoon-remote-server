package com.petya136900.raccoonvpn.rest;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.petya136900.raccoonvpn.tools.RegexpTools;
import com.petya136900.raccoonvpn.tools.Tools;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/**",method = RequestMethod.GET)
public class WebController {
	@RequestMapping(method = RequestMethod.GET, value="/scripts/raccoonvpn/vars.js")
	public ResponseEntity<byte[]> getVars() throws FileNotFoundException {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		headers.set("Content-Type", "text/javascript"); 
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(Tools.getVars(), headers, HttpStatus.OK);
		return responseEntity;	
	}
	@RequestMapping(method = RequestMethod.GET, value = "/**")
	public ResponseEntity<byte[]> getImageAsResponseEntity(HttpServletRequest request) {
		try {
		    HttpHeaders headers = new HttpHeaders();
			String requestURI = request.getRequestURI().replaceAll("/$", "/index.html").replaceAll("^/", "");
			try(InputStream in = Tools.getDefaultFile(requestURI)) {
				byte[] media = IOUtils.toByteArray(in);
			    //headers.setCacheControl(CacheControl.noCache().getHeaderValue());
			    headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
			    if(RegexpTools.checkRegexp("(.svg$)", requestURI)) {
			    	headers.set("Content-Type", "image/svg+xml");
			    } else if(RegexpTools.checkRegexp("(.js$)", requestURI)) {
			    	headers.set("Content-Type", "text/javascript");
			    } else if(RegexpTools.checkRegexp("(.html?$)", requestURI)) {
			    	headers.set("Content-Type", "text/html");
			    }
			    ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
			    return responseEntity;	
			}
		} catch (Exception e) {
			try {
				try(InputStream in = Tools.getDefaultFile("404.html")) {
					return new ResponseEntity<>(IOUtils.toByteArray(in),HttpStatus.NOT_FOUND);
				}
			} catch (Exception e2) {
				return new ResponseEntity<>("Not Found".getBytes(),HttpStatus.NOT_FOUND);
			}
		}
	}
}
