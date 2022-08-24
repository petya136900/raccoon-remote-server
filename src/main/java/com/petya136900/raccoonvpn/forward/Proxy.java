package com.petya136900.raccoonvpn.forward;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.petya136900.raccoonvpn.LifeCycle;
import com.petya136900.raccoonvpn.enums.ConditionType;
import com.petya136900.raccoonvpn.exceptions.SocketTimeoutException;
import com.petya136900.raccoonvpn.exceptions.SocketWasRejectedException;
import com.petya136900.raccoonvpn.services.AuthorizedService;
import com.petya136900.raccoonvpn.tools.JsonParser;
public class Proxy {
	static final Logger LOG = 
				LoggerFactory.getLogger(LifeCycle.class);
	public static final int READ_FIRST_PACKET_TIMEOUT_MS = 5000;
	private static final Integer TTFB_TIMEOUT_MS = 5000;
	private Integer port;
	private Thread proxyThread;
	private boolean debug=false;
	private ServerSocket server = null;
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	private ConcurrentHashMap<Long, LoadedCondition> conditionsStorage;
	private AuthorizedService authorizedService;
	public void setAuthorizedService(AuthorizedService authorizedService) {
		this.authorizedService = authorizedService;
	}
	private ConnectedDevicesStorage deviceStorage;
	public Proxy(int port) {
		this.port=port;
	}
	public void start() throws IOException {
		synchronized (this) {
			if(proxyThread!=null) {
				return;
			}
			server = new ServerSocket(port);
			proxyThread = new Thread(()->{
				try {
					while(!proxyThread.isInterrupted()) {
						Socket clientSocket = server.accept();
						if(proxyThread.isInterrupted())
							break;
						clientSocket.getInetAddress();
						debug("New client: "+clientSocket.getInetAddress());
						new Thread(()->{
							byte[] firstPacket = readFirstPacket(clientSocket);
							TLSRecord record = parseTLS(firstPacket);
							String sni=null;
							if(record.isHandshake()&&record.getTlsHandshake()!=null) {
								sni = record.getTlsHandshake().getServerNameString();
								debug(clientSocket.getInetAddress()+" | SNI: "+sni);
							}
							try {
								Socket socketToTarget = null;
								ClientInfo clientInfo = new ClientInfo(sni,clientSocket.getInetAddress());
								LoadedCondition condition = findCondition(clientInfo);
								if(condition==null) {
									debug("Подходящих правил не нашлось");
									clientSocket.close();
									return;
								}
								debug("Условие: "+JsonParser.toJson(condition));
								ConnectedDevice device = deviceStorage.getDevice(condition.getDevId());
								String targetHost = condition.getTargetHost();
								Integer targetPort = condition.getTargetPort();
								if(device==null) {
									debug("Устройство не подключено, отключение");
									clientSocket.close();
									return;
								}
								if(device.isLocal()) {
									socketToTarget = new Socket(targetHost,targetPort);	
								} else {
									try {
										socketToTarget = deviceStorage.getSocket(device,targetHost,targetPort,clientInfo.getRemoteAddress().getHostAddress());
									} catch (SocketWasRejectedException e) {
										debug(String.format("Агент[%s] не смог подключиться к %s:%s",device.getName(),targetHost,targetPort));
									} catch (SocketTimeoutException e) {
										debug(String.format("Агент[%s] не смог подключиться к %s:%s, Reason: Timeout",device.getName(),targetHost,targetPort));
									}
								}
								if(socketToTarget==null) {
									clientSocket.close();
									return;
								}									
								debug("Real Target for "+targetHost+":"+targetPort+" - "+socketToTarget.getRemoteSocketAddress());
								try {
									exchange(clientSocket,socketToTarget,firstPacket,targetHost+":"+targetPort);
								} catch (Exception e) {
									e.printStackTrace();
									debug("Can't start exchange between sockets");
								}
							} catch (IOException e) {
								debug("Can't connect to target");
								e.printStackTrace();
							} 
						},":"+port+" | "+"FIND-TARGET | "+clientSocket.getInetAddress()).start();
					}
				} catch (Exception e) {
					
				}
				debug("Trying to stop server");
				try {
					server.close();
				} catch (IOException e1) {}
			},"TCP FORWARDER | :"+port);
			proxyThread.start();
			debug("Listener started: port "+port);
		}
	}
	private LoadedCondition findCondition(ClientInfo clientInfo) {
		ArrayList<LoadedCondition> fulfilledConditions = new ArrayList<>();
		ArrayList<LoadedCondition> finalArray = new ArrayList<>();
		conditionsStorage.forEach((id,cond)->{
			if(cond.getStatus()==LoadedCondition.STATUS_ENABLED) {
				if(checkCondition(clientInfo,cond)) {
					fulfilledConditions.add(cond);
				}
			}
		});
		if(fulfilledConditions.size()>1) {
			fulfilledConditions.forEach(x->{
				if(!x.getCondType().equalsIgnoreCase(ConditionType.DEFAULT.toString()))
				finalArray.add(x);
			});
		} else {
			if(fulfilledConditions.size()==1) 				
				finalArray.add(fulfilledConditions.get(0));
		}
		if(finalArray.size()>1) {
			debug("Найдено несколько подходящих правил, возвращаем 0 индекс");
		}
		LoadedCondition[] arr = finalArray.toArray(new LoadedCondition[finalArray.size()]);
		return arr.length>0?arr[0]:null;
	}
	private boolean checkCondition(ClientInfo clientInfo, LoadedCondition cond) {
		ConditionType condType = ConditionType.valueOf(cond.getCondType().toUpperCase());
		switch (condType) {
			case DEFAULT:
				if(cond.getFirewall()) {
					return checkIp(clientInfo.getRemoteAddress());
				} else {
					return true;
				}
			case IP:
				if(clientInfo.getRemoteAddress().getHostAddress().equals(cond.getCondData().trim()))
					return true;
				break;
			case NETWORK:
				return deviceStorage.matches(clientInfo.getRemoteAddress().getHostAddress(), cond.getCondData().trim());
			case USER:
				return checkIpAndUserId(clientInfo.getRemoteAddress(),Long.parseLong(cond.getCondData()));
			case SNI:
				if(debug) {
					debug("Required SNI: "+clientInfo.getServerNameIndication());
					debug("Cond data: "+cond.getCondData());
				}
				if(clientInfo.getServerNameIndication()==null)
					return false;
				if(clientInfo.getServerNameIndication().toLowerCase().trim().equals(cond.getCondData().trim().toLowerCase())) {
					if(cond.getFirewall()) {
						return checkIp(clientInfo.getRemoteAddress());
					} else {
						return true;
					}
				}
				break;
			default:
				return false;
		}
		return false;
	}
	private boolean checkIpAndUserId(InetAddress remoteAddress, long userId) {
		debug("Для правила включена проверка пользователя");
		boolean result = authorizedService.checkAuthorizedByIpAndUserId(remoteAddress.getHostAddress(),userId);
		debug("Клиент подходит: "+result);
		return result;
	}
	private boolean checkIp(InetAddress remoteAddress) {
		debug("Для правила включен файрволл, проверка");
		boolean result = authorizedService.checkAuthorizedByIp(remoteAddress.getHostAddress());
		debug("Клиент подходит: "+result);
		return result;
	}
	private byte[] readFirstPacket(Socket clientSocket) {
		byte[] data = null;
		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			clientSocket.setSoTimeout(READ_FIRST_PACKET_TIMEOUT_MS);
			int length = in.available();
			if(length<1) {
				long timeout = System.currentTimeMillis()+TTFB_TIMEOUT_MS;
				try {
					do {
						length = clientSocket.getInputStream().available();
						try {
							Thread.sleep(2);
						} catch (InterruptedException e) {}
					} while(length<1&&System.currentTimeMillis()<timeout);
				} catch (IOException e) {}
			}
			data = new byte[length];
            in.readFully(data, 0, length);
		} catch (Exception e) {}
		try {
			clientSocket.setSoTimeout(0);
		} catch (SocketException e) {}
		return data;
	}
	private void exchange(Socket clientSocket, Socket socketToTarget, byte[] firstPacket, String target) throws IOException {
		InputStream clientIS = clientSocket.getInputStream();
		InputStream targetIS = socketToTarget.getInputStream();
		
		OutputStream clientOS = clientSocket.getOutputStream();
		OutputStream targetOS = socketToTarget.getOutputStream();
		
		String labelCT = "CLIENT -> TARGET | "+clientSocket.getInetAddress()+" -> "+target;
		String labelTC = "TARGET -> CLIENT | "+target+" -> "+clientSocket.getInetAddress();
		
		new Thread(readThenWrite(clientIS,targetOS,labelCT,firstPacket),":"+port+" | "+labelCT).start();
		new Thread(readThenWrite(targetIS,clientOS,labelTC,null),":"+port+" | "+labelTC).start();
	}
	private Runnable readThenWrite(InputStream is, OutputStream os, String label, byte[] dataFromClient) {
		return new Runnable() {
			DataInputStream in = new DataInputStream(is);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] data = null;
		    int length = -1;
		    int zeroCount=0;
			@Override
			public void run() {
				if(dataFromClient!=null) {
				    try {
				    	os.write(dataFromClient);
				    } catch(Exception e) {}
				}
			    while (!Thread.currentThread().isInterrupted()) {
			    	try {
				        if ((length = in.available()) > 0) {
				        	data = new byte[length];
				            in.readFully(data, 0, length);
				            os.write(data, 0, length);
				            if(debug)
				            debug(label+" | Bytes sended: "+length);
				        } else {
				        	if(zeroCount++>4096) {
				        		zeroCount=0;
				        		if(debug)
				        		debug(label+" | Long idle, check manually..");
				        		int firstByte=in.read();
				        		if(firstByte!=-1) {
				        			if(debug)
				        			debug(label+" | And.. gotcha!");
					        		length = in.available();
					        		outStream.write(firstByte);
					        		data = new byte[length];
						            in.readFully(data, 0, length);
						            outStream.write(data);
					        		os.write(outStream.toByteArray(), 0, outStream.size());
					        		outStream.reset();
					        		if(debug)
					        		debug(label+" | Bytes sended: "+length+1);
				        		} else {
				        			if(debug)
				        			debug(label+" | socket was closed?");
				        			break;
				        		}
				        	} else {
				        		try {
									Thread.sleep(0,250);
								} catch (InterruptedException e) {
									
								}
				        	}
				        }
			    	} catch (IOException e) {
						break;
					}
			    }
			    if(debug)
		    	debug(label+" | Socket closed");
				try {
					is.close();
					if(debug)
					debug(label+" | is closed");
				} catch (IOException e1) {
					if(debug)
					debug(label+" | can't close is");
				}
				try {
					os.close();
					if(debug)
					debug(label+" | os closed");
				} catch (IOException e1) {
					if(debug)
					debug(label+" | can't close os");
				}
			}	
		};
	}
	private TLSRecord parseTLS(byte[] data) {
		TLSRecord tlsRecord = new TLSRecord();
		tlsRecord.parse(data);
		return tlsRecord;
	}		
	private void debug(String string) {
		if(debug)
			LOG.info("[:"+port+"] "+string);
	}
	public void stop() {
		synchronized (this) {
			if(proxyThread==null) {
				debug("Proxy not started");
				return;
			}
			try {
				proxyThread.interrupt();
			} catch (Exception e) {}
			try {
				server.close();
			} catch (IOException e) {
			}
			debug("Stopped");
		}
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
	public void setConditionsStorage(ConcurrentHashMap<Long, LoadedCondition> conditionsStorage) {
		this.conditionsStorage=conditionsStorage;
	}
	public void setDeviceStorage(ConnectedDevicesStorage deviceStorage) {
		this.deviceStorage = deviceStorage;
	}
}
