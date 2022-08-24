package com.petya136900.raccoonvpn.agent;
import java.io.ByteArrayOutputStream;
public class Bytes {
	public static byte[] toByteArray(int a) {
		return new byte[] {
			(byte)((a >> 8) & 0xff),
			(byte)((a >> 0) & 0xff),
		};		
	}
	public static int toInt(byte[] b) {
		if (b == null || b.length != 2) return 0x0;
	    return (int)(
    		(0xff & b[0]) << 8   |
	        (0xff & b[1]) << 0
        );		
	}
	public static byte[] merge(byte[] a, byte[] b) {
		ByteArrayOutputStream res = new ByteArrayOutputStream();
		try {
			res.write(a);
			res.write(b);
		} catch (Exception e) {}
		return res.toByteArray();
	}
	public static byte[] merge(byte[] a, byte b) {
		return merge(a, new byte[] {b});
	}
}
