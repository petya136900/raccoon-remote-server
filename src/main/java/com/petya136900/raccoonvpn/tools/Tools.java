package com.petya136900.raccoonvpn.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.ldap.LdapName;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import com.petya136900.raccoonvpn.RaccoonVPNServer;
import com.petya136900.raccoonvpn.services.AuthorizedService;

public class Tools {	
	public static final String WORK_DIR;
	
	private static String DB_FILE = "db";
	private static String TEMP_DIR = "temp";
	
	public static String getDBFile() {
		return "jdbc:h2:file:"+WORK_DIR+DB_FILE;
	}
	
	private static byte[] vars;
	
	private static final Logger LOG 
    = Logger.getLogger(Tools.class.getName());
	
	public static Integer getHttpsPort() {
		return (RaccoonVPNServer.PORT_BUSY?RaccoonVPNServer.HTTPS_PORT_IF_BUSY:RaccoonVPNServer.HTTPS_PORT);
	}
	
	private static String[] classPathDirs = {
		"classpath:defaults/*",
		"classpath:defaults/index_files/*",
		"classpath:defaults/assets/*",
		"classpath:defaults/assets/raccoonvpn/*",
		"classpath:defaults/assets/raccoonvpn/404/*",
		"classpath:defaults/assets/raccoonvpn/brand/*",
		"classpath:defaults/assets/raccoonvpn/dist/*",
		"classpath:defaults/assets/raccoonvpn/dist/css/*",
		"classpath:defaults/assets/raccoonvpn/dist/js/*",
		"classpath:defaults/assets/raccoonvpn/dist/logo/*",
		"classpath:defaults/scripts/*",
		"classpath:defaults/scripts/raccoonvpn/*"
	};
	
	static {
		String homeDir = System.getProperty("user.home");
		if(homeDir==null||homeDir.trim().length()<1) {
			WORK_DIR = new File("~/.raccoon/").getAbsolutePath()+"/";
		} else {
			WORK_DIR = new File(homeDir+"/.raccoon/").getAbsolutePath()+"/";
		}
		LOG.info("Work directory: "+WORK_DIR);
	}
	
