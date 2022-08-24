package com.petya136900.raccoonvpn.forward;
import java.util.Arrays;

public class TLSRecord {
	private byte type; // 1 byte, 0x01 - ClientHello
	private byte[] version; // 2 bytes
	private byte[] length; // 3 bytes,
	private boolean isCorrent = false;
	private boolean isHandshake = false;
	private TLSHandshake tlsHandshake;
	class TLSHandshake {
		public byte[] getServerName() {
			return serverName;
		}
		public String getServerNameString() {
			if(serverName==null)
				return null;
			return new String(serverName);
		}
		public byte getCompressionMethodsLength() {
			return compressionMethodsLength;
		}
		public byte[] getCompressionMethods() {
			return compressionMethods;
		}
		public byte[] getExtensionsLength() {
			return extensionsLength;
		}
		public boolean isCorrent() {
			return isCorrent;
		}
		public boolean isClientHello() {
			return isClientHello;
		}
		public byte[] getClientRandom() {
			return clientRandom;
		}
		public byte getSessionIDlength() {
			return sessionIDlength;
		}
		public byte[] getSessionID() {
			return sessionID;
		}
		public byte[] getChiperSuitesLength() {
			return chiperSuitesLength;
		}
		public byte[] getChiperSuites() {
			return chiperSuites;
		}
		public byte[] getExtensions() {
			return extensions;
		}
		private boolean isCorrent = false;
		private boolean isClientHello = false;
		private byte[] clientRandom; // 32 bytes
		private byte sessionIDlength; // 1 byte
		private byte[] sessionID = new byte[] {}; // sessionIDlength bytes
		private byte[] chiperSuitesLength; // 2 byte
		private byte[] chiperSuites = new byte[] {}; // chiperSuitesLength bytes
		private byte compressionMethodsLength; // 1 byte
		private byte[] compressionMethods = new byte[] {}; // compressionMethodsLength bytes
		private byte[] extensionsLength; // 2 bytes
		private byte[] serverName = null;		
		private byte[] extensions = new byte[] {}; // 
		private TLSRecord.TLSHandshake parseHandshake(byte[] rawHandshake) {
			try {
				type    = rawHandshake[0];
				length = new byte[] {rawHandshake[1],rawHandshake[2],rawHandshake[3]};
				version  = new byte[] {rawHandshake[4],rawHandshake[5]};
				clientRandom = Arrays.copyOfRange(rawHandshake, 6, 38);
				sessionIDlength = rawHandshake[38];
				int sIdOff = 38+(sessionIDlength & 0xFF);
				if(sessionIDlength!=0x00)
					sessionID = Arrays.copyOfRange(rawHandshake, 39, sIdOff+1);
				chiperSuitesLength = new byte[] {rawHandshake[sIdOff+1],rawHandshake[sIdOff+2]};
				if((chiperSuitesLength[0]!=0x00)||(chiperSuitesLength[1]!=0x00)) {
					chiperSuites = Arrays.copyOfRange(rawHandshake, 
							sIdOff+3, 
							(sIdOff+3)+(toInt(chiperSuitesLength)));
				}
				compressionMethodsLength = rawHandshake[(sIdOff+3)+chiperSuites.length];
				int compMOff = ((sIdOff+3)+chiperSuites.length+1)+(compressionMethodsLength&0xFF);
				if(compressionMethodsLength!=0x00)
					compressionMethods = Arrays.copyOfRange(rawHandshake, 
							(sIdOff+3)+chiperSuites.length+1, 
							compMOff);
				extensionsLength = Arrays.copyOfRange(rawHandshake, compMOff, compMOff+2);
				isCorrent=true;
				if((extensionsLength[0]!=0x00)|(extensionsLength[1]!=0x00))
					extensions = Arrays.copyOfRange(rawHandshake, 
							compMOff+2, 
							(compMOff+2)+(toInt(extensionsLength)));
				if(extensions.length>0)
					parseExtentions();
			} catch (Exception e) {
				//System.err.println("error while parsing handshake");
				isCorrent=false;
			}
			return this;
		}
		private void parseExtentions() {
			int stage = 1;
			byte[] type = new byte[2];
			byte[] length = new byte[2];
			int lengthInt;
			byte bbyte;
			for(int i=0;i<extensions.length;i++) {
				bbyte = extensions[i];
				switch(stage) {
					case(1):
						type[0] = bbyte;
						stage=2;
						break;
					case(2):
						type[1] = bbyte;
						stage=3;
						break;
					case(3):
						length[0] = bbyte;
						stage=4;
						break;
					case(4):
						length[1] = bbyte;
						stage=5;
						break;
					case(5):
						//System.out.println("extention type: "+Proxy.byteArrayToHex(type)+" | ext_length: "+Proxy.byteArrayToHex(length));
						if((type[0]==0x00)&&(type[1]==0x00)) {
							byte[] sniExtensionData = Arrays.copyOfRange(extensions, // byte[]  
											i, 
											i+(length[1]&0xFF));
							//System.out.println("Extention data: "+Proxy.byteArrayToHex(sniExtensionData));
							//System.out.println("Extention data: "+new String(sniExtensionData));
							//byte[] sniListLength = new byte[] {sniExtensionData[0],sniExtensionData[1]};
							//byte sniType = sniExtensionData[2];
							byte[] serverNameLength = new byte[] {sniExtensionData[3],sniExtensionData[4]};
							serverName = Arrays.copyOfRange(sniExtensionData, 
									5, 
									5+(toInt(serverNameLength)));
							
						}
						lengthInt = toInt(length);
						i+=lengthInt;
						i--;
						stage=1;
						break;
				}
			}
		}
	}
	public void parse(byte[] rawRecord) {
		try {
			type    = rawRecord[0];
			version = new byte[] {rawRecord[1],rawRecord[2]};
			length  = new byte[] {rawRecord[3],rawRecord[4]};
			if(checkIsTls()) {
				if(isHandshake) {
					TLSHandshake tlsHandshake = new TLSHandshake();
					byte[] rawHandshake = Arrays.copyOfRange(rawRecord, 5, rawRecord.length);
					tlsHandshake.parseHandshake(rawHandshake);
					this.tlsHandshake = tlsHandshake;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			//System.err.println("error while parsing record");
			isCorrent=false;
		}
	}
	private boolean checkIsTls() {
		switch(type) { 
			case(0x16):
				isHandshake=true;
			case(0x15):
			case(0x14):
			case(0x17):
				isCorrent=true;
				break;
			default:
				isCorrent=false;
		}
		return isCorrent;
	}
	public byte[] getVersion() {
		return version;
	}
	public byte[] getLength() {
		return length;
	}
	public boolean isHandshake() {
		return isHandshake;
	}
	public boolean isCorrent() {
		return isCorrent;
	}
	public byte getType() {
		return type;
	}	
	public TLSHandshake getTlsHandshake() {
		return tlsHandshake;
	}
	private int toInt(byte[] b) {
		return (int)(
	        (0xff & b[0]) << 8  |
	        (0xff & b[1]) << 0 
        );
	}
}
