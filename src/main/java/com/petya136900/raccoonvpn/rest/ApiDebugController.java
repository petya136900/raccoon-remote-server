package com.petya136900.raccoonvpn.rest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.petya136900.raccoonvpn.RaccoonVPNServer;
import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.entitys.Setting;
import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.exceptions.AuthorizedNotFoundException;
import com.petya136900.raccoonvpn.exceptions.ConditionNotFoundException;
import com.petya136900.raccoonvpn.exceptions.DeviceAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotSpecifiedException;
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
@Profile("debug")
@RequestMapping(value = "/api",method = RequestMethod.GET)
public class ApiDebugController {
	@Autowired
	private UserService userService;
	@Autowired
	private SettingService settingService;
	@Autowired
	private DeviceService deviceService;
	//@Autowired
	//private RuleService ruleService;
	@Autowired
	private ConditionService conditionService;
	@Autowired
	private AuthorizedService authorizedService;
	
	@Autowired
	private TestClass testClass;
	
	@GetMapping("/tool/stop")
	public String toolsStop(HttpServletResponse response,
			@RequestParam(value = "token", defaultValue = "none") String token) {
		return RaccoonVPNServer.stop()+"";
	}
	
	@GetMapping("/test/cookie")
	public String testCookie(HttpServletResponse response,
			@RequestParam(value = "token", defaultValue = "none") String token) {
		if(token.equals("none")) {
			token = Tools.generateToken();
		}
		return JsonParser.toJson(new Response().setData(token));
	}
	
	@GetMapping("/tool/time")
	public Long getTime() {
		return Tools.getCurrentSecs();
	}
	
