package com.petya136900.raccoonvpn.agent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;
import com.petya136900.raccoonvpn.tools.JsonParser;
public class RProtocol {
	private static final Logger LOG 
    = Logger.getLogger(RProtocol.class.getName());
	private boolean debug=false;
	public Data send(Socket client, Data data) throws IOException {
		write(client,data);
		return read(client);
	}
	public Data read(Socket client) throws IOException {
		InputStream in = client.getInputStream();
		byte[] pattern = new byte[3];
		in.read(pattern, 0, 3);
		byte[] codeByte = new byte[1];
		in.read(codeByte, 0, 1);
		byte[] code = Bytes.merge(pattern, codeByte);
		if(debug) {
			debug("RP <<| "+"Клиент: "+client.getRemoteSocketAddress());
			debug("RP <<| "+"Код: "+Codes.getCodeName(code));
		}
		if(!Arrays.equals(pattern, Codes.RACCOON_PATTERN))
			throw new IllegalArgumentException("Unsupported packet");
		byte[] lengthBytes = new byte[2];
		in.read(lengthBytes, 0, 2);
		int length = Bytes.toInt(lengthBytes);
		if(debug) {
			debug("RP <<| "+"Правильный пакет, длина: "+length);
		}
		Data data;
		if(length==0) {
			data = new Data(code);
			return data;
		}
		byte[] dataBytes = new byte[length];
		in.read(dataBytes, 0, length);
		String dataString = new String(dataBytes,"UTF-8");
		data = JsonParser.fromJson(dataString, Data.class);
		data.setCode(code);
		return data;
	}

	public void write(Socket socket, Data data) throws IOException {
		OutputStream out =  socket.getOutputStream();
		byte[] code = data.getCode();
		data.setCode(null);
		String dataString = ((data==null||data.isEmpty())?"":JsonParser.toJson(data));
		int length = dataString.length();
		byte[] lengthBytes = Bytes.toByteArray(length);
		if(debug) {
			debug("RP >>> | Клиенту: "+socket.getRemoteSocketAddress());
			debug("RP >>> | "+"Код: "+Codes.getCodeName(code));
			debug("RP >>> | "+"Отправляю пакет длиной: "+length+" | "+Arrays.toString(lengthBytes));
		}
		out.write(Bytes.merge(
			code,   							   // Code
			Bytes.merge(lengthBytes, 			   // Length 
			dataString.getBytes("UTF-8"))));  	   // Data
	}
	private void debug(String string) {
		if(debug) {
			LOG.info(string);
		}
	}
	public boolean isDebug() {
		return debug;
	}
	public RProtocol setDebug(boolean debug) {
		this.debug = debug;
		return this;
	}
}
