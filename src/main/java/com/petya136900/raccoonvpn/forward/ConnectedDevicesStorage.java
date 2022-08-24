package com.petya136900.raccoonvpn.forward;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import com.petya136900.raccoonvpn.RaccoonVPNServer;
import com.petya136900.raccoonvpn.agent.Codes;
import com.petya136900.raccoonvpn.agent.Data;
import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.enums.ConditionType;
import com.petya136900.raccoonvpn.exceptions.ConditionAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.exceptions.SocketTimeoutException;
import com.petya136900.raccoonvpn.exceptions.SocketWasRejectedException;
import com.petya136900.raccoonvpn.longpolling.LongPollEvent;
import com.petya136900.raccoonvpn.longpolling.LongPollStorage;
import com.petya136900.raccoonvpn.services.AuthorizedService;
import com.petya136900.raccoonvpn.services.ConditionService;
import com.petya136900.raccoonvpn.services.DeviceService;
import com.petya136900.raccoonvpn.tools.RegexpTools;
import com.petya136900.raccoonvpn.tools.Tools;
@Service
public class ConnectedDevicesStorage {
	private ConcurrentHashMap<Long, ConnectedDevice> connectedDevices = new ConcurrentHashMap<>(); // devId, connectedDevice
	private ConcurrentHashMap<String, Long> tasksLinks = new ConcurrentHashMap<>(); // taskId, sockId
	private long sockCounter=0;
	private ConcurrentHashMap<Long, Socket> socketsStorage = new ConcurrentHashMap<>(); // sockId, socket
	private ConcurrentHashMap<Integer, Proxy> tcpForwarders = new ConcurrentHashMap<>(); // extPort, tcpProxy
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Long, LoadedCondition>> loadedConditionsForExtPort 
			= new ConcurrentHashMap<Integer, ConcurrentHashMap<Long,LoadedCondition>>(); // extPort, <condId, loadedCond>
	@Autowired
	private ConditionService conditionService;
	@Autowired
	private AuthorizedService authorizedService;
	@Autowired
	private LongPollStorage longPollStorage;
	@Autowired
	private DeviceService deviceService;
	@Autowired 
	private AgentListener tcpListener;
	// Adders
	public void addDevice(ConnectedDevice connectedDevice) {
		synchronized (connectedDevices) {
			connectedDevices.put(connectedDevice.getId(), connectedDevice);
			longPollStorage.addEvent("devices-"+connectedDevice.getUserId(), LongPollEvent.CODE_DEVICE_CONNECTED ,"device connected");
			longPollStorage.addEvent("deviceconditions-"+connectedDevice.getId(), LongPollEvent.CODE_DEVICE_CONNECTED ,"device connected");
			startConditions(connectedDevice);
		}
	}
	private void startConditions(ConnectedDevice connectedDevice) {
		List<Condition> conds = conditionService.getConditionsByDevice(connectedDevice.getId());
		for(Condition cond : conds) {
			if(cond.getAutorun()) {
				LoadedCondition loadedCondition = loadCondition(new LoadedCondition(cond));
				loadedCondition.setStatus(LoadedCondition.STATUS_STARTS_UP);
				longPollStorage.addEvent("deviceconditions-"+cond.getDevice().getId(), LongPollEvent.CODE_CONDITION_STARTING, "condition starts up");
				startCondition(loadedCondition);
			}
		}
	}
	public LoadedCondition loadCondition(LoadedCondition cond) {
		ConcurrentHashMap<Long, LoadedCondition> condsForExtPort = loadedConditionsForExtPort.get(cond.getExtPort());
		if(condsForExtPort==null) {
			condsForExtPort = new ConcurrentHashMap<Long, LoadedCondition>();
			loadedConditionsForExtPort.put(cond.getExtPort(), condsForExtPort);
		}
		condsForExtPort.put(cond.getId(), cond);
		if(cond.getAutorun()) {
			cond.setStatus(LoadedCondition.STATUS_STARTS_UP);
			startCondition(cond);
		}
		return cond;
	}
	public LoadedCondition addConditionAndLoad(LoadedCondition cond, Boolean update) throws ConditionAlreadyExistException, DeviceNotFoundException {
		synchronized (loadedConditionsForExtPort) {
			Device device = deviceService.getDeviceById(cond.getDevId());
			Condition condDB = null;
			if(!update) {
				condDB = conditionService.addNewCondition(new Condition(cond,device));
				cond.setId(condDB.getId());
				cond.setCondTypeEnum(condDB.getCondType());
			}
			ConnectedDevice conDev = connectedDevices.get(cond.getDevId());
			LoadedCondition loadedCond = null;
			if(conDev!=null)
				loadedCond = loadCondition(cond);
			if(!update) {
				longPollStorage.addEvent("deviceconditions-"+cond.getDevId(), LongPollEvent.CODE_CONDITION_ADDED, "condition added");
			}
			return loadedCond;
		}			
	}
	// Getters
	public boolean deviceConnected(Long devId) {
		ConnectedDevice device = getDevice(devId);
		if(device!=null)
			return true;
		return false;
	}
	public boolean isLocalDevice(Long devId) {
		ConnectedDevice device = getDevice(devId);
		if(device.isLocal())
			return true;
		return false;
	}
	public ConnectedDevice getDevice(Long id) {
		synchronized (connectedDevices) {
			return connectedDevices.get(id);
		}
	}
	public LoadedCondition getCondition(Long id) {
		LoadedCondition cond;
		Set<Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>>> entrys = loadedConditionsForExtPort.entrySet();
		for(Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>> entry : entrys) {
			cond = entry.getValue().get(id);
			if(cond!=null)
				return cond;
		}
		return null;
	}
	// Deleters
	public void stopAllConditions() {
		Set<Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>>> entrys = loadedConditionsForExtPort.entrySet();
		for(Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>> entry : entrys) {
			entry.getValue().forEach((k,v)->{
				stopCondition(v);
			});
		}
	}
	 
