package com.petya136900.raccoonvpn.agent;
import java.util.Arrays;
public class Data {
	private String login;
	private String passwordHash;
	private String token;
	private String devId;
	private String devName;
	private String localIp;
	private String host;
	private Integer port;
	private String taskId;
	private Long userId;
	private String asker;
	private String serverHost;
	private String serverIp;
	private Integer serverWebPort;
	private Integer serverForwarderPort;
	//
	private Integer tTimeout;
	private String tIp;
	private String tMask;
	private String data;
	//
	private byte[] code;
	public Data(byte[] code) {
		this.setCode(code);
	}
	public byte[] getCode() {
		return code;
	}
	public void setCode(byte[] code) {
		this.code = code;
	}
	public String getLogin() {
		return login;
	}
	public Data setLogin(String login) {
		this.login = login;
		return this;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public Data setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
		return this;
	}
	public String getToken() {
		return token;
	}
	public Data setToken(String token) {
		this.token = token;
		return this;
	}
	public String getDevId() {
		return devId;
	}
	public Data setDevId(String devId) {
		this.devId = devId;
		return this;
	}
	public String getDevName() {
		return devName;
	}
	public Data setDevName(String devName) {
		this.devName = devName;
		return this;
	}
	public String getLocalIp() {
		return localIp;
	}
	public Data setLocalIp(String localIp) {
		this.localIp = localIp;
		return this;
	}
	public String getHost() {
		return host;
	}
	public Data setHost(String host) {
		this.host = host;
		return this;
	}
	public Integer getPort() {
		return port;
	}
	public Data setPort(Integer port) {
		this.port = port;
		return this;
	}
	public String getTaskId() {
		return taskId;
	}
	public Data setTaskId(String taskId) {
		this.taskId = taskId;
		return this;
	}
	public String getServerHost() {
		return serverHost;
	}
	public Data setServerHost(String serverHost) {
		this.serverHost = serverHost;
		return this;
	}
	public String getServerIp() {
		return serverIp;
	}
	public Data setServerIp(String serverIp) {
		this.serverIp = serverIp;
		return this;
	}
	public Integer getServerWebPort() {
		return serverWebPort;
	}
	public Data setServerWebPort(Integer serverWebPort) {
		this.serverWebPort = serverWebPort;
		return this;
	}
	public Integer getServerForwarderPort() {
		return serverForwarderPort;
	}
	public Data setServerForwarderPort(Integer serverForwarderPort) {
		this.serverForwarderPort = serverForwarderPort;
		return this;
	}
	public boolean isEmpty() {
		if(login==null&&
		passwordHash==null&&
		token==null&&
		devId==null&&
		devName==null&&
		localIp==null&&
		host==null&&
		port==null&&
		taskId==null&&
		serverHost==null&&
		serverIp==null&&
		serverWebPort==null&&
		serverForwarderPort==null)
			return true;
		return false;
	}
	public static Data code(byte[] code) {
		Data data = new Data(code);
		return data;
	}
	public String getCodeName() {
		return this.code==null?"null":Codes.getCodeName(this.getCode());
	}
	public boolean codeEqual(byte[] code) {
		return Arrays.equals(this.code, code);
	}
	public Long getUserId() {
		return userId;
	}
	public Data setUserId(Long userId) {
		this.userId = userId;
		return this;
	}
	public String getAsker() {
		return asker;
	}
	public Data setAsker(String asker) {
		this.asker = asker;
		return this;
	}
	public Integer gettTimeout() {
		return tTimeout;
	}
	public Data settTimeout(Integer tTimeout) {
		this.tTimeout = tTimeout;
		return this;
	}
	public String gettIp() {
		return tIp;
	}
	public Data settIp(String tIp) {
		this.tIp = tIp;
		return this;
	}
	public String gettMask() {
		return tMask;
	}
	public Data settMask(String tMask) {
		this.tMask = tMask;
		return this;
	}
	public String getData() {
		return data;
	}
	public Data setData(String data) {
		this.data = data;
		return this;
	}
}
