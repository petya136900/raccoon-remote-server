package com.petya136900.raccoonvpn.tools;

import com.petya136900.raccoonvpn.forward.LoadedCondition;
import com.petya136900.raccoonvpn.gson.DeviceGson;
import com.petya136900.raccoonvpn.gson.SettingGson;
import com.petya136900.raccoonvpn.gson.UserGson;
import com.petya136900.raccoonvpn.longpolling.LongPollEvent;
import com.petya136900.raccoonvpn.rest.v1.codes.ResponseCodes;

public class Response {
	private Boolean error=false;
	private String errorType;
	private String errorDesc;
	private String exception;
	private String data;
	private Long code;
	private String codeDesc;
	private String codeDescRu;
	private Long id;
	private String raccoonToken;
	private String maxAge;
	private Boolean admin;
	private Long userId;
	private String login;
	private String mail;
	private LoadedCondition[] conditions;
	private DeviceGson device;
	private DeviceGson[] devices;
	private UserGson[] users;
	private LongPollEvent event;
	private SettingGson settings;
	private String[] list;
	private Boolean done;
	
	public Boolean getError() {
		return error;
	}
	public Response setError(Boolean error) {
		this.error = error;
		return this;
	}
	public String getErrorType() {
		return errorType;
	}
	public Response setErrorType(String errorType) {
		this.errorType = errorType;
		return this;
	}
	public String getErrorDesc() {
		return errorDesc;
	}
	public Response setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
		return this;
	}
	public String getData() {
		return data;
	}
	public Response setData(String data) {
		this.data = data;
		return this;
	}
	public Long getCode() {
		return code;
	}
	public Response setCode(ResponseCodes code) {
		this.code = code.getCode();
		this.codeDesc=code.toString();
		this.codeDescRu=code.getDescRu();
		return this;
	}
	public static Response data(String data) {
		Response response = new Response();
		response.setData(data);
		return response;
	}
	public static Response code(ResponseCodes code) {
		Response response = new Response();
		response.code = code.getCode();
		response.codeDesc=code.toString();
		response.codeDescRu=code.getDescRu();
		return response;
	}
	public static Response error(ResponseCodes code) {
		Response response = new Response();
		response.setError(true);
		response.code = code.getCode();
		response.codeDesc=code.toString();
		response.codeDescRu=code.getDescRu();
		return response;
	}
	public String toJson() {
		return JsonParser.toJson(this);
	}
	public String getCodeDesc() {
		return codeDesc;
	}
	public Response setCodeDesc(String codeDesc) {
		this.codeDesc = codeDesc;
		return this;
	}
	public Long getId() {
		return id;
	}
	public Response setId(Long id) {
		this.id = id;
		return this;
	}
	public String getToken() {
		return raccoonToken;
	}
	public Response setToken(String token) {
		this.raccoonToken = token;
		return this;
	}
	public String getMaxAge() {
		return maxAge;
	}
	public Response setMaxAge(String maxAge) {
		this.maxAge = maxAge;
		return this;
	}
	public Boolean getAdmin() {
		return admin;
	}
	public Response setAdmin(Boolean admin) {
		this.admin = admin;
		return this;
	}
	public Long getUserId() {
		return userId;
	}
	public Response setUserId(Long userId) {
		this.userId = userId;
		return this;
	}
	public String getLogin() {
		return login;
	}
	public Response setLogin(String login) {
		this.login = login;
		return this;
	}
	public String getMail() {
		return mail;
	}
	public Response setMail(String mail) {
		this.mail = mail;
		return this;
	}
	public String getCodeDescRu() {
		return codeDescRu;
	}
	public Response setCodeDescRu(String codeDescRu) {
		this.codeDescRu = codeDescRu;
		return this;
	}
	public DeviceGson[] getDevices() {
		return devices;
	}
	public Response setDevices(DeviceGson[] deviceGsons) {
		this.devices = deviceGsons;
		return this;
	}
	public UserGson[] getUsers() {
		return users;
	}
	public Response setUsers(UserGson[] users) {
		this.users = users;
		return this;
	}
	public LongPollEvent getEvent() {
		return event;
	}
	public Response setEvent(LongPollEvent event) {
		this.event = event;
		return this;
	}
	public String getException() {
		return exception;
	}
	public Response setException(String exception) {
		this.exception = exception;
		return this;
	}
	public LoadedCondition[] getConditions() {
		return conditions;
	}
	public Response setConditions(LoadedCondition[] conditions) {
		this.conditions=conditions;
		return this;
	}
	public DeviceGson getDevice() {
		return device;
	}
	public Response setDevice(DeviceGson device) {
		this.device = device;
		return this;
	}
	public SettingGson getSettings() {
		return settings;
	}
	public Response setSettings(SettingGson settings) {
		this.settings = settings;
		return this;
	}
	public String[] getList() {
		return list;
	}
	public Response setList(String[] list) {
		this.list=list;
		return this;
	}
	public boolean isDone() {
		return this.done;
	}
	public Response setDone(boolean done) {
		this.done=done;
		return this;
	}
}
