package com.petya136900.raccoonvpn;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.petya136900.raccoonvpn.forward.ConnectedDevicesStorage;
import com.petya136900.raccoonvpn.forward.AgentListener;
import com.petya136900.raccoonvpn.tools.Tools;
@Component
public class LifeCycle implements CommandLineRunner {
	static final Logger LOG = LoggerFactory.getLogger(LifeCycle.class);
	@Autowired
	private ConnectedDevicesStorage connectedDevicesStorage;
	@Autowired
	private AgentListener tcpForwarderListener;
	@Override
	public void run(String... arg0) throws Exception {
		System.out.println("#");
		System.out.println("#");
		System.out.println("# RaccoonVPN running at https://"+Tools.getLocalIP()+":"+Tools.getHttpsPort()+"/");
		if(RaccoonVPNServer.useHttp) {
			System.out.println("#");
			System.out.println("#\t\t\t\thttp://"+Tools.getLocalIP()+":"+RaccoonVPNServer.HTTP_PORT);
		}
		System.out.println("#");
		System.out.println("# Agents are accepted on port :"+RaccoonVPNServer.agentsPort);
		System.out.println("#");
	}

	@PreDestroy
	public void onExit() {
		LOG.info("Stopping all conditions.. unload devices");
	    tcpForwarderListener.stop();
	    connectedDevicesStorage.stopAllConditions();
	    connectedDevicesStorage.unloadAllDevice();
	}
}
