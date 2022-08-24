package com.petya136900.raccoonvpn;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.entitys.Setting;
import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.exceptions.DeviceAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.UserAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.UserNotSpecifiedException;
import com.petya136900.raccoonvpn.forward.ConnectedDevice;
import com.petya136900.raccoonvpn.forward.ConnectedDevicesStorage;
import com.petya136900.raccoonvpn.forward.LoadedCondition;
import com.petya136900.raccoonvpn.forward.AgentListener;
import com.petya136900.raccoonvpn.longpolling.LongPollEvent;
import com.petya136900.raccoonvpn.longpolling.LongPollStorage;
import com.petya136900.raccoonvpn.services.ConditionService;
import com.petya136900.raccoonvpn.services.DeviceService;
import com.petya136900.raccoonvpn.services.SettingService;
import com.petya136900.raccoonvpn.services.UserService;
import com.petya136900.raccoonvpn.tools.Tools;

@Component
public class StartupCheck {	
    private static final Logger LOG 
      = Logger.getLogger(StartupCheck.class.getName());
    @Autowired
    private SettingService settingService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ConditionService conditionService;
    @Autowired
	private LongPollStorage longPollStorage;
    @Autowired
    private ConnectedDevicesStorage connectedDevicesStorage;
    @Autowired
    private AgentListener tcpForwarderListener;
    @Autowired
    ResourcePatternResolver resourceResolver;
    @PostConstruct
    public void init() {
    	LOG.fine("Checking for the first launch");
    	Setting firstRun = settingService.getSettingByName("first-run");
    	if(firstRun==null||firstRun.getValue()=="true") { 
    		LOG.info("First launch detected");
    		LOG.info("Adding default admin user");
    		try {
    			User admin = new User();
    			admin.setLogin("admin");
    			admin.setPassword("password");
    			admin.setAdmin(true);
				admin = userService.addNewUser(admin);
				LOG.info("User 'admin' added");
			} catch (UserAlreadyExistException e) {
				LOG.info("User 'admin' already exist");
			}
    		LOG.info("Adding local device as agent");
    		Device device = Device.generateLocalDevice();
    		device.setLocal(true);
    		try {
				Tools.createFile("deviceids", device.getDeviceId());
			} catch (IOException e) {
				LOG.warning("Can't write deviceids to disk: "+e.getLocalizedMessage());
			}
    		device.setUser(userService.listAdmins().get(0));
    		try {
				deviceService.addNewDevice(device);
			} catch (DeviceAlreadyExistException e) {
				LOG.warning("Device with same ID exist");
			} catch (UserNotSpecifiedException e) {
				LOG.warning("Error, admin '"+device.getUser().getLogin()+"' not exist");
			}
    		LOG.info("Device["+device.getDeviceId()+"] created");
    		settingService.updateSettingByName("first-run", "false");
    		settingService.allowRegister(true);
    		settingService.allowChangeUserByUser(true);
    		settingService.allowCreateRulesByUser(true);
    	}
    	deviceService.getLocalDevices().forEach(x->{
    		ConnectedDevice connectedLocalDevice = new ConnectedDevice(x,Tools.getLocalIP());
    		connectedDevicesStorage.addDevice(connectedLocalDevice);
    	});
    	Tools.updateVars();
    	LOG.info("Vars updated");
    	String tcpListenerPortString = null;
    	if(!RaccoonVPNServer.tcpIgnoreDb) {
    		tcpListenerPortString = settingService.getAgentsPort();
    	}
    	Integer tcpListenerPort; 
    	try {
    		tcpListenerPort = Integer.parseInt(tcpListenerPortString);
    	} catch (Exception e) {
    		tcpListenerPort = RaccoonVPNServer.TCP_LISTENER_PORT;
		}
    	tcpForwarderListener.startAt(tcpListenerPort);
    	List<Condition> conds = conditionService.list();
    	LOG.info("Loading conditions");
    	for(Condition cond : conds) {
    		try {
		    	LoadedCondition loadedCondition = connectedDevicesStorage.getCondition(cond.getId());
				if(cond.getAutorun()) {
					ConnectedDevice conDev = connectedDevicesStorage.getDevice(cond.getDevice().getId());
					if(conDev!=null) {
						loadedCondition = connectedDevicesStorage.loadCondition(new LoadedCondition(cond));
						loadedCondition.setStatus(LoadedCondition.STATUS_STARTS_UP);
						longPollStorage.addEvent("deviceconditions-"+cond.getDevice().getId(), LongPollEvent.CODE_CONDITION_STARTING, "condition starts up");
						connectedDevicesStorage.startCondition(loadedCondition);
					}
				}
    		} catch (Exception e) {
    			LOG.warning("Error while loading condition with ID#"+((cond!=null)?cond.getId():"condNull"));
			}
    	}
    }
}