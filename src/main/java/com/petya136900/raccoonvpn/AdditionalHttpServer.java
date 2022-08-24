package com.petya136900.raccoonvpn;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AdditionalHttpServer {
  @ConditionalOnProperty(value="raccoonvpn.http.enable") 
  @Bean
  public ServletWebServerFactory servletContainer(@Value("${server.http.port}") int httpPort) {
      Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
      connector.setPort(httpPort);
      TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
      tomcat.addAdditionalTomcatConnectors(connector);
      return tomcat;
  }
}