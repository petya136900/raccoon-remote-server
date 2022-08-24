package com.petya136900.raccoonvpn.rest.v1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.JsonSyntaxException;
import com.petya136900.raccoonvpn.RaccoonVPNServer;
import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.enums.ConditionType;
import com.petya136900.raccoonvpn.enums.Protocol;
import com.petya136900.raccoonvpn.exceptions.ApiException;
import com.petya136900.raccoonvpn.exceptions.AuthorizedNotFoundException;
import com.petya136900.raccoonvpn.exceptions.ConditionAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.ConditionNotFoundException;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotFoundException;
import com.petya136900.raccoonvpn.forward.ConnectedDevice;
import com.petya136900.raccoonvpn.forward.ConnectedDevicesStorage;
import com.petya136900.raccoonvpn.forward.LoadedCondition;
import com.petya136900.raccoonvpn.forward.AgentListener;
import com.petya136900.raccoonvpn.gson.DeviceGson;
import com.petya136900.raccoonvpn.gson.SettingGson;
import com.petya136900.raccoonvpn.gson.UserGson;
import com.petya136900.raccoonvpn.longpolling.LongPollEvent;
import com.petya136900.raccoonvpn.longpolling.LongPollStorage;
import com.petya136900.raccoonvpn.rest.AuthorizedAdmin;
import com.petya136900.raccoonvpn.rest.AuthorizedChecker;
import com.petya136900.raccoonvpn.rest.AuthorizedUser;
import com.petya136900.raccoonvpn.rest.HandlerInterface;
import com.petya136900.raccoonvpn.rest.v1.codes.ResponseCodes;
import com.petya136900.raccoonvpn.services.AuthorizedService;
import com.petya136900.raccoonvpn.services.ConditionService;
import com.petya136900.raccoonvpn.services.DeviceService;
import com.petya136900.raccoonvpn.services.SettingService;
import com.petya136900.raccoonvpn.services.UserService;
import com.petya136900.raccoonvpn.tools.JsonParser;
import com.petya136900.raccoonvpn.tools.Response;
import com.petya136900.raccoonvpn.tools.Tools;