	public void removeCondition(Long id) {
		synchronized (loadedConditionsForExtPort) {
			Set<Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>>> entrys = loadedConditionsForExtPort.entrySet();
			for(Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>> entry : entrys) {
				LoadedCondition cond = entry.getValue().get(id);
				if(cond!=null) {
					int extPort = cond.getExtPort();
					entry.getValue().remove(id);
					ConcurrentHashMap<Long, LoadedCondition> loadedCondsForPort = loadedConditionsForExtPort.get(extPort);
					if(loadedCondsForPort!=null&&loadedCondsForPort.size()<1) {
						loadedConditionsForExtPort.remove(extPort);
						Proxy proxy = tcpForwarders.get(extPort);
						if(proxy!=null) {
							proxy.stop();
							tcpForwarders.remove(extPort);
						}
					}
					break;
				}
			}
		}
	}
	// Update
	public void updateCondition(Condition condition) throws ConditionAlreadyExistException, DeviceNotFoundException {
		removeCondition(condition.getId());
		if(condition.getAutorun()) {
			ConnectedDevice conDev = connectedDevices.get(condition.getDevice().getId());
			if(conDev!=null)
				addConditionAndLoad(new LoadedCondition(condition),true);
		}
	}
	public long getCountLoadedConditions() {
		long count = 0l;
		Set<Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>>> entrys = loadedConditionsForExtPort.entrySet();
		for(Entry<Integer, ConcurrentHashMap<Long, LoadedCondition>> entry : entrys) {
			for(@SuppressWarnings("unused") Entry<Long, LoadedCondition> idCond : entry.getValue().entrySet()) {
				count++;
			}
		}
		return count;
	}
	synchronized public void startCondition(LoadedCondition loadedCondition) {
		Proxy proxy = tcpForwarders.get(loadedCondition.getExtPort());
		if(proxy==null) {
			proxy = new Proxy(loadedCondition.getExtPort());
			tcpForwarders.put(loadedCondition.getExtPort(), proxy);
			proxy.setConditionsStorage(loadedConditionsForExtPort.get(loadedCondition.getExtPort()));
			proxy.setAuthorizedService(authorizedService);
			proxy.setDeviceStorage(this);
			proxy.setDebug(RaccoonVPNServer.RACCON_DEBUG);
		}
		try {
			if(checkConditionValidity(loadedCondition)) {
				if(checkConditionCollision(loadedCondition)) {
					loadedCondition.setStatus(LoadedCondition.STATUS_CONFLICT);
					longPollStorage.addEvent("deviceconditions-"+loadedCondition.getDevId(), LongPollEvent.CODE_CONDITION_CANT_START, "condition collision");
				} else {
					proxy.start();
					loadedCondition.setStatus(LoadedCondition.STATUS_ENABLED);
					longPollStorage.addEvent("deviceconditions-"+loadedCondition.getDevId(), LongPollEvent.CODE_CONDITION_STARTED, "condition enabled");
				}
			} else {
				loadedCondition.setStatus(LoadedCondition.STATUS_BAD_CONDITION);
				longPollStorage.addEvent("deviceconditions-"+loadedCondition.getDevId(), LongPollEvent.CODE_CONDITION_CANT_START, "invalid condition");
			}
		} catch (IOException e) {
			loadedCondition.setStatus(LoadedCondition.STATUS_PORT_BUSY_BY_ANOTHER_PROGRAMM);
			longPollStorage.addEvent("deviceconditions-"+loadedCondition.getDevId(), LongPollEvent.CODE_CONDITION_CANT_START, "port busy");
		}
	}
	private boolean checkConditionValidity(LoadedCondition loadedCondition) {
		ConditionType condTypeEnum = loadedCondition.getCondTypeEnum();
		if(condTypeEnum==null)
			return false;
		boolean res = false;
		switch(condTypeEnum) {
			case DEFAULT:
				res = true;
				break;
			case IP:		
				res = RegexpTools.checkRegexp(RegexpTools.IP_PATTERN, loadedCondition.getCondData());
				break;
			case NETWORK:
				res =  RegexpTools.checkRegexp(RegexpTools.SUBNET_PATTERN, loadedCondition.getCondData());
				break;
			case SNI:
				res = (loadedCondition.getCondData().trim().length()>0);
				break;
			case USER:
				res = (loadedCondition.getCondData().trim().length()>0);
				break;
			default:
				res = false;
				break;
		}
		return res;
	}
	private boolean checkConditionCollision(LoadedCondition newCond) {
		try {
			ConcurrentHashMap<Long, LoadedCondition> conds = loadedConditionsForExtPort.get(newCond.getExtPort());
			if(conds==null||conds.size()<1)
				return false;
			for(Entry<Long, LoadedCondition> condEntry : conds.entrySet()) {
				if(condEntry.getValue().getStatus()==LoadedCondition.STATUS_ENABLED) {
					if(condEntry.getValue().getCondTypeEnum().equals(newCond.getCondTypeEnum())|(
							((condEntry.getValue().getCondTypeEnum().equals(ConditionType.IP))||
							(condEntry.getValue().getCondTypeEnum().equals(ConditionType.NETWORK))
															)&&(
							(condEntry.getValue().getCondTypeEnum().equals(ConditionType.NETWORK))||
							(condEntry.getValue().getCondTypeEnum().equals(ConditionType.IP))))) {
						//System.out.println("Совпал тип или Сеть\\IP: "+condEntry.getValue().getCondTypeEnum()+" - "+newCond.getCondTypeEnum());
						switch(condEntry.getValue().getCondTypeEnum()) {
							case DEFAULT:
								return true;
							case IP:
								if(checkIpConditions(condEntry.getValue().getCondData(),newCond.getCondData()))
									return true;
								break;
							case NETWORK:
								if(checkIpConditions(condEntry.getValue().getCondData(),newCond.getCondData()))
									return true;
								break;
							case SNI:
								if(condEntry.getValue().getCondData().trim().equalsIgnoreCase(newCond.getCondData().trim()))
									return true;
								break;
							case USER:
								if(condEntry.getValue().getCondData().trim().equalsIgnoreCase(newCond.getCondData().trim()))
									return true;
								break;
							default:
								break;
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean checkIpConditions(String ipOrNetwork, String ipOrNetwork2) {
		if((matches(ipOrNetwork.toLowerCase().trim(), ipOrNetwork2.toLowerCase().trim()))
				||
		(matches(ipOrNetwork2.toLowerCase().trim(), ipOrNetwork.toLowerCase().trim())))
			return true;
		return false;
	}
	public boolean matches(String ip, String subnet) {
	    IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(subnet);
	    boolean res = false;
	    try {
	    	int index;
	    	if((index = ip.indexOf("/"))!=-1) {
	    		ip = ip.substring(0, index);
	    	}
	    	res = ipAddressMatcher.matches(ip);
	    	//System.out.println("M: "+ip+" - "+subnet+" | "+res);
	    } catch (Exception e) {
	    	//System.out.println("M: "+ip+" - "+subnet+" | Exception");
		}
	    return res;
	}
	public void stopCondition(LoadedCondition loadedCondition) {
		removeCondition(loadedCondition.getId());
	}
	public ConcurrentHashMap<Long, ConnectedDevice> getLoadedDevices() {
		return connectedDevices;
	}
	public void sendBye(ConnectedDevice conDev) {
		Socket socketToDevice = conDev.getDeviceSocket();
		try {
			tcpListener.getrProtocol().write(socketToDevice, Data.code(Codes.RACCOON_BYE));
			socketToDevice.close();
		} catch (IOException e) {}
	}	
	public void unloadDevice(ConnectedDevice conDev) {
		synchronized (connectedDevices) {
			Long userId = conDev.getUserId();
			if(connectedDevices.remove(conDev.getId())!=null) { 
				longPollStorage.addEvent("devices-"+userId, LongPollEvent.CODE_DEVICE_DISCONNECTED ,"device disconnected");
				longPollStorage.addEvent("deviceconditions-"+conDev.getId(), LongPollEvent.CODE_DEVICE_DISCONNECTED ,"device disconnected");
				stopConditions(conDev);
			}
		}
	}
	
	private void stopConditions(ConnectedDevice connectedDevice) {
		List<Condition> conds = conditionService.getConditionsByDevice(connectedDevice.getId());
		for(Condition cond : conds) {
			LoadedCondition loadedCondition = getCondition(cond.getId());
			if(loadedCondition!=null) {
				if((loadedCondition.getStatus()==LoadedCondition.STATUS_ENABLED)||(loadedCondition.getStatus()==LoadedCondition.STATUS_STARTS_UP)) {
					loadedCondition.setStatus(LoadedCondition.STATUS_TURNED_OFF);		
					stopCondition(loadedCondition);
					longPollStorage.addEvent("deviceconditions-"+cond.getDevice().getId(), LongPollEvent.CODE_CONDITION_STARTING, "condition stopped");
				}
			}
		}
	}
	
	public void unloadAllDevice() {
		synchronized (connectedDevices) {
			connectedDevices.forEach((id,conDev)->{
				try {
					try {
						conDev.getDeviceSocket().close();
					} catch (Exception e) {}
					connectedDevices.remove(id);
				} catch (Exception e) {}
			});
		}
	}
	public Socket getSocket(ConnectedDevice device, String targetHost, Integer targetPort, String asker) throws IOException, SocketTimeoutException, SocketWasRejectedException {
		long taskIdLong = createTask(device,targetHost,targetPort,asker);
		LongPollEvent event = longPollStorage.getEvent(taskIdLong, 0l, 150000l);
		if(event.isTimeout())
			throw new SocketTimeoutException();
		
		if(event.getCode()==LongPollEvent.CODE_SOCKET_REJECT) 
			throw new SocketWasRejectedException();
		
		Socket sock = socketsStorage.get(event.getSockId());
		socketsStorage.remove(event.getSockId());
		return sock;
	}
	private long createTask(ConnectedDevice device, String targetHost, Integer targetPort, String asker) throws IOException {
		String taskId =Tools.generateToken();
		if(sockCounter++>=Long.MAX_VALUE)
			sockCounter=0;
		tasksLinks.put(taskId, sockCounter);
		tcpListener.getrProtocol().write(device.getDeviceSocket(), 
				Data.code(Codes.RACCOON_NEED_SOCKET)
				.setTaskId(taskId)
				.setHost(targetHost)
				.setAsker(asker)
				.setPort(targetPort));
		return sockCounter;
	}
	public Long getSockId(String taskIdS, Boolean delete) {
		long taskId = tasksLinks.get(taskIdS);
		if(delete)
			tasksLinks.remove(taskIdS);
		return taskId;
	}
	public void addSocketForTask(Long sockId, Socket client) {
		socketsStorage.put(sockId, client);
	}
	public void removeTask(String taskIdS) {
		getSockId(taskIdS,true);			
	}
	public String reqNet(ConnectedDevice conDev, String ip, String mask, int timeOutMs) throws IOException {
		String taskId = Tools.generateToken();
		tcpListener.getrProtocol().write(conDev.getDeviceSocket(), Data.code(Codes.RACCON_REQ_NET)
				.setTaskId(taskId)
				.settIp(ip)
				.settMask(mask)
				.settTimeout(timeOutMs));
		return taskId;
	}
	public boolean checkTask(ConnectedDevice conDev, String taskId) throws IOException {
		tcpListener.getrProtocol().write(conDev.getDeviceSocket(), Data.code(Codes.RACCON_CHECK_TASK)
				.setTaskId(taskId));
		LongPollEvent event = longPollStorage.getEvent("check-"+taskId, 0l, 15000l);
		if((!event.isTimeout())&&event.getCode()==1)
			return true;
		return false;
	}
}
