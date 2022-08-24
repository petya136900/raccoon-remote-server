package com.petya136900.raccoonvpn.agent;
import java.lang.reflect.Field;
import java.util.Arrays;
public class Codes {
	// SERVER
	public final static byte[] RACCOON_PATTERN = {b(0xAB),b(0xFF),b(0xAB)};
	public final static byte[] RACCOON_PING = b(RACCOON_PATTERN,b(0x00));
	// REQUESTS
	public final static byte[] RACCOON_LOGIN = b(RACCOON_PATTERN,b(0x01));
	public final static byte[] RACCOON_NEW = b(RACCOON_PATTERN,b(0x02));
	public final static byte[] RACCOON_CONNECT = b(RACCOON_PATTERN,b(0x03));
	public final static byte[] RACCOON_NEED_SOCKET = b(RACCOON_PATTERN,b(0x04));
	public final static byte[] RACCOON_REGEN = b(RACCOON_PATTERN,b(0x05));
	public static final byte[] RACCOON_BYE = b(RACCOON_PATTERN,b(0x06));
	public final static byte[] RACCOON_UPGRADE_TO_TLS = b(RACCOON_PATTERN,b(0x07));
	public final static byte[] RACCON_REQ_NET = b(RACCOON_PATTERN,b(0x08));
	public final static byte[] RACCON_CHECK_TASK = b(RACCOON_PATTERN,b(0x09));
	// RESPONSES
	public final static byte[] RACCOON_OK = b(RACCOON_PATTERN,b(0x10));
	public final static byte[] RACCOON_TOKEN_EXPIRED = b(RACCOON_PATTERN,b(0x11));
	public final static byte[] RACCOON_CONNECTED = b(RACCOON_PATTERN,b(0x12));
	public final static byte[] RACCOON_ALREADY_CONNECTED = b(RACCOON_PATTERN,b(0x13));
	public final static byte[] RACCOON_UNKNOWN_CODE = b(RACCOON_PATTERN,b(0x14));
	public final static byte[] RACCOON_SOCKET = b(RACCOON_PATTERN,b(0x15));
	public final static byte[] RACCOON_SOCKET_REJECT = b(RACCOON_PATTERN,b(0x16));
	public final static byte[] RACCOON_USER_NOT_FOUND = b(RACCOON_PATTERN,b(0x17));
	public final static byte[] RACCOON_WRONG_PASS = b(RACCOON_PATTERN,b(0x18));
	public final static byte[] RACCOON_SUCCESS_LOGIN = b(RACCOON_PATTERN,b(0x19));
	public static final byte[] RACCOON_UNKNOWN_ERROR = b(RACCOON_PATTERN,b(0x20));
	public static final byte[] RACCOON_BAD_TOKEN = b(RACCOON_PATTERN,b(0x21));
	public static final byte[] RACCOON_CHECKING = b(RACCOON_PATTERN,b(0x22));
	public static final byte[] RACCOON_TASK_OK = b(RACCOON_PATTERN,b(0x23));
	public static final byte[] RACCOON_TASK_NF = b(RACCOON_PATTERN,b(0x24));
	public static final byte[] RACCOON_TASK_DATA = b(RACCOON_PATTERN,b(0x25));
	// OTHER
	public final static byte[] RACCOON_GUI_OPEN = b(RACCOON_PATTERN,b(0x50));
	public final static byte[] RACCOON_TEST_STOP_SERVER = b(RACCOON_PATTERN,b(0x51));	
	private static byte b(int i) {
		return (byte) i;
	}
	private static byte[] b(byte[] a, byte b) {
		return Bytes.merge(a,b);
	}
	public static String getCodeName(byte[] code) {
		Class<Codes> c = Codes.class;
		for (Field f : c.getFields()) {
			if(f.getType().equals(byte[].class)) {
				try {
					byte[] value = (byte[]) f.get(null);
					if((value!=null)&&Arrays.equals(value,code))
						return f.getName();
				} catch (Exception e) {
					continue;
				}
			}
		}
		return "UNKNOWN";
	}
}