	@GetMapping("/user/getall")
    public List<User> getAllUsers() {
        return userService.list();
    }
	@GetMapping("/device/getall")
    public List<Device> getAllDevices() {
        return deviceService.list();
    }
	/*
	@GetMapping("/rule/getall")
    public List<Rule> getAllRules() {
        return ruleService.list();
    }
    */
	@GetMapping("/condition/getall")
    public List<Condition> getAllConditions() {
        return conditionService.list();
    }
	@GetMapping("/setting/getall")
	public List<Setting> getAllSettings() {
		testClass.testMethod();
		return settingService.list();
	} 
	@GetMapping("/authorized/getall")
	public List<Authorized> getAllAuthorized() {
		return authorizedService.list();
	}
	// get by id
	@GetMapping("/user/getbyid/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        try {
			return userService.getUserById(id);
		} catch (UserNotFoundException e) {
			return null;
		}
    }
	@GetMapping("/device/getbyid/{id}")
    public Device getDeviceById(@PathVariable("id") Long id) {
		try {
			return deviceService.getDeviceById(id);
		} catch (DeviceNotFoundException e) {
			return null;
		}
    }
	/*
	@GetMapping("/rule/getbyid/{id}")
    public Rule getRuleById(@PathVariable("id") Long id) {
        try {
			return ruleService.getRuleById(id);
		} catch (RuleNotFoundException e) {
			return null;
		}
    }
    */
	@GetMapping("/condition/getbyid/{id}")
    public Condition getConditionById(@PathVariable("id") Long id) {
		try {
			return conditionService.getConditionById(id);
		} catch (ConditionNotFoundException e) {
			return null;
		}
    }
	@GetMapping("/authorized/getbyid/{id}")
    public Authorized getAuthorizedById(@PathVariable("id") Long id) {
		try {
			return authorizedService.getAuthorizedById(id);
		} catch (AuthorizedNotFoundException e) {
			return null;
		}
    }
	// add
	@GetMapping("/user/add/{login}/{pass}")
    public String addUser(@PathVariable("login") String login,@PathVariable("pass") String pass) {
    	User user = new User();
    	user.setLogin(login);
    	user.setPassword(pass);
    	try {
    		User newUser = userService.addNewUser(user);
    		return "User "+newUser.getLogin()+" added with ID: "+newUser.getId();
    	} catch (Exception e) {
    		return "Error: "+e.getLocalizedMessage();
		}
    }
	@GetMapping("/device/add/{userId}/{name}")
    public String addDevice(@PathVariable("userId") Long userId,@PathVariable("name") String name) {
		User user=null;
		try {
			user = userService.getUserById(userId);
		} catch (Exception e) {
			return "Error, user with id '"+userId+"'not found";
		}
    	Device device = new Device();
    	device.setName(name);
    	device.setUser(user);
    	try {
			deviceService.addNewDevice(device);
		}  catch (DeviceAlreadyExistException e) {
			return "Error, this device already added";
		} catch (UserNotSpecifiedException e) {
			return "Error, user not specified";
		}
    	return "Device added!";
    }
	@GetMapping("/setting/add/{name}")
    public String addSetting(@PathVariable("name") String name) {
		Setting setting = settingService.getSettingByName(name);
		if(setting!=null) {
			setting.setValue("");
			settingService.updateOrPut(setting);
		} else {
			setting = new Setting();
			setting.setName(name);
			setting.setValue("");
			settingService.updateOrPut(setting);
		}
		return "Setting "+name+" added!";
	}
	/*
	@GetMapping("/rule/add/{devId}/{name}")
	public String addRule(@PathVariable("devId") Long devId,@PathVariable("name") String name) {
		Device device = null;
		try {
			device = deviceService.getDeviceById(devId);
		} catch (Exception e) {
			return "Error, device with id '"+devId+"'not found";
		}
    	Rule rule = new Rule();
    	rule.setName(name);
    	rule.setDevice(device);
    	try {
			ruleService.addNewRule(rule);
		}  catch (RuleAlreadyExistException e) {
			return "Error, this rule already added";
		} catch (DeviceNotSpecifiedException e) {
			return "Error, device not specified";
		}
    	return "Rule added!";
    }
    */
	/*
	@GetMapping("/condition/add/{ruleId}/{protocol}/{type}/{data}")
    public String addCondition(@PathVariable("ruleId") Long ruleId,
    		@PathVariable("protocol") String protocol,
    		@PathVariable("type") String type,
    		@PathVariable("data") String data) {
		Rule rule = null;
		try {
			rule = ruleService.getRuleById(ruleId);
		} catch (Exception e) {
			return "Error, rule with id '"+ruleId+"'not found";
		}
		Protocol p = null;
		ConditionType t = null;
		try {
			p = Protocol.valueOf(protocol);
		} catch (Exception e) {
			return "Error! Bad protocol, only TCP\\UDP allowed";
		}
		try {
			t = ConditionType.valueOf(type);
		} catch (Exception e) {
			return "Error! Bad Type";
		}
    	Condition condition = new Condition();
    	condition.setRule(rule);
    	condition.setProtocol(p);
    	condition.setConditionType(t);
    	condition.setData(data);
    	try {
	    	conditionService.addNewCondition(condition);
	    	return "Condition added";
    	} catch (Exception e) {
    		return "Error: "+e.getMessage();
		}
    }
    */
	@GetMapping("/authorized/deleteall")
	public String deleteAllAuths() {
		List<Authorized> auths = authorizedService.list();
		for(Authorized auth : auths) {
			authorizedService.deleteAuthorizedById(auth.getId());
		}
		return "Deleted!";
	}
	@GetMapping("/authorized/add/{userId}")
    public String addSetting(@PathVariable("userId") Long userId) {
		try {
			Authorized authorized = authorizedService.addNewAuthorized(userId,"ip");
			return "Token '"+authorized.getToken()+"' for user["+authorized.getUser().getLogin()+"] created";
		} catch (UserNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		} catch (Exception e) {
			return "Error: "+e.getMessage();
		}
	}
	// update
	@GetMapping("/user/update/mail/{id}/{newMail}")
	public String updateMail(@PathVariable("id") Long id,@PathVariable("newMail") String newMail) {
		try {
			userService.updateMail(id, newMail);
			return "Updated";
		} catch (UserNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/testt")
	public ModelAndView index () {
	    ModelAndView modelAndView = new ModelAndView();
	    modelAndView.setViewName("login");
	    return modelAndView;
	}
	
	@GetMapping("/user/update/password/{id}/{newPass}")
	public String updatePassword(@PathVariable("id") Long id,@PathVariable("newPass") String newPass) {
		try {
			userService.updatePassword(id, newPass,true);
			return "Updated";
		} catch (UserNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
	@GetMapping("/device/update/name/{id}/{newName}")
	public String updateDevName(@PathVariable("id") Long id,@PathVariable("newName") String newName) {
		try {
			deviceService.updateDeviceName(id, newName);
			return "Device updated";
		} catch (DeviceNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
	/*
	@GetMapping("/rule/update/name/{id}/{newName}")
	public String updateRuleName(@PathVariable("id") Long id,@PathVariable("newName") String newName) {
		try {
			ruleService.getRuleById(id);
			try {
				ruleService.updateRuleName(id, newName);
				return "Rule updated";
			} catch (RuleAlreadyExistException e) {
				return "Error: "+e.getLocalizedMessage();
			}
		} catch (RuleNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
	*/
	/*
	@GetMapping("/condition/update/{id}/{protocol}/{type}/{data}")
	public String updateCondition(@PathVariable("id") Long id,
			@PathVariable("protocol") String protocol,
    		@PathVariable("type") String type,
    		@PathVariable("data") String data) {
		Protocol p = null;
		ConditionType t = null;
		try {
			p = Protocol.valueOf(protocol);
		} catch (Exception e) {
			return "Error! Bad protocol, only TCP\\UDP allowed";
		}
		try {
			t = ConditionType.valueOf(type);
		} catch (Exception e) {
			return "Error! Bad Type";
		}
		Condition newCondition = new Condition();
		newCondition.setProtocol(p);
		newCondition.setConditionType(t);
		newCondition.setData(data);
		try {
			conditionService.getConditionById(id);
			try {
				conditionService.updateCondition(id, newCondition);
				return "Rule updated";
			} catch (Exception e) {
				return "Error: "+e.getLocalizedMessage();
			}
		} catch (ConditionNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
	*/
	@GetMapping("/authorized/update/{id}")
	public String updateToken(@PathVariable("id") Long id) {
		try {
			authorizedService.updateToken(id);
			return "Token updated!";
		} catch (AuthorizedNotFoundException e) {
			return "Error: "+e.getLocalizedMessage();
		}
	}
	// set
	@GetMapping("/setting/set/{name}/{value}")
	public String setSetting(@PathVariable("name") String name,@PathVariable("value") String value) {
		Setting setting = settingService.getSettingByName(name);
		if(setting!=null) {
			setting.setValue(value);
			settingService.updateOrPut(setting);
		} else {
			setting = new Setting();
			setting.setName(name);
			setting.setValue(value);
			settingService.updateOrPut(setting);
		}
		return "The "+name+" setting value is set to "+value;
	}
	// delete
	@GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
		if(userService.deleteUserById(id)) {
			return "User deleted";	
		}
		return "User don't exist";
    }
	@GetMapping("/device/delete/{id}")
    public String deleteDevice(@PathVariable("id") Long id) {
		if(deviceService.deleteDeviceById(id))
			return "Device deleted";
		return "Device don't exist";
    }
	/*
	@GetMapping("/rule/delete/{id}")
    public String deleteRule(@PathVariable("id") Long id) {
		if(ruleService.deleteRuleById(id))
			return "Rule deleted";
		return "Rule don't exist";
    }
    */
	@GetMapping("/condition/delete/{id}")
    public String deleteCondition(@PathVariable("id") Long id) {
		if(conditionService.deleteConditionById(id))
			return "Condition deleted";
		return "Condition don't exist";
    }
	@GetMapping("/setting/delete/{id}")
	public String deleteSetting(@PathVariable("id") Long id) {
    	Setting setting =  settingService.getSettingById(id);
    	if(setting==null) 
    		return "Setting doesn't exist";
    	settingService.delete(setting);
    	return "Setting deleted";
    }
	@GetMapping("/authorized/delete/{id}")
    public String deleteToken(@PathVariable("id") Long id) {
		if(authorizedService.deleteAuthorizedById(id))
			return "Token deleted";
		return "Token don't exist";
    }
	
	@GetMapping("/localport")
	public String localPort() {
		return RaccoonVPNServer.HTTPS_PORT_IF_BUSY+"";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/showmeinfoaboutpath")
	public @ResponseBody String index2(HttpServletRequest request) {
		return "Hello!</br></br>"
				+ "getPathInfo: "+request.getPathInfo()+"</br>"
				+ "getPathTranslated: "+request.getPathTranslated()+"</br>"
				+ "getContextPath: "+request.getContextPath()+"</br>"
				+ "getQueryString: "+request.getQueryString()+"</br>"
				+ "getRequestURI: "+request.getRequestURI()+"</br>"
				+ "getServletPath: "+request.getServletPath()+"</br></br>"+
				Collections.list(request.getParameterNames()).stream().map(x->(x+" - "+request.getParameter(x))).collect(Collectors.joining(" | "));
	}	
}
