package com.petya136900.raccoonvpn;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.petya136900.raccoonvpn.tools.Tools;
@SpringBootApplication
public class RaccoonVPNServer {	
	static {
		AnsiConsole.systemInstall();
	}
	public static int HTTPS_PORT = 8443;
	public static Integer TCP_LISTENER_PORT = 8444;
	public static int HTTP_PORT = 8080;
	public static String KEYSTORE_TYPE = "JKS"; // PKCS12
	public static String KEYSTORE_FILE = "keystore.jks"; // keystore.p12
	public static String KEYSTORE_PASS = "password";
	public static String KEYSTORE_ALIAS = "raccoonvpn";
	public static String API_VERSION = "v1";
	public static Boolean PORT_BUSY = false;
	public static int HTTPS_PORT_IF_BUSY = 0;
	// other
	public static Boolean tcpIgnoreDb = false;
	public static String certCN;
	public static String certIssuer;
	public static boolean certSelfSigned;
	public static Integer agentsPort;
	public static String[] argsList;
	public static Boolean useHttp = false;
	public static Boolean RACCON_DEBUG = false; 
	public static ConfigurableApplicationContext context;
	/**
	 * 
	 * @return -1 if Application not running, </br>
	 * otherwise exit code, 0 if normal
	 */
	public static int stop() {
		if(context!=null) {
			return SpringApplication.exit(context,()->0);
		}
		return -1;
	}
	public static Integer getServerPort() {
		return PORT_BUSY?HTTPS_PORT_IF_BUSY:HTTPS_PORT;
	}
	public static void main (String[] args) throws Exception {
		if(args==null)
			args=new String[] {};
		argsList = args;
		readArg(new String[] {"--help","help","-help","-h"}, false, null, null, ()->{
			System.out.println(s("--help",sa("-help","help"),false,false,"Show this message"));
			System.out.println(s("--web-port",sa("--wport","-wp"),true,false,"RaccoonVPN Server web port","-p 8443"));
			System.out.println(s("--agents-port",sa("--aport","-ap"),true,false,"RaccoonVPN Server port for connecting agents","-p 8444"));
			System.out.println(s("--debug",sa("-d"),false,false,"Show debug messages","--debug"));
			System.out.println(s("--http",sa("-http"),false,false,"Also use http","--http"));
			System.out.println(Ansi.ansi().fgDefault());
			System.exit(0);
		});
		try {
			HTTPS_PORT = Integer.parseInt(Tools.readFileToString("wport"));
		} catch (Exception e) {}
		useHttp = readArg(new String[] {"--http","-http"},false, true, false);
		readArg(new String[] {"--web-port","--wport","-wp"}, false, null, null, ()->{
			try {
				HTTPS_PORT = Integer.parseInt(readArg(new String[] {"--web-port","--wport","-wp"}, true, null, "8443"));
			} catch (Exception e) {}
		});
		try {
			TCP_LISTENER_PORT = Integer.parseInt(readArg(new String[] {"--agents-port","--aport","-ap"}, true, null, "8444"));
			readArg(new String[] {"--agents-port","--aport","-ap"}, false, null, null, ()->{
				tcpIgnoreDb=true;
			});
		} catch (Exception e) {}
		RACCON_DEBUG = readArg(new String[] {"--debug","-d"}, false, true, false);
		try {
			Tools.checkCert();
		} catch (Exception e) {
			System.err.println("Не удалось сгенерировать сертификат, проверьте, что keytool присутствует в системе и прописан в PATH");
			System.exit(1);
		}
		Tools.checkPort();	
		SpringApplication app = new SpringApplication(RaccoonVPNServer.class);
		Map<String, Object> mapArgs = new HashMap<>();
		mapArgs.put("spring.datasource.url", Tools.getDBFile());
		Tools.clearAndGetTempDir();
		mapArgs.put("spring.profiles.active", RACCON_DEBUG?"debug":"");
		mapArgs.put("spring.h2.console.enabled", RACCON_DEBUG?"true":"false");
		mapArgs.put("spring.h2.console.settings.web-allow-others", RACCON_DEBUG?"true":"false");
		mapArgs.put("server.http.port", HTTP_PORT);
		mapArgs.put("server.port", getServerPort());
		mapArgs.put("custombean.redirect.enable", "true");
		mapArgs.put("server.ssl.key-store-type", KEYSTORE_TYPE);
		mapArgs.put("server.ssl.key-store", Tools.WORK_DIR+KEYSTORE_FILE);
		mapArgs.put("server.ssl.key-store-password", KEYSTORE_PASS);
		mapArgs.put("server.ssl.key-alias", KEYSTORE_ALIAS);
		mapArgs.put("server.ssl.enabled", "true");
		mapArgs.put("raccoonvpn.http.enable",useHttp?"true":"false");
		app.setDefaultProperties(mapArgs);
        context = app.run(args);
	}
	/// Args
	private static <T> T readArg(String[] names, boolean needValue, T ifFound, T ifNot) {
		return readArg(names, needValue, ifFound, ifNot, null);
	}
	@SuppressWarnings("unchecked")
	private static <T> T readArg(String[] names, boolean needValue, T ifFound, T ifNot, Action actionIfFound) {
		Stream<String> stream = Stream.of(names);
		if(argsList==null)
			return ifFound;
		for(int i=0;i<argsList.length;i++) {
			String arg = argsList[i];
			Boolean hasNext = argsList.length>i+1;
			String nextArg = hasNext?argsList[i+1]:null;
			stream = Stream.of(names);
			if(stream.anyMatch(x->x.equals(arg))) {
				if(actionIfFound!=null)
					actionIfFound.perfome();
				if(needValue) {
					if(hasNext) {
						try {
							return (T) new String(nextArg);
						} catch (Exception e) {
							return ifNot;
						}
					} else {
						return ifNot;
					}
				} else {
					return ifFound;
				}
			}
			stream.close();
		}
		return ifNot;
	}
	private static String[] sa(String ...elements) {
		return elements;
	}
	private static Ansi s(String argName, String[] altNames, Boolean hasValue,Boolean isArrayValues, String desc) {
		return s(argName,altNames,hasValue,isArrayValues,desc,null);
	}
	private static Ansi s(String argName, String[] altNames, Boolean hasValue,Boolean isArrayValues, String desc, String example) {
		String altNamesString ="";
		if(altNames!=null&&altNames.length>0) {
			altNamesString = Stream.of(altNames)
				.filter(x->x!=null&&x.length()>0)
				.map(x->"OR "+x.trim()+" ")
				.collect(Collectors.joining());
		}
		return Ansi.ansi().a("\n").fgBrightYellow().a(argName).a(" ")
		.fgYellow().a(altNamesString)
		.fgBrightBlue().a((hasValue?(isArrayValues?"[value1,value2,..,valueN] ":"[value] "):""))
		.fgDefault()
		.a("| ")
		.fgBrightGreen()
		.a(desc)
		.fgCyan()
		.a(((example!=null&&example.length()>0)?"\n\tFor example: "+example:""));
	}
	public static void restart() {
		Thread newMain = new Thread(()->{
			RaccoonVPNServer.stop();
			Boolean restarted=false;
			do {
				try {
					Thread.sleep(3000);
					System.out.println("Вызываем main");
					RaccoonVPNServer.main(null);
					System.out.println("Main loaded");
					restarted=true;
				} catch (Exception e) {
					System.out.println("Error catch");
					e.printStackTrace();
					try{Thread.sleep(5000);}catch (Exception e2) {}
				}
			} while(!restarted);
		});
		newMain.setDaemon(false);
		newMain.start();
	}
}