	public static String getLocalIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "127.0.0.1";
		}
	}
	
	public static String getLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}
	
	public static File createFile(String relFileName, String data) throws IOException {
		createWorkDir();
		File newFile = new File(WORK_DIR+relFileName);
		LOG.info(String.format("Trying to create file[%s]",newFile.getAbsolutePath()));
		checkAndCreateFile(newFile);
		FileUtils.writeStringToFile(newFile, data, StandardCharsets.UTF_8, false);
		return newFile;
	}
	
	public static void createFile(String relFileName, byte[] bytes) throws FileNotFoundException, IOException {
		createWorkDir();
		File newFile = new File(WORK_DIR+relFileName);
		LOG.info(String.format("Trying to create file[%s]",newFile.getAbsolutePath()));
		checkAndCreateFile(newFile);
		try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
		    outputStream.write(bytes);
		}
	}
	
	public static File createFileCustom(String pathAbsolute, String data) throws IOException {
		LOG.info(String.format("Trying to create file[%s]",pathAbsolute));
		createWorkDir();
		File newFile = new File(pathAbsolute);
		checkAndCreateFile(newFile);
		FileUtils.writeStringToFile(newFile, data, StandardCharsets.UTF_8, false);
		return newFile;
	}   
	
	public static File createFileFromResource(String resourceSource, String fsTarget) throws IOException {
		createWorkDir();
		File newFile = Tools.createFile(fsTarget);
		checkAndCreateFile(newFile);
		FileUtils.copyInputStreamToFile(Tools.getDefaultFile(resourceSource), newFile);
		return newFile;
	}
	
	public static File createFileFromResourceCustom(String resourceSource, String fsTarget) throws IOException {
		File newFile = Tools.createFileCustom(fsTarget);
		checkAndCreateFile(newFile);
		try(InputStream is = Tools.getDefaultFile(resourceSource)) {
			FileUtils.copyInputStreamToFile(is, newFile);	
		}
		return newFile;
	}
	
	private static void checkAndCreateFile(File newFile) throws IOException {
		File parents = newFile.getParentFile();
		if(!parents.exists()&&parents.mkdirs()) {}
		if(newFile.isDirectory())
			newFile.delete();
		if(!newFile.exists())
			newFile.createNewFile();
	}

	public static File createFile(String fileName) throws IOException {
		return createFile(fileName,"");
	}
	
	public static File createFileCustom(String absolutePath) throws IOException {
		return createFileCustom(absolutePath,"");
	}
	
	
	public static String readDefaultFileToString(String defailtFileName) throws FileNotFoundException {
		return privateReadISToString(getDefaultFile(defailtFileName));
	}
	
	private static String privateReadISToString(InputStream is) {
		BufferedReader br = new BufferedReader(
			      new InputStreamReader(is, StandardCharsets.UTF_8));
		String result = br.lines()
		.collect(Collectors.joining("\n"));
		try {
			br.close();
		} catch (IOException e) {}
		return result;
	}
	
	public static File createFileFromDefault(String defaultFileName) throws FileNotFoundException, IOException {
		File file = Tools.createFile(defaultFileName);
		try(InputStream is = Tools.getDefaultFile(defaultFileName)) {
			FileUtils.copyInputStreamToFile(Tools.getDefaultFile(defaultFileName), file);	
		}
		return file;
	}
	
	public static File getFile(String fileName) throws IOException {
		File file = new File(WORK_DIR+fileName);
		if(file.exists())
			return file;
		throw new FileNotFoundException();
	}
	
	public static void createWorkDir() {
		File workDir = new File(WORK_DIR);
		if(workDir.isFile())
			workDir.delete();
		if(!workDir.exists())
			workDir.mkdirs();		
	}
	public static InputStreamReader getDefaultFileISR(String fileName) throws FileNotFoundException {
		try {
			return new InputStreamReader(getDefaultFile(fileName), "UTF-8");
		} catch (Exception e) {
			throw new FileNotFoundException();
		}
	}
	public static InputStream getDefaultFile(String fileName) throws FileNotFoundException {
		try {
			return new ClassPathResource("defaults/"+fileName).getInputStream();
		} catch (Exception e) {
			throw new FileNotFoundException();
		}
	}
	
	public static String generateToken() {
		return (UUID.randomUUID().toString()+UUID.randomUUID().toString()).replaceAll("-", "");
	}
	
	public static Long getCurrentSecs() {
		return ((System.currentTimeMillis()/1000));
	}
	
    public static Long getExpiresIn() {
    	return ((System.currentTimeMillis()/1000)+AuthorizedService.TOKEN_LIFE_SEC);
    }
	
    public static Boolean checkExpire(String expire_in) {
    	return checkExpire(Long.parseLong(expire_in));
    }
    /**
     * 
     * @param expire_in
     * @return true if token expired </br>
     * false otherwise
     */
	private static Boolean checkExpire(long expire_in) {
		if(expire_in>getCurrentSecs())
			return false;
		return true;
	}
	public static void updateVars() {
		try {
			vars = readDefaultFileToString("scripts/raccoonvpn/vars.js").replaceAll("\\{\\{port\\}\\}", getHttpsPort()+"").replaceAll("\\{\\{protocol\\}\\}", "https").replaceAll("\\{\\{version\\}\\}", RaccoonVPNServer.API_VERSION).getBytes();
		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING, "Can't update vars");
		}
	}
	public static byte[] getVars() throws FileNotFoundException {
		if(vars==null) {
			updateVars();
		}
		return vars;
	}
	
	public static String hashSHA256(String string) {
		return DigestUtils.sha256Hex(string.getBytes());
	}

	public static String[] getClassPathDirs() {
		return classPathDirs;
	}

	public static void setClassPathDirs(String[] classPathDirs) {
		Tools.classPathDirs = classPathDirs;
	}

	public static void checkCert() throws Exception {
		createWorkDir();
		try {
			try {
				getFile(RaccoonVPNServer.KEYSTORE_FILE);
			} catch (Exception e) {
				LOG.info("Certificate not found, generating");
				File keytool = createFile("keytool.exe");
				File jli = createFile("jli.dll");
				File j2pcsc = createFile("j2pcsc.dll");
				try(InputStream is = new ClassPathResource("tools/keytool.exe").getInputStream()) {
					FileUtils.copyInputStreamToFile(is, keytool);	
				}
				try(InputStream is = new ClassPathResource("tools/jli.dll").getInputStream()) {
					FileUtils.copyInputStreamToFile(is, jli);	
				}
				try(InputStream is = new ClassPathResource("tools/j2pcsc.dll").getInputStream()) {
					FileUtils.copyInputStreamToFile(is, j2pcsc);	
				}
				try {
					LOG.info("Execute: "+"keytool"+" -genkeypair -noprompt -alias "+RaccoonVPNServer.KEYSTORE_ALIAS+" -keyalg RSA -keysize 2048 -storetype "+RaccoonVPNServer.KEYSTORE_TYPE+" -keystore "+WORK_DIR+RaccoonVPNServer.KEYSTORE_FILE+" -validity 3650 --storepass "+RaccoonVPNServer.KEYSTORE_PASS+"-keypass "+RaccoonVPNServer.KEYSTORE_PASS+" -dname \"CN="+RaccoonVPNServer.KEYSTORE_ALIAS+"\" -ext san=dns:localhost"); // ,dns:127.0.0.1
					runCommand(new File(WORK_DIR),"keytool","-genkeypair","-noprompt","-alias",RaccoonVPNServer.KEYSTORE_ALIAS,"-keyalg","RSA","-keysize","2048","-storetype",RaccoonVPNServer.KEYSTORE_TYPE,"-keystore",WORK_DIR+RaccoonVPNServer.KEYSTORE_FILE,"-validity","3650","--storepass",RaccoonVPNServer.KEYSTORE_PASS,"-keypass",RaccoonVPNServer.KEYSTORE_PASS,"-dname","CN="+RaccoonVPNServer.KEYSTORE_ALIAS,"-ext","san=dns:localhost"); // ,dns:127.0.0.1
				} catch (Exception e2) {
					e2.printStackTrace();
					try {
						runCommand(new File(WORK_DIR),WORK_DIR+"\\keytool.exe","-genkeypair","-noprompt","-alias",RaccoonVPNServer.KEYSTORE_ALIAS,"-keyalg","RSA","-keysize","2048","-storetype",RaccoonVPNServer.KEYSTORE_TYPE,"-keystore",WORK_DIR+RaccoonVPNServer.KEYSTORE_FILE,"-validity","3650","--storepass",RaccoonVPNServer.KEYSTORE_PASS,"-keypass",RaccoonVPNServer.KEYSTORE_PASS,"-dname","CN="+RaccoonVPNServer.KEYSTORE_ALIAS,"-ext","san=dns:localhost"); // ,dns:127.0.0.1
					} catch (Exception e3) {
						e3.printStackTrace();
						LOG.info("Not Windows-OS detect");
						try(InputStream is = new ClassPathResource("tools/"+RaccoonVPNServer.KEYSTORE_FILE).getInputStream()) {
							FileUtils.copyInputStreamToFile(is, createFile(RaccoonVPNServer.KEYSTORE_FILE));	
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			KeyStore ks = getAsKS(getFile(RaccoonVPNServer.KEYSTORE_FILE), 
					RaccoonVPNServer.KEYSTORE_PASS);
			Certificate cert = ks.getCertificate("raccoonvpn");
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate x509Cert = (X509Certificate)certFactory.generateCertificate(
					new ByteArrayInputStream(cert.getEncoded()));
			String dn = x509Cert.getSubjectX500Principal().getName();
			LdapName ldapDN = new LdapName(dn);
			Principal principal = x509Cert.getIssuerDN();
			String commonName = (String) ldapDN.getRdns().get(0).getValue();
	        String issuerDn = principal.getName();
	        Boolean selfSigned = (issuerDn.replaceAll("^.*=", "").equals(commonName));
	        RaccoonVPNServer.certCN=commonName;
	        RaccoonVPNServer.certIssuer=issuerDn;
	        RaccoonVPNServer.certSelfSigned=selfSigned;
	        System.out.println("CN: "+commonName);
	        System.out.println("Issuer: "+issuerDn);
	        System.out.println("SelfSigned: "+selfSigned);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Cert not found, error while generating");
		}
	}
	public static KeyStore getAsKS(File ksFile, String pass) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(ksFile), 
				pass.toCharArray());
		return ks;
	}
	public static void runCommand(File dir, String... command) throws IOException, InterruptedException {
		System.out.println("Running: "+Stream.of(command)
			.collect(Collectors.joining(" ")));
	    ProcessBuilder processBuilder = new ProcessBuilder().command(command);
	    processBuilder.redirectErrorStream(true); 
    	processBuilder.directory(dir);
        Process process = processBuilder.start();
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String output = null;
        while ((output = bufferedReader.readLine()) != null) {
            LOG.info("Output: "+output);
        }
        process.waitFor();
        bufferedReader.close();
        process.destroy();
	}
	public static void checkPort() throws Exception {
		if(RaccoonVPNServer.useHttp) {
			RaccoonVPNServer.HTTP_PORT = Tools.checkFreePort(RaccoonVPNServer.HTTP_PORT);
		}
		ServerSocket server;
		try {
			server = new ServerSocket(RaccoonVPNServer.HTTPS_PORT);
			server.close();
			RaccoonVPNServer.PORT_BUSY=false;
		} catch (Exception e) {
			try {
				server = new ServerSocket(0);
				RaccoonVPNServer.HTTPS_PORT_IF_BUSY = server.getLocalPort();
				server.close();
			} catch (IOException e1) {
				
			}
			LOG.warning("Port "+RaccoonVPNServer.HTTPS_PORT+" Busy, use "+RaccoonVPNServer.HTTPS_PORT_IF_BUSY+" instead");
			RaccoonVPNServer.PORT_BUSY=true;
		}
	}
	public static String readFileToString(String relativePath) throws IOException {
		File file = getFile(relativePath);
		return privateReadISToString(new FileInputStream(file));
	}

	public static void createDirs(String dir) {
		new File(WORK_DIR+"\\"+dir).mkdirs();
	}

	public static File getWorkDir() {
		return new File(WORK_DIR);
	}

	public static Integer checkFreePort(Integer port) {
		Integer freePort;
		try {
			new ServerSocket(port).close();
			return port;
		} catch (Exception e) {
			ServerSocket server;
			try {
				server = new ServerSocket(0);
			} catch (IOException e2) {
				return 0;
			}
			freePort = server.getLocalPort();
			try {
				server.close();
			} catch (IOException e1) {}
			return freePort;
		}
	}

	public static void deleteFile(String string) {
		File f = new File(WORK_DIR+"\\"+string);
		if(f.exists())
			f.delete();
	}

	public static KeyStore convertPemToJKS(String unicId) throws IOException, InterruptedException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		// PEM -> p12
		runCommand(getWorkDir(), new String[] {"openssl","pkcs12","-export","-out","certs/"+unicId+".p12","-in","certs/"+unicId+".crt",
				"-inkey","certs/"+unicId+".key","-passin","pass:"+RaccoonVPNServer.KEYSTORE_PASS,"-passout",
				"pass:"+RaccoonVPNServer.KEYSTORE_PASS});
		getFile("certs/"+unicId+".p12");
		// p12 -> JKS
		try {
			runCommand(getWorkDir(), new String[] {"keytool","-importkeystore","-srckeystore","certs/"+unicId+".p12",
					"-destkeystore","certs/"+unicId+".jks","-storepass",RaccoonVPNServer.KEYSTORE_PASS,"-keypass",
					RaccoonVPNServer.KEYSTORE_PASS,"-srcstorepass",RaccoonVPNServer.KEYSTORE_PASS});
		} catch (Exception e) {
			runCommand(getWorkDir(), new String[] {"keytool.exe","-importkeystore","-srckeystore","certs/"+unicId+".p12",
					"-destkeystore","certs/"+unicId+".jks","-storepass",RaccoonVPNServer.KEYSTORE_PASS,
					"-keypass",RaccoonVPNServer.KEYSTORE_PASS,"-srcstorepass",RaccoonVPNServer.KEYSTORE_PASS});	
		}
		getFile("certs/"+unicId+".jks");
		// change alias to -> "raccoonvpn"
		try {
			runCommand(getWorkDir(), new String[] {"keytool","-changealias","-alias","1","-destalias",RaccoonVPNServer.KEYSTORE_ALIAS,
					"-keypass",RaccoonVPNServer.KEYSTORE_PASS,
					"-keystore","certs/"+unicId+".jks","-storepass",RaccoonVPNServer.KEYSTORE_PASS});
		} catch (Exception e) {
			runCommand(getWorkDir(), new String[] {"keytool.exe","-changealias","-alias","1","-destalias",RaccoonVPNServer.KEYSTORE_ALIAS,
					"-keypass",RaccoonVPNServer.KEYSTORE_PASS,
					"-keystore","certs/"+unicId+".jks","-storepass",RaccoonVPNServer.KEYSTORE_PASS});	
		}
		KeyStore ks = getAsKS(getFile("certs/"+unicId+".jks"), RaccoonVPNServer.KEYSTORE_PASS);
		return ks;
	}

	public static void copyFile(String file1, String file2) throws IOException {
		FileUtils.copyFile(getFile(file1), createFile(file2));
	}

	public static String clearAndGetTempDir() throws IOException {
		FileUtils.deleteQuietly(new File(WORK_DIR+TEMP_DIR));
		FileUtils.forceMkdir(new File(WORK_DIR+TEMP_DIR));
		System.setProperty("java.io.tmpdir", WORK_DIR+TEMP_DIR);
		return WORK_DIR+TEMP_DIR;
	}
}
