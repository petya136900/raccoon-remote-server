package com.petya136900.raccoonvpn.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petya136900.raccoonvpn.entitys.Device;
import com.petya136900.raccoonvpn.exceptions.DeviceAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotSpecifiedException;
import com.petya136900.raccoonvpn.repositories.DeviceRepository;

@Service
public class DeviceService {
	@Autowired
	DeviceRepository deviceRepository;
	
	public List<Device> list() {
		return deviceRepository.findAll();
	}
	public Device getDeviceById(Long id) throws DeviceNotFoundException {
    	Optional<Device> opt = deviceRepository.findById(id);
    	if(opt.isPresent()) {
    		return opt.get();
    	} 
    	throw new DeviceNotFoundException();
    }	
    /** 
     * @return true if device deleted</br>
     * false if device don't exist
     */
    public Boolean deleteDeviceById(Long id) {
    	if(isDeviceExist(id)) {
    		deviceRepository.deleteById(id);
    		return true;
    	}
    	return false;
    }
    public boolean isDeviceExistByUserIdAndDevId(Long userId, String devId) {
    	try {
    		getDeviceByUserAndDevId(userId, devId);
    		return true;
    	} catch (DeviceNotFoundException e) {
    		return false;
		}
    }
	public boolean isDeviceExist(Long id) {
		try {
    		getDeviceById(id);
    		return true;
    	} catch (DeviceNotFoundException e) {
    		return false;
		}
	}
	public List<Device> getDevicesByUser(Long userId) {
		return deviceRepository.findByUserId(userId);
	}
	public Device getDeviceByUserAndDevId(Long userId, String deviceId) throws DeviceNotFoundException {
		List<Device> list = deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
		if (list!=null&&list.size()>0)
			return list.get(0);
		throw new DeviceNotFoundException();
	}
	public Device addNewDevice(Device device) throws DeviceAlreadyExistException, UserNotSpecifiedException {
		if(device.getUser()==null) 
			throw new UserNotSpecifiedException();
		Long userId = device.getUser().getId();
		String devId = device.getDeviceId();
		if(isDeviceExistByUserIdAndDevId(userId, devId))
			throw new DeviceAlreadyExistException();
		return deviceRepository.saveAndFlush(device);
	}
	public List<Device> getLocalDevices() {
		return deviceRepository.findAllLocals();
	}
	public Device updateDeviceName(Long id, String newName) throws DeviceNotFoundException {
		Device device = getDeviceById(id);
		device.setName(newName);
		return deviceRepository.saveAndFlush(device);
	}
}
