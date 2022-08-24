package com.petya136900.raccoonvpn.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petya136900.raccoonvpn.entitys.Setting;
import com.petya136900.raccoonvpn.repositories.SettingRepository;

@Service
public class SettingService {
	
    @Autowired
    private SettingRepository settingRepository;

    public List<Setting> list() {
        return settingRepository.findAll();
    }
    public Setting getSettingById(Long id) {
    	Optional<Setting> opt = settingRepository.findById(id);
    	if(opt.isPresent()) {
    		return opt.get();
    	} 
    	return null;
    }
    public List<Setting> getSettingsByName(String name) {
    	return settingRepository.findByName(name);
    }
    public void delete(Setting setting) {
    	settingRepository.delete(setting);
    }
    public void deleteById(Long id) {
    	settingRepository.deleteById(id);
    }
    public void deleteByName(String name) {
    	getSettingsByName(name).forEach(setting->{
    		settingRepository.delete(setting); 
    	});
    }
    public Setting updateOrPut(Setting setting) {
    	return settingRepository.saveAndFlush(setting);
    }
    public Setting updateById(Long id, Setting newSetting) {
    	Setting s = getSettingById(id);
    	if(s==null)
    		return null;
    	s.setName(newSetting.getName());
    	s.setValue(newSetting.getValue());
    	return settingRepository.saveAndFlush(s);
    }
    public Setting updateSettingByName(String name, String value) {
    	Setting setting = getSettingByName(name);
    	if(setting==null)
    		setting = new Setting();
    	setting.setName(name);
    	setting.setValue(value);
    	return updateOrPut(setting);
    }
    public Setting getSettingByName(String name) {
    	List<Setting> list = settingRepository.findByName(name);
    	return (list!=null&&list.size()>0?list.get(0):null);
    }
    public boolean getAllowRegister() {
    	Setting setting = getSettingByName("allow-register");
    	if((setting!=null)&&setting.getValue().equals("1")) {    		
    		return true;
    	}
    	return false;
    }
    public boolean getAllowChangeUserByUser() {
    	Setting setting = getSettingByName("allow-change-user-by-user");
    	if((setting!=null)&&setting.getValue().equals("1")) {    		
    		return true;
    	}
    	return false;
    }
    public boolean getAllowCreateRulesByUser() {
    	Setting setting = getSettingByName("allow-create-rules-by-user");
    	if((setting!=null)&&setting.getValue().equals("1")) {    		
    		return true;
    	}
    	return false;
    }
	public Setting allowRegister(boolean b) {
		return updateSettingByName("allow-register", b?"1":"0");
	}
	public Setting allowChangeUserByUser(boolean b) {
		return updateSettingByName("allow-change-user-by-user", b?"1":"0");
	}
	public Setting allowCreateRulesByUser(boolean b) {
		return updateSettingByName("allow-create-rules-by-user", b?"1":"0");
	}
	public Setting setAgentsPort(Integer port) {
		return updateSettingByName("agents-port", port+"");
	}
	public String getAgentsPort() {
		Setting setting = getSettingByName("agents-port");
			if(setting==null)
				return null;
		return setting.getValue();
	}
}