package com.petya136900.raccoonvpn.gson;

import com.petya136900.raccoonvpn.entitys.User;

public class UserGson {
	public UserGson(User user) {
		this.id=user.getId();
		this.login=user.getLogin();
		this.mail=user.getMail();
		this.admin=user.getAdmin();
	}
	
	private Long id;
	private String login;
	private String mail;
	private Boolean admin;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public Boolean getAdmin() {
		return admin;
	}
	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
}
