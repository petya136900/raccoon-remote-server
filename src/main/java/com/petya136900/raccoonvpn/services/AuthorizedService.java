package com.petya136900.raccoonvpn.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petya136900.raccoonvpn.entitys.Authorized;
import com.petya136900.raccoonvpn.entitys.User;
import com.petya136900.raccoonvpn.exceptions.AuthorizedNotFoundException;
import com.petya136900.raccoonvpn.exceptions.UserNotFoundException;
import com.petya136900.raccoonvpn.repositories.AuthorizedRepository;
import com.petya136900.raccoonvpn.tools.Tools;

@Service
public class AuthorizedService {
	
	public static final Long TOKEN_LIFE_SEC = (long) (7*24*60*60);
	
	@Autowired
	private AuthorizedRepository authorizedRepository;
	
	@Autowired
	private UserService userService;
	
	public List<Authorized> list() {
		return authorizedRepository.findAll();
	}
    public Authorized getAuthorizedById(Long id) throws AuthorizedNotFoundException {
    	Optional<Authorized> opt = authorizedRepository.findById(id);
    	if(opt.isPresent()) {
    		return opt.get();
    	} 
    	throw new AuthorizedNotFoundException();
    }	
    public Authorized getAuthorizedByToken(String token) throws AuthorizedNotFoundException {
    	List<Authorized> list = authorizedRepository.findByToken(token);
    	if (list!=null&&list.size()>0)
			return list.get(0);
		throw new AuthorizedNotFoundException();
    }
    public boolean isAuthorizedExist(Long userId) {
    	try {
    		getAuthorizedById(userId);
    		return true;
    	} catch (AuthorizedNotFoundException e) {
    		return false;
		}
    }
    public boolean isAuthorizedExistByToken(String token) {
    	try {
    		getAuthorizedByToken(token);
    		return true;
    	} catch (AuthorizedNotFoundException e) {
    		return false;
		}
    }
    private String generateUnicToken() {
    	String token = null;
    	Integer count = 0;
    	do {
	    	token = Tools.generateToken();
	    	if(isAuthorizedExistByToken(token)) {
	    		token = null;
	    		count++;
	    	}
    	} while(token==null&&count<5);
    	if(token==null)
    		throw new IllegalStateException("Bad token");
    	return token;
    }
    public Authorized addNewAuthorized(Long userId, String ip) throws UserNotFoundException {
    	User user = userService.getUserById(userId);
    	String token = generateUnicToken();
    	Authorized authorized = new Authorized();
    	authorized.setToken(token);
    	authorized.setIp(ip);
    	authorized.setExpires_in(Tools.getExpiresIn()+"");
    	authorized.setUser(user);
    	return authorizedRepository.saveAndFlush(authorized);
    }
    /** 
     * @return true if user deleted</br>
     * false if user don't exist
     */
    public Boolean deleteAuthorizedById(Long id) {
    	if(isAuthorizedExist(id)) {
    		authorizedRepository.deleteById(id);
    		return true;
    	}
    	return false;
    }
	public Authorized updateToken(Long authId) throws AuthorizedNotFoundException {
		Authorized authorized = getAuthorizedById(authId);
		String token = generateUnicToken();
		authorized.setToken(token);
		// SET +N expires_in
		authorized.setExpires_in(Tools.getExpiresIn()+"");
    	return authorizedRepository.saveAndFlush(authorized);
	}
	public Authorized getAuthorizedAgentByUserIdAndIp(Long userId, String ip) throws UserNotFoundException {
		Authorized auth = null;
		User user = userService.getUserById(userId); 
		if(auth==null) {
			auth = new Authorized();
			auth.setUser(user);
			auth.setAgent(true);
			auth.setIp(ip);
			auth.setToken(Tools.generateToken());
			auth = authorizedRepository.saveAndFlush(auth);
		} 
		return auth;
	}
	public Boolean removeAuthorizedAgentByUserId(Long userId, String hostAddress) {
		Authorized auth;
		try {
			auth = getAuthorizedAgentByUserIdAndIp(userId, hostAddress);
		} catch (UserNotFoundException e) {
			return false;
		}
		return deleteAuthorizedById(auth.getId());
	}
	public Boolean revokeToken(String token) {
		Authorized auth;
		auth = getAuthorizedAgentByToken(token);
		if(auth!=null) 
			return deleteAuthorizedById(auth.getId());
		return false;
	}
	private Authorized getAuthorizedAgentByToken(String token) {
		List<Authorized> auths = authorizedRepository.getByToken(token);
		if(auths!=null&&auths.size()>0)
			return auths.get(0);
		return null;
	}
	public boolean checkAuthorizedByIp(String hostAddress) {
		List<Authorized> auths = authorizedRepository.getByIp(hostAddress);
		if(auths!=null&&auths.size()>0)
			return true;
		return false;				
	}
	public boolean checkAuthorizedByIpAndUserId(String ip, Long userId) {
		List<Authorized> auths = authorizedRepository.getByIpAndUserId(ip,userId);
		if(auths!=null&&auths.size()>0)
			return true;
		return false;	
	}
}
