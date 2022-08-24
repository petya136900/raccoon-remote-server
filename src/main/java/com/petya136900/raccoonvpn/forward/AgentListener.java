package com.petya136900.raccoonvpn.forward;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.petya136900.raccoonvpn.RaccoonVPNServer;
import com.petya136900.raccoonvpn.agent.Codes;
import com.petya136900.raccoonvpn.agent.Data;
import com.petya136900.raccoonvpn.agent.RProtocol;
import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.exceptions.DeviceAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotSpecifiedException;
import com.petya136900.raccoonvpn.longpolling.LongPollEvent;
import com.petya136900.raccoonvpn.longpolling.LongPollStorage;
import com.petya136900.raccoonvpn.rest.v1.TaskThreadsCollector;
import com.petya136900.raccoonvpn.services.AuthorizedService;
import com.petya136900.raccoonvpn.services.DeviceService;
import com.petya136900.raccoonvpn.services.UserService;
import com.petya136900.raccoonvpn.tools.JsonParser;
import com.petya136900.raccoonvpn.tools.Tools;
@Service
public class AgentListener {
	@Autowired
	private UserService userService;
	@Autowired
	private AuthorizedService authorizedService;
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private ConnectedDevicesStorage connectedDevicesStorage;
	@Autowired
	private LongPollStorage longPollStorage;
	@Autowired
	private TaskThreadsCollector taskThreadsCollector;
	private static final Logger LOG 
    = Logger.getLogger(AgentListener.class.getName());
	private Integer port;
	private ServerSocket server;
	private Thread serverThread;
	private boolean debug;
	private int TIMEOUT_SEC = 10;
	private RProtocol rProtocol;
	public RProtocol getrProtocol() {
		return rProtocol;
	}
	public void setrProtocol(RProtocol rProtocol) {
		this.rProtocol = rProtocol;
	}
	public void start(Integer port) throws IOException {
		rProtocol = new RProtocol().setDebug(debug);
		synchronized (this) {
			this.port=port;
			debug("Staring TCP socks listener");
			if(serverThread==null) {
				try {
					server = new ServerSocket(port);
					RaccoonVPNServer.agentsPort = port;
				} catch (Exception e) {
					debug("Error, port busy? "+e.getLocalizedMessage());
					throw e;
				}
				serverThread = new Thread(getServerThread(),"TCP LISTENER| :"+port);
				serverThread.start();
			} else {
				debug("Listener already started");
			}
		}		
	}
	private Runnable getServerThread() {
		return ()->{
			debug("TCP Socks listener now running");
			try {
				while(!serverThread.isInterrupted()) {
					try {
						SocketWrapper sWrapper = new SocketWrapper(server.accept());
						debug("New client | "+sWrapper.getSocket().getInetAddress());
						sWrapper.getSocket().setSoTimeout(TIMEOUT_SEC*1000);
						new Thread(()->{
							boolean deviveWasConnected=false;
							ConnectedDevice connectedDev = null;
							String tokenN = null;
							while(!sWrapper.getSocket().isClosed()) {
								try {
									Socket client = sWrapper.getSocket();
									Data rData = null;
									try {
										rData = rProtocol.read(client);
									} catch (SocketTimeoutException e) {
										rProtocol.write(client,Data.code(Codes.RACCOON_PING));
										continue;
									}
									debug("client | "+client.getInetAddress()+" | Data: "+JsonParser.toJson(rData));
									if(rData.codeEqual(Codes.RACCOON_PING)) {
										rProtocol.write(client,Data.code(Codes.RACCOON_OK));
									} else if(rData.codeEqual(Codes.RACCOON_OK)) {
										//
									} else if(rData.codeEqual(Codes.RACCOON_TASK_OK)) {
										longPollStorage.addEvent("check-"+rData.getTaskId(), 1l);
									} else if(rData.codeEqual(Codes.RACCOON_TASK_NF)) {
										longPollStorage.addEvent("check-"+rData.getTaskId(), 0l);
										longPollStorage.addEvent(rData.getTaskId(),new LongPollEvent()
												.setCode(3));
									} else if(rData.codeEqual(Codes.RACCOON_TASK_DATA)) {
										if(taskThreadsCollector.containsKey(rData.getTaskId())) {
											if(rData.getData()==null) {
												continue;
											}
											longPollStorage.addEvent(rData.getTaskId(),new LongPollEvent()
													.setCode(2)
													.setData(rData.getData()));
										} else {
											rProtocol.write(client,Data.code(Codes.RACCOON_TASK_NF).setTaskId(rData.getTaskId()));
										}
									} else if(rData.codeEqual(Codes.RACCOON_LOGIN)) {
										try {
											User user = userService.getUserByLoginOrMail(rData.getLogin());
											if(user.getPassword().equals(Tools.hashSHA256(user.getLogin()+rData.getPasswordHash().trim()))) {
												Authorized auth = null;
												try {
													try {
														auth = authorizedService.getAuthorizedAgentByUserIdAndIp(user.getId(),client.getInetAddress().getHostAddress());
														tokenN = auth.getToken();
														rProtocol.write(client,Data.code(Codes.RACCOON_SUCCESS_LOGIN)
																.setToken(auth.getToken())
																.setUserId(auth.getUserId()));
													} catch (UserNotFoundException e) {
														rProtocol.write(client,Data.code(Codes.RACCOON_USER_NOT_FOUND));
													}
												} catch (Exception e) {
													rProtocol.write(client,Data.code(Codes.RACCOON_UNKNOWN_ERROR));
												}
											} else {
												rProtocol.write(client,Data.code(Codes.RACCOON_WRONG_PASS));
											}
										} catch (Exception e) {
											rProtocol.write(client,Data.code(Codes.RACCOON_USER_NOT_FOUND));
										}
									} else if(rData.codeEqual(Codes.RACCOON_CONNECT)) {
										String devId = rData.getDevId();
										String token = rData.getToken();
										Authorized auth;
										try {
											auth = authorizedService.getAuthorizedByToken(token);
										} catch (Exception e) {
											rProtocol.write(client, Data.code(Codes.RACCOON_BAD_TOKEN));
											continue;
										}
										Device device = null;
										try {
											device = deviceService.getDeviceByUserAndDevId(auth.getUserId(), devId);
										} catch (DeviceNotFoundException e) {}
										if(device==null||(!(device.getUser().getId().equals(auth.getUserId())))) { 
											rProtocol.write(client,Data.code(Codes.RACCOON_REGEN));
										} else {
											ConnectedDevice conDev;
											conDev = connectedDevicesStorage.getDevice(device.getId());
											if(conDev!=null) {
												rProtocol.write(client,Data.code(Codes.RACCOON_CHECKING));
												Thread.sleep((long) ((int)TIMEOUT_SEC*1000*1.5));
												if(connectedDevicesStorage.getDevice(device.getId())!=null) {
													rProtocol.write(client,Data.code(Codes.RACCOON_ALREADY_CONNECTED));
													continue;
												}
											}
											device.setName(rData.getDevName());
											conDev = new ConnectedDevice(device, rData.getLocalIp());
											conDev.setPubIp(client.getInetAddress().getHostAddress());
											conDev.setDeviceSocket(client);
											connectedDevicesStorage.addDevice(conDev);
											rProtocol.write(client,Data.code(Codes.RACCOON_CONNECTED).setServerWebPort(RaccoonVPNServer.getServerPort()));
											deviveWasConnected=true;
											connectedDev=conDev;
										}
									} else if(rData.codeEqual(Codes.RACCOON_NEW)) {
										String devId = rData.getDevId();
										String token = rData.getToken();
										Authorized auth;
										try {
											auth = authorizedService.getAuthorizedByToken(token);
										} catch (Exception e) {
											rProtocol.write(client, Data.code(Codes.RACCOON_BAD_TOKEN));
											continue;
										}
										Device device = null;
										try {
											device = deviceService.getDeviceByUserAndDevId(auth.getUserId(), devId);
										} catch (DeviceNotFoundException e) {}
										if(device!=null) { 
											rProtocol.write(client,Data.code(Codes.RACCOON_REGEN));
										} else {
											device = new Device();
											device.setName(rData.getDevName());
											device.setLocal(false);
											device.setUser(auth.getUser());
											device.setDeviceId(devId);
											try {
												device = deviceService.addNewDevice(device);
											} catch (DeviceAlreadyExistException e) {
												rProtocol.write(client, Data.code(Codes.RACCOON_UNKNOWN_CODE));
												continue;
											} catch (UserNotSpecifiedException e) {
												rProtocol.write(client, Data.code(Codes.RACCOON_BAD_TOKEN));
												continue;
											}
											ConnectedDevice conDev;
											conDev = connectedDevicesStorage.getDevice(device.getId());
											conDev = new ConnectedDevice(device, rData.getLocalIp());
											conDev.setPubIp(client.getInetAddress().getHostAddress());
											conDev.setDeviceSocket(client);
											connectedDevicesStorage.addDevice(conDev);
											rProtocol.write(client,Data.code(Codes.RACCOON_CONNECTED).setServerWebPort(RaccoonVPNServer.getServerPort()));
											deviveWasConnected=true;
											connectedDev=conDev;
										}
									} else if(rData.codeEqual(Codes.RACCOON_SOCKET)) {
										client.setSoTimeout(0);
										Long sockId = connectedDevicesStorage.getSockId(rData.getTaskId(),true);
										if(sockId!=null) {
											connectedDevicesStorage.addSocketForTask(sockId,client);
											longPollStorage.addEvent(sockId,
													new LongPollEvent()
													.setCode(LongPollEvent.CODE_SOCKET_FOUND)
													.setSockId(sockId));
											return;
										} else {
											client.close();
										}
									} else if(rData.codeEqual(Codes.RACCOON_SOCKET_REJECT)) {
										debug("client | "+client.getInetAddress()+" | Task was rejected: "+rData.getHost()+":"+rData.getPort());
										connectedDevicesStorage.removeTask(rData.getTaskId());
										rProtocol.write(client,Data.code(Codes.RACCOON_OK));
									} else if(rData.codeEqual(Codes.RACCOON_UPGRADE_TO_TLS)) {
										debug("client | "+client.getInetAddress()+" | Client ask us to use TLS - ok");
										rProtocol.write(client,Data.code(Codes.RACCOON_UPGRADE_TO_TLS));
										SSLSocketFactory sslSocketFactory = getSslSocketFactoryForServer();
											SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
																client,
																client.getInetAddress().getHostAddress(),
																client.getPort(),
																false);
												    sslSocket.setUseClientMode(false);
												    sslSocket.startHandshake();		
												    sWrapper.setSocket(sslSocket);
									}
									else {
										rProtocol.write(client,Data.code(Codes.RACCOON_UNKNOWN_CODE));
									}
								} catch (Exception e) {
									debug("client | "+sWrapper.getSocket().getInetAddress()+" | Client disconnected");
									try {
										sWrapper.getSocket().close();
									} catch (IOException e1) {}
								}
							}
							if(tokenN!=null) {
								authorizedService.revokeToken(tokenN);
							}
							if(deviveWasConnected&&connectedDev!=null) {
								connectedDevicesStorage.unloadDevice(connectedDev);
							}
						},"RP :"+port+"| "+sWrapper.getSocket().getInetAddress()+":"+sWrapper.getSocket().getPort()).start();
					} catch (IOException e) {
						// can't accept client
					}
				}
			} catch (NullPointerException e) {}
			debug("TCP Socks listener now stopped");
		};
	}
	private static SSLSocketFactory getSslSocketFactoryForServer() throws IOException {
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf
	        = KeyManagerFactory.getInstance("SunX509");
	        char ksPass[] = RaccoonVPNServer.KEYSTORE_PASS.toCharArray();
	        KeyStore ks = KeyStore.getInstance("JKS");
	        ks.load(new FileInputStream(Tools.WORK_DIR+RaccoonVPNServer.KEYSTORE_FILE), ksPass);
	        kmf.init(ks,ksPass);
	        KeyManager[] kmList = kmf.getKeyManagers();
	        sc.init(kmList, null, null);
	        SSLSocketFactory sf = sc.getSocketFactory();
	        return sf;
		} catch (Exception e) {
			throw new IOException();
		}
	}
	private void debug(String string) {
		if(debug)
			LOG.info(":"+port+" | "+string);
	}
	public void stop() {
		synchronized (this) {
			if(serverThread!=null&&serverThread.isAlive()) {
				serverThread.interrupt();
				if(server!=null&&((server.isClosed())|server.isBound())) {
					try {
						server.close();
					} catch (IOException e) { }
				}
			}
			serverThread=null;
		}
	}
	
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public int getTIMEOUT_SEC() {
		return TIMEOUT_SEC;
	}
	public void setTIMEOUT_SEC(int tIMEOUT_SEC) {
		TIMEOUT_SEC = tIMEOUT_SEC;
	}
	public void startAt(Integer port) {
		port =  Tools.checkFreePort(port); 
    	LOG.info("Starting TCP Listener at port "+port);
    	try { 
    		setDebug(RaccoonVPNServer.RACCON_DEBUG);
    		start(port);
    		LOG.info("Listener started");
    	} catch (Exception e) {
    		LOG.warning("Error: "+e.getLocalizedMessage());
		}
	}
}
