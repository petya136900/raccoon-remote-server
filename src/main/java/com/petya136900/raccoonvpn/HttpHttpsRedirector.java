package com.petya136900.raccoonvpn;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class HttpHttpsRedirector {
	private static final String HTTP = "http";
	private static final String USER_CONSTRAINT = "CONFIDENTIAL";

	@ConditionalOnProperty(value="raccoonvpn.http.https.redirect") 
	@Bean
	public ServletWebServerFactory servletContainer() {
	    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
	        @Override
	        protected void postProcessContext(Context context) {
	            SecurityConstraint securityConstraint = new SecurityConstraint();
	            securityConstraint.setUserConstraint(USER_CONSTRAINT);
	            SecurityCollection collection = new SecurityCollection();
	            collection.addPattern("/*");
	            securityConstraint.addCollection(collection);
	            context.addConstraint(securityConstraint);
	        }
	    };
	    tomcat.addAdditionalTomcatConnectors(redirectConnector());
	    return tomcat;
	}
	private Connector redirectConnector() {
	    Connector connector = new Connector(
	            TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
	    connector.setScheme(HTTP);
	    connector.setPort(RaccoonVPNServer.HTTP_PORT);
	    connector.setSecure(false);
	    connector.setRedirectPort(RaccoonVPNServer.HTTPS_PORT);
	    return connector;
	}
}