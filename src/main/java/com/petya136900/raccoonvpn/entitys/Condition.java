package com.petya136900.raccoonvpn.entitys;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petya136900.raccoonvpn.enums.ConditionType;
import com.petya136900.raccoonvpn.enums.Protocol;
import com.petya136900.raccoonvpn.exceptions.DeviceNotFoundException;
import com.petya136900.raccoonvpn.forward.LoadedCondition;
import com.petya136900.raccoonvpn.services.DeviceService;
@Entity
@Table(name = "conditions")
@EnableAutoConfiguration
public class Condition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Boolean autorun=true;
	private Boolean firewall = false;
	private Protocol protocol;
	private Integer extPort;
	private ConditionType condType;
	private String condData;
	private String targetHost;
	private Integer targetPort;
	@Transient
	@JsonIgnore
	@Autowired
	private DeviceService deviceService;
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
	private Device device;
	public Condition() {
		
	}
	public Condition(LoadedCondition cond, Device device) throws DeviceNotFoundException {
		this.name=cond.getName();
		this.autorun=cond.getAutorun();
		this.firewall=cond.getFirewall();
		this.protocol=(Protocol.valueOf(cond.getProtocol().toUpperCase()));
		this.setExtPort(cond.getExtPort());
		this.setCondType(ConditionType.valueOf(cond.getCondType().toUpperCase()));
		this.setCondData(cond.getCondData());
		this.targetHost=cond.getTargetHost();
		this.targetPort=cond.getTargetPort();
		this.device = device;
	}
	@JsonIgnore
	public Device getDevice() {
		return device;
	}
	// GETTERS & SETTERS
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getAutorun() {
		return autorun;
	}
	public void setAutorun(Boolean autorun) {
		this.autorun = autorun;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	public String getTargetHost() {
		return targetHost;
	}
	public void setTargetHost(String targetHost) {
		this.targetHost = targetHost;
	}
	public Integer getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(Integer targetPort) {
		this.targetPort = targetPort;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public Boolean getFirewall() {
		return firewall;
	}
	public void setFirewall(Boolean firewall) {
		this.firewall = firewall;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ConditionType getCondType() {
		return condType;
	}
	public void setCondType(ConditionType condType) {
		this.condType = condType;
	}
	public String getCondData() {
		return condData;
	}
	public void setCondData(String condData) {
		this.condData = condData;
	}
	public Integer getExtPort() {
		return extPort;
	}
	public void setExtPort(Integer extPort) {
		this.extPort = extPort;
	}
}
