package com.petya136900.raccoonvpn.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.exceptions.UserAlreadyExistException;
import com.petya136900.raccoonvpn.exceptions.UserNotFoundException;
import com.petya136900.raccoonvpn.repositories.UserRepository;
import com.petya136900.raccoonvpn.tools.Tools;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
	public List<User> list() {
		return userRepository.findAll();
	}
	public List<User> listAdmins() {
		return userRepository.findAllAdmins();
	}
    public User getUserById(Long id) throws UserNotFoundException {
    	Optional<User> opt = userRepository.findById(id);
    	if(opt.isPresent()) {
    		return opt.get();
    	} 
    	throw new UserNotFoundException();
    }	
    public User getUserByLoginOrMail(String loginOrMail) throws UserNotFoundException {
    	try {
    		loginOrMail = loginOrMail.toLowerCase();
    		return getUserByLogin(loginOrMail);
    	} catch (Exception e) {
    		return getUserByMail(loginOrMail);
		}
    }
    public User getUserByLogin(String login) throws UserNotFoundException {
    	if(login!=null)
    		login = login.toLowerCase();
    	List<User> users = userRepository.findByLogin(login);
    	if (users!=null&&users.size()>0)
			return users.get(0);
		throw new UserNotFoundException();
    }
    public User getUserByMail(String mail) throws UserNotFoundException {
    	if(mail!=null)
    		mail = mail.toLowerCase();
    	List<User> users = userRepository.findByMail(mail);
    	if (users!=null&&users.size()>0)
			return users.get(0);
		throw new UserNotFoundException();
    }
    public boolean isUserExist(Long userId) {
    	try {
    		getUserById(userId);
    		return true;
    	} catch (UserNotFoundException e) {
    		return false;
		}
    }
    public boolean isUserExist(String loginOrMail) {
    	if(loginOrMail!=null)
    		loginOrMail = loginOrMail.toLowerCase();
    	if(loginOrMail==null)
    		return false;
    	try {
    		getUserByLoginOrMail(loginOrMail);
    		return true;
    	} catch (UserNotFoundException e) {
    		return false;
		}
    }
    public User addNewUser(User user) throws UserAlreadyExistException {
    	if(isUserExist(user.getLogin())|isUserExist(user.getMail()))
    		throw new UserAlreadyExistException();
    	user.setLogin(user.getLogin().toLowerCase());
    	user.setPassword(Tools.hashSHA256(user.getLogin()+Tools.hashSHA256(user.getPassword())));
    	return userRepository.saveAndFlush(user);
    }
    public User addNewUser(String login, String mail, String password, boolean needHash) throws UserAlreadyExistException {
    	if(isUserExist(login)|isUserExist(mail))
    		throw new UserAlreadyExistException();
    	User user = new User();
    	user.setLogin(login.toLowerCase());
    	user.setMail(mail);
    	user.setPassword(Tools.hashSHA256(user.getLogin()+(needHash?Tools.hashSHA256(password):password)));
    	return userRepository.saveAndFlush(user);
    }
    public User updateMail(Long id, String newMail) throws UserNotFoundException {
    	User user = getUserById(id);
    	user.setMail(newMail);
    	return userRepository.saveAndFlush(user);
    }
    /** 
     * @return true if user deleted</br>
     * false if user don't exist
     */
    public Boolean deleteUserById(Long id) {
    	if(isUserExist(id)) {
    		userRepository.deleteById(id);
    		return true;
    	}
    	return false;
    }
	public User updatePassword(Long id, String newPass, Boolean needHash) throws UserNotFoundException {
		User user = getUserById(id);
		user.setPassword(Tools.hashSHA256(user.getLogin()+(needHash?(Tools.hashSHA256(newPass)):newPass)));
    	return userRepository.saveAndFlush(user);
	}
	public User updateLogin(Long id, String login, String oldPasswordHash) throws UserNotFoundException {
		User user = getUserById(id);
		user.setLogin(login);
		user.setPassword(Tools.hashSHA256(login+oldPasswordHash));
		return userRepository.saveAndFlush(user);
	}
}
