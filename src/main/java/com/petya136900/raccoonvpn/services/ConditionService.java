package com.petya136900.raccoonvpn.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.exceptions.ConditionAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.ConditionNotFoundException;
import com.petya136900.raccoonvpn.repositories.ConditionRepository;

@Service
public class ConditionService {
	@Autowired
	ConditionRepository conditionRepository;
	
	public List<Condition> list() {
		return conditionRepository.findAll();
	}
	public Condition getConditionById(Long id) throws ConditionNotFoundException {
    	Optional<Condition> opt = conditionRepository.findById(id);
    	if(opt.isPresent()) {
    		return opt.get();
    	} 
    	throw new ConditionNotFoundException();
    }	
    /** 
     * @return true if Condition deleted</br>
     * false if Condition don't exist
     */
    public Boolean deleteConditionById(Long id) {
    	if(isConditionExist(id)) {
    		conditionRepository.deleteById(id);
    		return true;
    	}
    	return false;
    }
    public Boolean isConditionExist(Long id) {
    	try {
    		getConditionById(id);
    		return true;
    	} catch (ConditionNotFoundException e) {
    		return false;
		}
    }
	public List<Condition> getConditionsByDevice(Long deviceId) {
		return conditionRepository.findByDevId(deviceId);
	}
	public Condition addNewCondition(Condition condition) throws ConditionAlreadyExistException {
		return conditionRepository.saveAndFlush(condition);
	}
	public Condition updateCondition(Condition condition) {
		return conditionRepository.saveAndFlush(condition);
	}
}
