package com.petya136900.raccoonvpn.longpolling;

public class LongPollEvent {
	
	public static final long CODE_SETTING_CHANGED = 17l;
	public static final long CODE_SOCKET_REJECT = 16l;
	public static final long CODE_SOCKET_FOUND = 15l;
	public static final long CODE_CONDITION_STARTING = 14l;
	public static final long CODE_CONDITION_CANT_START=13l;
	public static final long CODE_CONDITION_STOPPED=12l;
	public static final long CODE_CONDITION_STARTED=11l;
	public static final long CODE_CONDITION_DELETED=10l;
	public static final long CODE_CONDITION_CHANGED=9l;
	public static final long CODE_CONDITION_ADDED=8l;
	public static final long CODE_DEVICE_DELETED=7l;
	public static final long CODE_DEVICE_ADDED=6l;
	public static final long USER_CHANGED=5l;
	public static final long USER_DELETED=4l;
	public static final long USER_ADDED=3l;
	public static final long CODE_DEVICE_DISCONNECTED=2l;
	public static final long CODE_DEVICE_CONNECTED=1l;
	
	private long sockId;
	private long ts = 0l;
	private String desc = "No events";
	private String data;
	private long id;
	private long time;
	private long code=0l;
	private boolean timeout = true;
	public String getDesc() {
		return desc;
	}
	public LongPollEvent setDesc(String desc) {
		this.desc = desc;
		return this;
	}
	public long getTs() {
		return ts;
	}
	public LongPollEvent setTs(long ts) {
		this.ts = ts;
		return this;
	}
	public boolean isTimeout() {
		return timeout;
	}
	public LongPollEvent setTimeout(boolean byTimeout) {
		this.timeout = byTimeout;
		return this;
	}
	public long getTime() {
		return time;
	}
	public LongPollEvent setTime(long time) {
		this.time = time;
		return this;
	}
	public long getCode() {
		return code;
	}
	public LongPollEvent setCode(long code) {
		this.code = code;
		return this;
	}
	public String getData() {
		return data;
	}
	public LongPollEvent setData(String data) {
		this.data = data;
		return this;
	}
	public long getId() {
		return id;
	}
	public LongPollEvent setId(long id) {
		this.id = id;
		return this;
	}
	public long getSockId() {
		return sockId;
	}
	public LongPollEvent setSockId(long sockId) {
		this.sockId = sockId;
		return this;
	}
}
