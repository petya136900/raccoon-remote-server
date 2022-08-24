package com.petya136900.raccoonvpn.forward;
import org.springframework.stereotype.Service;
import com.petya136900.raccoonvpn.entitys.Condition;
import com.petya136900.raccoonvpn.enums.ConditionType;
@Service
public class LoadedCondition {
	public static final long STATUS_TURNED_OFF = 1;
	public static final long STATUS_STARTS_UP = 2;
	public static final long STATUS_ENABLED = 3;
	public static final long STATUS_PORT_BUSY_BY_ANOTHER_PROGRAMM = 4;
	public static final long STATUS_CONFLICT = 5;
	public static final long YIFF_ALLOWED = 7;
	public static final long STATUS_BAD_CONDITION = 6;
	private Long devId;
	private Long id;
	private String name;
	private Boolean autorun;
	private Boolean firewall;
	private String protocol;
	private Integer extPort;
	private String condType;
	private ConditionType condTypeEnum;
	private String condData;
	private String targetHost;
	private Integer targetPort;
	private long weight; // later
	private long status = STATUS_TURNED_OFF;
	public LoadedCondition() {
	
	}
	public LoadedCondition(Condition condition) {
		this.id=condition.getId();
		this.name=condition.getName();
		this.autorun=condition.getAutorun();
		this.firewall=condition.getFirewall();
		this.extPort=condition.getExtPort();
		this.protocol=condition.getProtocol().toString();
		this.setCondType(condition.getCondType().toString());
		this.condTypeEnum=condition.getCondType();
		if(condition.getDevice()!=null) {
			this.devId=condition.getDevice().getId();
		}
		this.condData=condition.getCondData();
		this.targetHost=condition.getTargetHost();
		this.targetPort=condition.getTargetPort();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getAutorun() {
		return autorun;
	}
	public void setAutorun(Boolean autorun) {
		this.autorun = autorun;
	}
	public Boolean getFirewall() {
		return firewall;
	}
	public void setFirewall(Boolean firewall) {
		this.firewall = firewall;
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
	public long getWeight() {
		return weight;
	}
	public void setWeight(long weight) {
		this.weight = weight;
	}
	public long getStatus() {
		return status;
	}
	public void setStatus(long status) {
		this.status = status;
	}
	public Long getDevId() {
		return devId;
	}
	public void setDevId(Long devId) {
		this.devId = devId;
	}
	public Integer getExtPort() {
		return extPort;
	}
	public void setExtPort(Integer extPort) {
		this.extPort = extPort;
	}
	public String getCondData() {
		return condData;
	}
	public void setCondData(String condData) {
		this.condData = condData;
	}
	public String getCondType() {
		return condType;
	}
	public void setCondType(String condType) {
		this.condType = condType;
	}
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public ConditionType getCondTypeEnum() {
		return condTypeEnum;
	}
	public void setCondTypeEnum(ConditionType condTypeEnum) {
		this.condTypeEnum = condTypeEnum;
	}
}
