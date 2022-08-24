package com.petya136900.raccoonvpn.gson;

import com.petya136900.raccoonvpn.RaccoonVPNServer;

public class SettingGson {
	
	private String certCN = RaccoonVPNServer.certCN;
	private String certIssuer = RaccoonVPNServer.certIssuer;
	private boolean selfSigned = RaccoonVPNServer.certSelfSigned;
	private Integer agentsPort = RaccoonVPNServer.agentsPort;
	private boolean allowRegister = false;
	private boolean allowCUBU = false;
	private boolean allowCRBU = false;
	private Integer webPort = RaccoonVPNServer.HTTPS_PORT;
	public String getCertCN() {
		return certCN;
	}
	public void setCertCN(String certCN) {
		this.certCN = certCN;
	}
	public String getCertIssuer() {
		return certIssuer;
	}
	public void setCertIssuer(String certIssuer) {
		this.certIssuer = certIssuer;
	}
	public boolean isSelfSigned() {
		return selfSigned;
	}
	public void setSelfSigned(boolean selfSigned) {
		this.selfSigned = selfSigned;
	}
	public Integer getAgentsPort() {
		return agentsPort;
	}
	public void setAgentsPort(Integer agentsPort) {
		this.agentsPort = agentsPort;
	}
	public boolean isAllowRegister() {
		return allowRegister;
	}
	public void setAllowRegister(boolean allowRegister) {
		this.allowRegister = allowRegister;
	}
	public boolean isAllowCUBU() {
		return allowCUBU;
	}
	public void setAllowCUBU(boolean allowCUBU) {
		this.allowCUBU = allowCUBU;
	}
	public boolean isAllowCRBU() {
		return allowCRBU;
	}
	public void setAllowCRBU(boolean allowCRBU) {
		this.allowCRBU = allowCRBU;
	}
	public Integer getWebPort() {
		return webPort;
	}
	public void setWebPort(Integer webPort) {
		this.webPort = webPort;
	}

}