@RestController 
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1",method = RequestMethod.GET)
public class ApiV1Controller {
	private static final Logger LOG 
    = Logger.getLogger(ApiV1Controller.class.getName());
	@Autowired
	private UserService userService;
	@Autowired
	private SettingService settingService;
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private ConditionService conditionService;
	@Autowired
	private AuthorizedService authorizedService;
	@Autowired
	private AuthorizedUser authorizedUser;
	@Autowired
	private AuthorizedAdmin authorizedAdmin;	
	/*
	 * LONG POLL
	 */
	@Autowired
	private LongPollStorage longPollStorage;
	@GetMapping("/longpoll/updates/deviceconditions")
	public String getLongPollUpdatesDeviceConditions(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "0") String ts,
			@RequestParam(defaultValue = "n") String devId) {
		return handle(token, request, authorizedUser, (x)->{
			Long tsLong;
			Long devIdLong;
			try {
				tsLong = Long.parseLong(ts);
			} catch (Exception e) {
				tsLong=0L;
			}
			try {
				devIdLong = Long.parseLong(devId);
			} catch (Exception e) {
				return Response.error(ResponseCodes.BAD_DEVICE_ID);
			}
			Device device;
			try {
				device = deviceService.getDeviceById(devIdLong);
			} catch (DeviceNotFoundException e) {
				return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
			}
			
			if(!(x.getUser().getAdmin())&&(!(device.getUser().getId().equals(x.getUserId()))))
				return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
			
			LongPollEvent event = longPollStorage.getEvent("deviceconditions-"+devIdLong,tsLong,14000L);
			return Response.code(ResponseCodes.GOOD_TOKEN).setEvent(event);
		}).toJson();
	} 
	
	@GetMapping("/longpoll/updates/userdevices")
	public String getLongPollUpdatesUserDevices(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "0") String ts,
			@RequestParam(defaultValue = "n") String userId) {
		return handle(token, request, authorizedUser, (x)->{
			Long tsLong;
			Long userIdLong;
			try {
				tsLong = Long.parseLong(ts);
			} catch (Exception e) {
				tsLong=0L;
			}
			try {
				userIdLong = Long.parseLong(userId);
			} catch (Exception e) {
				userIdLong = x.getUserId();
			}
			
			if(!(x.getUser().getAdmin())&&(!(x.getUserId().equals(userIdLong))))
				return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
			
			LongPollEvent event = longPollStorage.getEvent("devices-"+userIdLong,tsLong,14000L);
			return Response.code(ResponseCodes.GOOD_TOKEN).setEvent(event);
		}).toJson();
	} 
	
	@GetMapping("/longpoll/updates/device")
	public String getLongPollUpdatesDevice(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "0") String ts,
			@RequestParam(defaultValue = "n") String devId) {
				return handle(token, request, authorizedUser, (x)->{
					Long tsLong;
					Long devIdLong;
					try {
						tsLong = Long.parseLong(ts);
					} catch (Exception e) {
						tsLong=0L;
					}
					try {
						devIdLong = Long.parseLong(devId);
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_DEVICE_ID);
					}
					Device device;
					try {
						device = deviceService.getDeviceById(devIdLong);
					} catch (DeviceNotFoundException e) {
						return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
					}
					if(!(x.getUser().getAdmin())&&(!(x.getUserId().equals(device.getUser().getId()))))
						return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					LongPollEvent event = longPollStorage.getEvent("device-"+devIdLong,tsLong,14000L);
					return Response.code(ResponseCodes.GOOD_TOKEN).setEvent(event);
				}).toJson();
	} 
	
	@GetMapping("/longpoll/updates/users")
	public String getLongPollUpdatesUsers(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "0") String ts) {
				return handle(token, request, authorizedAdmin, (x)->{
					Long tsLong;
					try {
						tsLong = Long.parseLong(ts);
					} catch (Exception e) {
						tsLong=0L;
					}
					LongPollEvent event = longPollStorage.getEvent("users",tsLong,14000L);
					return Response.code(ResponseCodes.GOOD_TOKEN).setEvent(event);
				}).toJson();
	} 	
	
	@GetMapping("/user/getall")
	public String userGetAll(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
				return handle(token, request, authorizedAdmin, (x)->{
					List<User> users = userService.list();
					List<UserGson> usersGson = users.stream()
							.filter(user->user.getLogin()!=null&&user.getLogin().trim().length()>0)
							.map(user->new UserGson(user))
							.collect(Collectors.toList());
					return Response.code(ResponseCodes.GOOD_TOKEN).setUsers(usersGson.toArray(new UserGson[usersGson.size()]));
				}).toJson();
	}
	
	@GetMapping("/tools/reqcert")
	public String certReq(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String domain) {
				return handle(token, request, authorizedAdmin, (x)->{
					try {
						new ACME().go(domain);
					} catch (IOException | InterruptedException e) {
						return Response.error(ResponseCodes.UNKNOWN_ERROR).setCodeDesc(e.getLocalizedMessage());
					} catch (IllegalArgumentException e2) {
						return Response.error(ResponseCodes.BAD_DOMAIN_NAME);
					}
					return Response.code(ResponseCodes.GOOD_TOKEN);
				}).toJson();
	}
	
	@Autowired
	private TaskThreadsCollector taskThreadsCollector;
	
	@GetMapping("/tools/owogetlist")
	public String toolsGetListOwo(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
				return handle(token, request, authorizedAdmin, (x)->{
					ArrayList<String> list = new ArrayList<>();
					taskThreadsCollector.forEach((k,v)->{
						list.add(k);
					});
					return Response.code(ResponseCodes.GOOD_TOKEN).setList(list.toArray(new String[list.size()]));
				}).toJson();
	}
	
	@GetMapping("/tools/owostopall")
	public String toolsStopAllOwo(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
				return handle(token, request, authorizedAdmin, (x)->{
					taskThreadsCollector.forEach((k,v)->{
						v.stop();
						taskThreadsCollector.remove(k);
					});
					return Response.code(ResponseCodes.GOOD_TOKEN);
				}).toJson();
	}
	
	@GetMapping("/tools/owoget/{taskId}")
	public String toolsGetOwo(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@PathVariable String taskId) {
				return handle(token, request, authorizedAdmin, (x)->{
					TaskThread tt = taskThreadsCollector.get(taskId);
					if(tt==null)
						return Response.error(ResponseCodes.BAD_TASK_ID);
					return Response.code(ResponseCodes.GOOD_TOKEN).setData(tt.getData()).setDone(tt.isDone());
				}).toJson();
	}
	
	@GetMapping("/tools/owostop/{taskId}")
	public String toolsStopOwo(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@PathVariable String taskId) {
				return handle(token, request, authorizedAdmin, (x)->{
					TaskThread tt = null;
					if(taskId==null||(tt = taskThreadsCollector.remove(taskId))==null) {
						return Response.error(ResponseCodes.BAD_TASK_ID);
					}
					tt.stop();
					return Response.code(ResponseCodes.GOOD_TOKEN);
				}).toJson();
	}
	
	@GetMapping("/tools/owofind/{devId}/{ip}/{mask}/{timeout}")
	public String toolsFindOwoByDevId(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@PathVariable String devId, @PathVariable String ip, @PathVariable String mask, @PathVariable String timeout)  {
				return handle(token, request, authorizedAdmin, (x)->{
					Device device;
					try {
						device = deviceService.getDeviceById(Long.parseLong(devId.trim()));
					} catch (NumberFormatException e1) {
						return Response.error(ResponseCodes.BAD_DEVICE_ID);
					} catch (DeviceNotFoundException e1) {
						return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
					}
					ConnectedDevice conDev = connectedDevicesStorage.getDevice(device.getId());
					if(conDev==null)
						return Response.error(ResponseCodes.DEVICE_NOT_CONNECTED);
					try {
						int timeOutMs = Integer.parseInt(timeout);
						SubnetUtils utils = new SubnetUtils(ip+"/"+mask);
						utils.getInfo().getAllAddresses();
						String taskId = connectedDevicesStorage.reqNet(conDev,ip,mask,timeOutMs);
						TaskThread tt = new TaskThread((sb)->{
							long lts = 0;
							while(!Thread.currentThread().isInterrupted()) {
								LongPollEvent event = longPollStorage.getEvent(taskId, lts, 15000l);
								if(!event.isTimeout()) {
									lts=event.getTs();
									if(event.getCode()==2) {
										sb.append(event.getData());
									} else if(event.getCode()==3) {
										break;
									}
								} else {
									try {
										if(!connectedDevicesStorage.checkTask(conDev,taskId)) {
											break;
										}
									} catch (IOException e) {
										break;
									}
								}
							}
						});
						taskThreadsCollector.put(taskId, tt);
						tt.start();
						return Response.code(ResponseCodes.GOOD_TOKEN);
					} catch (Exception e) {
						return Response.error(ResponseCodes.UNKNOWN_ERROR);
					}
				}).toJson();
	}
	
	@GetMapping("/tools/owofind/{ip}/{mask}/{timeout}")
	public String toolsFindOwo(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@PathVariable String ip, @PathVariable String mask, @PathVariable String timeout) {
				return handle(token, request, authorizedAdmin, (x)->{
					try {
						int timeOutMs = Integer.parseInt(timeout);
						SubnetUtils utils = new SubnetUtils(ip+"/"+mask);
						final String[] allIps = utils.getInfo().getAllAddresses();
						String taskId = Tools.generateToken();
						TaskThread tt = new TaskThread((sb)->{
							boolean f = true;
							for(String oneIp : allIps) {
								if(Thread.currentThread().isInterrupted())
									break;
								try {
									if (InetAddress.getByName(oneIp).isReachable(timeOutMs)){
										sb.append((f?"":", ")+oneIp+" - "+InetAddress.getByName(oneIp).getHostName());
										f=false;
									}
								} catch (Exception e) {}
							}
						});
						taskThreadsCollector.put(taskId, tt);
						tt.start();
						return Response.code(ResponseCodes.GOOD_TOKEN);
					} catch (Exception e) {
						return Response.error(ResponseCodes.UNKNOWN_ERROR);
					}
				}).toJson();
	}
	
	@GetMapping("/tools/getloginbyid")
	public String getLoginById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					try {
						User user = userService.getUserById(Long.parseLong(id));
						if(user.getLogin()==null)
							throw new IllegalArgumentException();
						return Response.code(ResponseCodes.GOOD_TOKEN).setLogin(user.getLogin());
					}  catch (Exception e) {
						return Response.code(ResponseCodes.GOOD_TOKEN).setLogin("USER_NOT_FOUND");
					}
				}).toJson();
	}
	
	@Autowired
	private ConnectedDevicesStorage connectedDevicesStorage;

	@GetMapping("/conditions/stopbyid")
	public String conditionsStopById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
						return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
					}
					Long condId;
					try {
						condId = Long.parseLong(id.trim());
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_CONDITION_ID);
					}
					Condition condition;
					try {
						condition = conditionService.getConditionById(condId);
					} catch (ConditionNotFoundException e) {
						return Response.error(ResponseCodes.CONDITION_NOT_FOUND);
					}
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(condition.getDevice().getUser().getId())))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					
					LoadedCondition loadedCondition = connectedDevicesStorage.getCondition(condId);
					if(loadedCondition==null||(!(loadedCondition.getStatus()==LoadedCondition.STATUS_ENABLED))) {
						return Response.error(ResponseCodes.CONDITION_NOT_RUNNING);
					} 
					loadedCondition.setStatus(LoadedCondition.STATUS_TURNED_OFF);
					connectedDevicesStorage.stopCondition(loadedCondition);
					longPollStorage.addEvent("deviceconditions-"+condition.getDevice().getId(), LongPollEvent.CODE_CONDITION_DELETED, "condition stopped");
					//
					return Response.code(ResponseCodes.CONDITION_RUNNING);
				}).toJson();
	}
	
	///
	
	
	
	///
	
	@GetMapping("/longpoll/updates/settings")
	public String getLongPollUpdatesSettings(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "0") String ts) {
				return handle(token, request, authorizedAdmin, (x)->{
					Long tsLong;
					try {
						tsLong = Long.parseLong(ts);
					} catch (Exception e) {
						tsLong=0L;
					}
					LongPollEvent event = longPollStorage.getEvent("settings",tsLong,14000L);
					return Response.code(ResponseCodes.GOOD_TOKEN).setEvent(event);
				}).toJson();
	} 
	
	// START SETTINGS
	
	@Autowired
    private AgentListener tcpForwarderListener;
	
	@RequestMapping(value = "/setting/uploadcert", method = RequestMethod.POST)
    public String settingUploadCert(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
            @RequestParam("crt") MultipartFile crt,
            @RequestParam("key") MultipartFile key) {
		return handle(token, request, authorizedAdmin, (x)->{
			if(crt.isEmpty()&&key.isEmpty()) {
				return Response.error(ResponseCodes.BAD_FILES);	
			}
			String unic = Tools.generateToken();
			try {
				Tools.createFile("certs/"+unic+".crt", crt.getBytes());
				Tools.createFile("certs/"+unic+".key", key.getBytes());
			} catch (Exception e) {
				Tools.deleteFile("certs/"+unic+".crt");
				Tools.deleteFile("certs/"+unic+".key");
				return Response.error(ResponseCodes.ERROR_WHILE_CREATING_FILES);
			}
			try {
				Tools.convertPemToJKS(unic);
				Tools.copyFile("keystore.jks","certs/olds/old"+Tools.generateToken()+".jks");
				Tools.deleteFile("keystore.jks");
				Tools.copyFile("certs/"+unic+".jks","keystore.jks");
				Tools.deleteFile("certs/"+unic+".jks");
			} catch (Exception e) {
				Tools.deleteFile("certs/"+unic+".crt");
				Tools.deleteFile("certs/"+unic+".key");
				Tools.deleteFile("certs/"+unic+".p12");
				return Response.error(ResponseCodes.CANT_CONVERT_CERT);
			}
			Tools.deleteFile("certs/"+unic+".crt");
			Tools.deleteFile("certs/"+unic+".key");
			Tools.deleteFile("certs/"+unic+".p12");
			RaccoonVPNServer.restart();
			return Response.code(ResponseCodes.GOOD_TOKEN);
		}).toJson();
    }
	
	@GetMapping("/setting/getall")
	public String settingGetAll(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token,
		@RequestParam(defaultValue = "none") String port) {
			return handle(token, request, authorizedAdmin, (x)->{
				SettingGson settingGson = new SettingGson();
				settingGson.setAllowRegister(settingService.getAllowRegister());
				settingGson.setAllowCUBU(settingService.getAllowChangeUserByUser());
				settingGson.setAllowCRBU(settingService.getAllowCreateRulesByUser());
				return Response.code(ResponseCodes.GOOD_TOKEN).setSettings(settingGson);
			}).toJson();
	}	
	@GetMapping("/setting/webport")
	public String settingWebPort(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token,
		@RequestParam(defaultValue = "none") String port) {
			return handle(token, request, authorizedAdmin, (x)->{
				Integer intPort;
				try {
					intPort = Integer.parseInt(port);
					if(intPort>65535||intPort<0)
						throw new IllegalArgumentException();
				} catch (Exception e) {
					return Response.error(ResponseCodes.BAD_PORT);
				}
				if(intPort.equals(RaccoonVPNServer.HTTPS_PORT))
					return Response.error(ResponseCodes.PORT_ALREADY_IN_USE);
				
				Integer cPort = Tools.checkFreePort(intPort);
				if(!cPort.equals(intPort))
					return Response.error(ResponseCodes.PORT_BUSY);
				try {
					Tools.createFile("wport", intPort+"");
				} catch (IOException e1) {
					return Response.error(ResponseCodes.UNKNOWN_ERROR);
				}
				RaccoonVPNServer.restart();
				longPollStorage.addEvent("settings", LongPollEvent.CODE_SETTING_CHANGED);
				return Response.code(ResponseCodes.SERVER_RELOADING);
			}).toJson();
	}	
	@GetMapping("/setting/agentsport")
	public String settingAgentsPort(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token,
		@RequestParam(defaultValue = "none") String port) {
			return handle(token, request, authorizedAdmin, (x)->{
				Integer intPort;
				try {
					intPort = Integer.parseInt(port);
					if(intPort>65535||intPort<0)
						throw new IllegalArgumentException();
				} catch (Exception e) {
					return Response.error(ResponseCodes.BAD_PORT);
				}
				if(intPort.equals(RaccoonVPNServer.agentsPort))
					return Response.error(ResponseCodes.PORT_ALREADY_IN_USE);
				
				Integer cPort = Tools.checkFreePort(intPort);
				if(!cPort.equals(intPort))
					return Response.error(ResponseCodes.PORT_BUSY);
				
				tcpForwarderListener.stop();
				tcpForwarderListener.startAt(intPort);
				settingService.setAgentsPort(intPort);
				longPollStorage.addEvent("settings", LongPollEvent.CODE_SETTING_CHANGED);
				return Response.code(ResponseCodes.PORT_UPDATED);
			}).toJson();
	}	
	@GetMapping("/setting/toggleregister")
	public String settingToggleRegister(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
			return handle(token, request, authorizedAdmin, (x)->{
				settingService.allowRegister(!settingService.getAllowRegister());
				longPollStorage.addEvent("settings", LongPollEvent.CODE_SETTING_CHANGED);
				return Response.code(ResponseCodes.SETTING_CHANGED);
			}).toJson();
	}	
	@GetMapping("/setting/togglecubu")
	public String settingToggleChangeUserByUser(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
			return handle(token, request, authorizedAdmin, (x)->{
				settingService.allowChangeUserByUser(!settingService.getAllowChangeUserByUser());
				longPollStorage.addEvent("settings", LongPollEvent.CODE_SETTING_CHANGED);
				return Response.code(ResponseCodes.SETTING_CHANGED);
			}).toJson();
	}	
	@GetMapping("/setting/togglecrbu")
	public String settingToggleCreateRulesByUser(HttpServletRequest request,
		@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
			return handle(token, request, authorizedAdmin, (x)->{
				settingService.allowCreateRulesByUser(!settingService.getAllowCreateRulesByUser());
				longPollStorage.addEvent("settings", LongPollEvent.CODE_SETTING_CHANGED);
				return Response.code(ResponseCodes.SETTING_CHANGED);
			}).toJson();
	}	
	@GetMapping("/conditions/startbyid")
	public String conditionsStartById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
						return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
					}
					Long condId;
					try {
						condId = Long.parseLong(id.trim());
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_CONDITION_ID);
					}
					Condition condition;
					try {
						condition = conditionService.getConditionById(condId);
					} catch (ConditionNotFoundException e) {
						return Response.error(ResponseCodes.CONDITION_NOT_FOUND);
					}
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(condition.getDevice().getUser().getId())))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					
					ConnectedDevice conDev = connectedDevicesStorage.getDevice(condition.getDevice().getId());
					if(conDev==null)
						return Response.error(ResponseCodes.DEVICE_NOT_CONNECTED);
					
					LoadedCondition loadedCondition = connectedDevicesStorage.getCondition(condId);	
					if(loadedCondition==null) {
						loadedCondition = connectedDevicesStorage.loadCondition(new LoadedCondition(condition));
					} else {
						loadedCondition.setStatus(LoadedCondition.STATUS_STARTS_UP);
						connectedDevicesStorage.startCondition(loadedCondition);
					}
					longPollStorage.addEvent("deviceconditions-"+condition.getDevice().getId(), LongPollEvent.CODE_CONDITION_STARTING, "condition starts up");
					return Response.code(ResponseCodes.CONDITION_RUNNING);
				}).toJson();
	}
	
	@RequestMapping(value = "/conditions/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String addCondition(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestBody String data) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
			}
			try {
				LoadedCondition cond = JsonParser.fromJson(data, LoadedCondition.class);
				Device device;
				try {
					device = deviceService.getDeviceById(cond.getDevId());
				} catch (DeviceNotFoundException e1) {
					return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
				}
				if(!x.getUser().getAdmin())
					if(!(x.getUserId().equals(device.getUser().getId())))
						return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
				if(cond.getCondType().equalsIgnoreCase(ConditionType.USER.toString())) {
					try {
						User user = userService.getUserByLoginOrMail(cond.getCondData().trim());
						cond.setCondData(user.getId()+"");
					} catch (UserNotFoundException e) {
						return Response.error(ResponseCodes.USER_NOT_FOUND);
					}
				}
				LoadedCondition loadedCond = null;
				try {
					loadedCond = connectedDevicesStorage.addConditionAndLoad(cond,false);
				} catch (ConditionAlreadyExistException e) {
					return Response.error(ResponseCodes.CONDITION_ALREADY_EXIST);
				} catch (DeviceNotFoundException e) {
					return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
				} 
				return Response.code(ResponseCodes.CONDITION_ADDED).setConditions(new LoadedCondition[] {loadedCond});
			} catch (JsonSyntaxException e) {
				return Response.error(ResponseCodes.BAD_JSON);
			}
		}).toJson();
    }
	
	@RequestMapping(value = "/conditions/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateCondition(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestBody String data) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
			}
			try {
				LoadedCondition newCond = JsonParser.fromJson(data, LoadedCondition.class);
				Condition condition;
				try {
					condition = conditionService.getConditionById(newCond.getId());
				} catch (ConditionNotFoundException e) {
					return Response.error(ResponseCodes.CONDITION_NOT_FOUND);
				}
				if(!x.getUser().getAdmin())
					if(!(x.getUserId().equals(condition.getDevice().getUser().getId())))
						return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
				if(newCond.getCondType().equalsIgnoreCase(ConditionType.USER.toString())) {
					try {
						User user = userService.getUserByLoginOrMail(newCond.getCondData().trim());
						newCond.setCondData(user.getId()+"");
					} catch (UserNotFoundException e) {
						return Response.error(ResponseCodes.USER_NOT_FOUND);
					}
				}
				condition.setAutorun(newCond.getAutorun());
				condition.setCondData(newCond.getCondData());
				condition.setCondType(ConditionType.valueOf(newCond.getCondType().toUpperCase()));
				condition.setExtPort(newCond.getExtPort());
				condition.setFirewall(newCond.getFirewall());
				condition.setName(newCond.getName());
				condition.setProtocol(Protocol.valueOf(newCond.getProtocol().toUpperCase()));
				condition.setTargetHost(newCond.getTargetHost());
				condition.setTargetPort(newCond.getTargetPort());
				condition = conditionService.updateCondition(condition);
				try {
					connectedDevicesStorage.updateCondition(condition);
				} catch (DeviceNotFoundException e) {
					return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
				} catch (ConditionAlreadyExistException e) {
					return Response.error(ResponseCodes.CONDITION_ALREADY_EXIST);
				}
				longPollStorage.addEvent("deviceconditions-"+condition.getDevice().getId(), LongPollEvent.CODE_CONDITION_CHANGED, "condition updated");
				return Response.code(ResponseCodes.CONDITION_UPDATED);
			} catch (JsonSyntaxException e) {
				return Response.error(ResponseCodes.BAD_JSON);
			}
		}).toJson();
    }
	
	@GetMapping("/conditions/deleteallbydevid")
	public String conditionsDeleteAllByDevId(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
			}
			Long devId;
			try {
				devId = Long.parseLong(id.trim());
			} catch (Exception e) {
				return Response.error(ResponseCodes.BAD_DEVICE_ID);
			}
			Device device;
			try {
				device = deviceService.getDeviceById(devId);
			} catch (DeviceNotFoundException e) {
				return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
			}
			if(!x.getUser().getAdmin())
				if(!(x.getUserId().equals(device.getUser().getId())))
					return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
			
			List<Condition> list = conditionService.getConditionsByDevice(devId);
			for(Condition cond : list) {
				connectedDevicesStorage.removeCondition(cond.getId());
				conditionService.deleteConditionById(cond.getId());	
			}
			longPollStorage.addEvent("deviceconditions-"+device.getId(), LongPollEvent.CODE_CONDITION_DELETED, "all conditions deleted");
			return Response.code(ResponseCodes.CONDITION_DELETED);
		}).toJson();
	}
	
	@GetMapping("/conditions/deletebyid")
	public String conditionsDeleteById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
						return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
					}
					Long condId;
					try {
						condId = Long.parseLong(id.trim());
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_CONDITION_ID);
					}
					Condition condition;
					try {
						condition = conditionService.getConditionById(condId);
					} catch (ConditionNotFoundException e) {
						return Response.error(ResponseCodes.CONDITION_NOT_FOUND);
					}
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(condition.getDevice().getUser().getId())))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					connectedDevicesStorage.removeCondition(condId);
					conditionService.deleteConditionById(condId);
					longPollStorage.addEvent("deviceconditions-"+condition.getDevice().getId(), LongPollEvent.CODE_CONDITION_DELETED, "condition deleted");
					return Response.code(ResponseCodes.CONDITION_DELETED);
				}).toJson();
	}
	
	@GetMapping("/conditions/getbydeviceid")
	public String conditionsGetDeviceId(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					Long deviceId;
					try {
						deviceId = Long.parseLong(id.trim());
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_DEVICE_ID);
					}
					Device device;
					try {
						device = deviceService.getDeviceById(deviceId);
					} catch (DeviceNotFoundException e) {
						return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
					}
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(device.getUser().getId())))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					ArrayList<Condition> conditions = new ArrayList<>();
					conditions.addAll(conditionService.getConditionsByDevice(deviceId));
					List<LoadedCondition> loadedConditions = conditions.stream()
							.map(condition->{
								LoadedCondition loadedCondition = connectedDevicesStorage.getCondition(condition.getId());
								if(loadedCondition!=null) {
									return loadedCondition;
								} else {
									loadedCondition = new LoadedCondition(condition);
									return loadedCondition;
								}
							})	
							.collect(Collectors.toList());
					DeviceGson deviceGson = new DeviceGson(device);
					ConnectedDevice conDev = connectedDevicesStorage.getDevice(device.getId()); 
					if(conDev!=null) {
						deviceGson.setConnected(true);
						deviceGson.setIp(conDev.getIp());
					}					
					return Response.code(ResponseCodes.GOOD_TOKEN)
							.setConditions(loadedConditions.toArray(new LoadedCondition[loadedConditions.size()]))
							.setDevice(deviceGson);
				}).toJson();
	}
	
	@GetMapping("/devices/deletebyid")
	public String devicesDeleteById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
			}
			Long devId;
			try {
				devId = Long.parseLong(id);
			} catch (Exception e) {
				return Response.error(ResponseCodes.BAD_DEVICE_ID);
			}
			Device device = null;
			try {
				device = deviceService.getDeviceById(devId);
			} catch (DeviceNotFoundException e) {
				return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
			}
			if(!x.getUser().getAdmin())
				if(!(x.getUserId().equals(device.getUser().getId())))
					return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
			if(device.isLocal())
				return Response.error(ResponseCodes.CANT_DELETE_LOCAL_DEVICE);
			deviceService.deleteDeviceById(device.getId());
			ConnectedDevice conDev = connectedDevicesStorage.getDevice(devId);
			if(conDev!=null) {
				try {
					conDev.getDeviceSocket().close();
				} catch (IOException e) {}
			}
			longPollStorage.addEvent("devices-"+device.getUser().getId(), LongPollEvent.CODE_DEVICE_CONNECTED ,"device deleted");
			return Response.code(ResponseCodes.DEVICE_DELETED);
		}).toJson();
	}
	
	@GetMapping("/devices/disconnectbyid")
	public String devicesDiscById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowCreateRulesByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.CREATE_RULES_BY_USER_DISABLED);
			}
			Long devId;
			try {
				devId = Long.parseLong(id);
			} catch (Exception e) {
				return Response.error(ResponseCodes.BAD_DEVICE_ID);
			}
			Device device = null;
			try {
				device = deviceService.getDeviceById(devId);
			} catch (DeviceNotFoundException e) {
				return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
			}
			if(!x.getUser().getAdmin())
				if(!(x.getUserId().equals(device.getUser().getId())))
					return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);

			if(device.isLocal())
				return Response.error(ResponseCodes.CANT_DISCONNECT_LOCAL_DEVICE);
			
			ConnectedDevice conDev = connectedDevicesStorage.getDevice(devId);
			if(conDev==null)
				return Response.error(ResponseCodes.DEVICE_NOT_CONNECTED);
			connectedDevicesStorage.sendBye(conDev);
			return Response.code(ResponseCodes.DEVICE_DISCONNECTED);	
		}).toJson();
	}

	@GetMapping("/user/deletebyid")
	public String userDeleteById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
		return handle(token, request, authorizedUser, (x)->{
			Long userId;
			try {
				userId = Long.parseLong(id);
			} catch (Exception e) {
				return Response.error(ResponseCodes.BAD_USER_ID);
			}
			User user = null;
			try {
				user = userService.getUserById(userId);
			} catch (UserNotFoundException e) {
				return Response.error(ResponseCodes.USER_NOT_FOUND);
			}
			if(!x.getUser().getAdmin())
				if(!(x.getUserId().equals(userId)))
					return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
			
			if((!settingService.getAllowChangeUserByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.USER_CHANGE_DISABLED);
			}
			
			List<User> admins = userService.listAdmins();
			if(admins.size()<2&&user.getAdmin()) {
				return Response.error(ResponseCodes.CANT_DELETE_LAST_ADMIN);
			}
			if(userService.deleteUserById(userId)) {
				longPollStorage.addEvent("users",LongPollEvent.USER_DELETED,"user deleted");
				return Response.code(ResponseCodes.USER_DELETED);
			}
			return Response.error(ResponseCodes.FAILED_TO_DELETE_USER);
		}).toJson();
	}
	
	
	@GetMapping("/devices/getbyid")
	public String devicesGetById(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					Long devId;
					try {
						devId = Long.parseLong(id);
					} catch (Exception e) {
						return Response.error(ResponseCodes.BAD_DEVICE_ID);
					}
					Device device;
					try {
						device = deviceService.getDeviceById(devId);
					} catch (DeviceNotFoundException e) {
						return Response.error(ResponseCodes.DEVICE_NOT_FOUND);
					}
					
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(device.getUser().getId())))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					DeviceGson deviceG = new DeviceGson(device);
					ConnectedDevice conDev = connectedDevicesStorage.getDevice(device.getId());
					if(conDev!=null) {
						deviceG.setConnected(true);
						deviceG.setIp(conDev.getIp());
					}
					return Response.code(ResponseCodes.GOOD_TOKEN).setDevice(deviceG);
				}).toJson();
	}
	
	@GetMapping("/tools/getfreeport")
	public String devicesGetByUserId(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
		return handle(token, request, authorizedAdmin, (x)->{
			try {
				ServerSocket sSock = new ServerSocket(0);
				Integer port = sSock.getLocalPort();
				sSock.close();
				return Response.code(ResponseCodes.GOOD_TOKEN).setData(port+"");
			} catch (IOException e) {
				return Response.error(ResponseCodes.UNKNOWN_ERROR);
			}
		}).toJson();
	}
	
	@GetMapping("/devices/getbyuserid")
	public String devicesGetByUserId(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token,
			@RequestParam(defaultValue = "none") String id) {
				return handle(token, request, authorizedUser, (x)->{
					Long userId;
					try {
						userId = Long.parseLong(id);
					} catch (Exception e) {
						userId=x.getUserId();
					}
					if(!(userService.isUserExist(userId)))
						return Response.error(ResponseCodes.USER_NOT_FOUND);
					if(!x.getUser().getAdmin())
						if(!(x.getUserId().equals(userId)))
							return Response.error(ResponseCodes.NOT_ENOUGH_RIGHTS);
					ArrayList<Device> devices = new ArrayList<>();
					if(x.getUser().getAdmin()&&x.getUserId().equals(userId)) {
						devices.addAll(deviceService.getLocalDevices());
					}
					devices.addAll(deviceService.getDevicesByUser(userId));
					List<DeviceGson> devicesGson = devices.stream()
							.map(device->{
									DeviceGson deviceGson = new DeviceGson(device);
									ConnectedDevice conDev = connectedDevicesStorage.getDevice(device.getId()); 
									if(conDev!=null) {
										deviceGson.setConnected(true);
										deviceGson.setIp(conDev.getIp());
									}
									return deviceGson;
								})
							.collect(Collectors.toList());
					return Response.code(ResponseCodes.GOOD_TOKEN)
							.setDevices(devicesGson.toArray(new DeviceGson[devicesGson.size()]));
				}).toJson();
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
		return handle(token, request, authorizedUser, (x)->{
			try {
				if(authorizedService.deleteAuthorizedById(authorizedService.getAuthorizedByToken(token).getId()));
			} catch (AuthorizedNotFoundException e) {
				//
			}
			return Response.code(ResponseCodes.LOGGED_OUT);
		}).toJson();
	}
	
	@GetMapping("/user/current")
	public String userCurrent(HttpServletRequest request,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
		return handle(token, request, authorizedUser, (x)->{
			return Response.code(ResponseCodes.GOOD_TOKEN)
					.setUserId(x.getUserId())
					.setMail(x.getUser().getMail())
					.setAdmin(x.getUser().getAdmin())
					.setLogin(x.getUser().getLogin());
		}).toJson();
	}
	
	@GetMapping("/user/current/delete")
	public String userCurrentDelete(HttpServletRequest request,
			@RequestParam(defaultValue = "none") String oldPasswordHash,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowChangeUserByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.USER_CHANGE_DISABLED);
			}
			if(!(Tools.hashSHA256(x.getUser().getLogin()+oldPasswordHash).equals(x.getUser().getPassword())))
				return Response.error(ResponseCodes.WRONG_PASS);
			List<User> admins = userService.listAdmins();
			if(admins.size()<2&&x.getUser().getAdmin()) {
				return Response.error(ResponseCodes.CANT_DELETE_LAST_ADMIN);
			} else {
				if(userService.deleteUserById(x.getUserId())) {
					longPollStorage.addEvent("users",LongPollEvent.USER_DELETED,"user deleted");
					return Response.code(ResponseCodes.USER_DELETED);
				}
				return Response.error(ResponseCodes.FAILED_TO_DELETE_USER);
			}
		}).toJson();
	}
	@GetMapping("/user/current/update")
	public String userCurrentUpdate(HttpServletRequest request,
			@RequestParam(defaultValue = "none") String login,
			@RequestParam(defaultValue = "none") String oldPasswordHash,
			@RequestParam(defaultValue = "none") String newPasswordHash,
			@CookieValue(value="raccoontoken", defaultValue = "none") String token) {
		String fLogin = login.toLowerCase().replaceAll("[^a-z0-9]","").trim();
		return handle(token, request, authorizedUser, (x)->{
			if((!settingService.getAllowChangeUserByUser())&&(!x.getUser().getAdmin())) {
				return Response.error(ResponseCodes.USER_CHANGE_DISABLED);
			}
			String oldLogin = x.getUser().getLogin();
			if(!(Tools.hashSHA256(x.getUser().getLogin()+oldPasswordHash).equals(x.getUser().getPassword())))
				return Response.error(ResponseCodes.WRONG_PASS);
			Boolean passwordUpdated=false;
			Boolean loginUpdated=false;
			if(!(x.getUser().getLogin().equals(fLogin))) {
				if(fLogin.equals("none")||fLogin.length()<5) 
					return Response.error(ResponseCodes.BAD_LOGIN);	
				if(userService.isUserExist(fLogin))
					return Response.error(ResponseCodes.USER_ALREADY_EXIST);
				try {
					userService.updateLogin(x.getUser().getId(), fLogin, oldPasswordHash);
					loginUpdated=true;
				} catch (UserNotFoundException e) {
					return Response.error(ResponseCodes.CANT_UPDATE_LOGIN);
				}
			}
			if(newPasswordHash.length()>4) {
				try {
					userService.updatePassword(x.getUserId(), newPasswordHash, false);
					passwordUpdated=true;
				} catch (UserNotFoundException e) {
					if(loginUpdated) {
						try {
							userService.updateLogin(x.getUserId(), oldLogin, oldPasswordHash);
						} catch (Exception e2) {
							//
						}
					}
					return Response.error(ResponseCodes.CANT_UPDATE_PASSWORD);
				}
			}
			User user;
			try {
				user = userService.getUserById(x.getUserId());
			} catch (UserNotFoundException e) {
				return Response.error(ResponseCodes.UNKNOWN_ERROR);
			}
			if(!(loginUpdated|passwordUpdated)) {
				return Response.code(ResponseCodes.USER_NOT_CHANGED);
			}
			longPollStorage.addEvent("users",LongPollEvent.USER_CHANGED,"user updated");
			ResponseCodes code = (loginUpdated&passwordUpdated)?(ResponseCodes.LOGIN_AND_PASSWORD_UPDATED)
					:(loginUpdated?ResponseCodes.LOGIN_UPDATED:ResponseCodes.PASSWORD_UPDATED);
			return Response.code(code).setLogin(user.getLogin());
		}).toJson();
	}
	
	@GetMapping("/register")
	public String register(
			HttpServletRequest request,
			@RequestParam(defaultValue = "none") String token,
			@RequestParam(defaultValue = "nn") String login,
			@RequestParam(defaultValue = "none") String passwordHash) {
		try {
			if(!settingService.getAllowRegister()) {
				return Response.error(ResponseCodes.REGISTRATION_DISABLED).toJson();
			}
			login=login.toLowerCase().trim();
			String cLogin = login.replaceAll("[^a-z0-9]","");
			if(cLogin.length()!=login.length()||login==null||login.length()<3||login=="nn") {
				return Response.error(ResponseCodes.BAD_LOGIN).toJson();	
			}
			if(passwordHash==null||passwordHash.length()<6||passwordHash=="none") {
				return Response.error(ResponseCodes.BAD_PASS).toJson();	
			}
			if(userService.isUserExist(login)) {
				return Response.error(ResponseCodes.USER_ALREADY_EXIST).toJson();
			}
			try {
				userService.addNewUser(login, null, passwordHash, false);
				LOG.info("User "+login+" created by "+request.getRemoteAddr());
				longPollStorage.addEvent("users",LongPollEvent.USER_ADDED,"new user created");
				return Response.code(ResponseCodes.USER_CREATED).toJson();
			} catch (Exception e) {
				return Response.error(ResponseCodes.CANT_CREATE_USER).toJson();
			}
		} catch (Exception e) {
			return Response.error(ResponseCodes.CANT_CREATE_USER).toJson();
		}
	}
	
	@GetMapping("/login")
	public String login(
			HttpServletRequest request,
			@RequestParam(defaultValue = "none") String token,
			@RequestParam(defaultValue = "nn") String login,
			@RequestParam(defaultValue = "none") String passwordHash) {
		try{
			if(login==null||login.length()<3||login=="nn") {
				return Response.error(ResponseCodes.BAD_LOGIN).toJson();	
			}
			if(passwordHash==null||passwordHash.length()<6||passwordHash=="none") {
				return Response.error(ResponseCodes.BAD_PASS).toJson();	
			}
			User user;
			user = userService.getUserByLoginOrMail(login);
			if(user.getPassword().equals(Tools.hashSHA256(user.getLogin()+passwordHash.trim()))) {
				Authorized auth = null;
				try {
					auth = authorizedService.addNewAuthorized(user.getId(), request.getRemoteAddr());
				} catch (Exception e) {
					return Response.error(ResponseCodes.CANT_GENERATE_TOKEN).toJson();		
				}
				LOG.info("User "+auth.getUser().getLogin()+" logged from "+auth.getIp());
				return Response.code(ResponseCodes.LOGGED_IN).setToken(auth.getToken()).setMaxAge(AuthorizedService.TOKEN_LIFE_SEC.toString()).toJson();
			}
			LOG.info("Unsuccessful login attempt from "+request.getRemoteAddr()+" | Login: "+login);
			return Response.error(ResponseCodes.WRONG_PASS).toJson();
		} catch (Exception e) {
			return Response.error(ResponseCodes.USER_NOT_FOUND).toJson();
		}
	}

	private Response handle(String token, HttpServletRequest request, AuthorizedChecker checker, HandlerInterface handler) {
		Response response;
		Authorized auth;
		String ip = request.getRemoteAddr();
		try {
			auth = checker.check(token, ip);
			try {
				return handler.handle(auth);
			} catch (ApiException e) {
				response = Response.error(e.getErrorCode());
			} catch (Exception e) {
				e.printStackTrace();
				response = Response.error(ResponseCodes.UNKNOWN_ERROR).setErrorType(e.getLocalizedMessage()).setErrorDesc(e.getMessage()).setException(e.getClass().getSimpleName());
			}
		} catch (ApiException te) {
			switch (te.getErrorCode()) {
				case ADMIN_REQUIRED:
					response = Response.error(ResponseCodes.ADMIN_REQUIRED);
					break;
				case TOKEN_NOT_FOUND:
					response =  Response.error(ResponseCodes.TOKEN_NOT_FOUND);
					break;
				case TOKEN_EXPIRED:
				case IP_CHANGED:
					response = Response.error(ResponseCodes.TOKEN_EXPIRED);
					break;
				default:
					response = Response.error(ResponseCodes.BAD_TOKEN);
					break;
			}
		}
		return response;
	}
}
