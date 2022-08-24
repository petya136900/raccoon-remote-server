package com.petya136900.raccoonvpn.rest.v1;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import com.petya136900.raccoonvpn.exceptions.ApiException;
import com.petya136900.raccoonvpn.rest.v1.codes.ResponseCodes;
import com.petya136900.raccoonvpn.tools.RegexpTools;
import com.petya136900.raccoonvpn.tools.Tools;

public class ACME {

	private final static String LE_ACCOUNT_KEY_FILE = "leaccount.key";
	
	private final static String OPENSSL_CONFIG_FILE = "openssl.cnf";
	
	private final static String WORK_DIR = "workdir";
	
	private final static String WELL_KNOWN_DIR = ".well-known\\acme-challenge";
	
	private final static String CERT_DIR = "certdir";
	
	private final static String ACME_CLIENT = "acme_client.jar";
	
	public void go(String domain) throws IOException, InterruptedException, ApiException {
		if(!RegexpTools.checkRegexp("^((?!-))(xn--)?[a-z0-9][a-z0-9-_]{0,61}[a-z0-9]{0,1}\\.(xn--)?([a-z0-9\\-]{1,61}|[a-z0-9-]{1,30}\\.[a-z]{2,})$", domain)) {
			throw new IllegalArgumentException("Bad Domain");
		}
		try {
			ServerSocket test80Port = new ServerSocket(80);
			test80Port.close();
		} catch (Exception e) {
			throw new ApiException(ResponseCodes.PORT_80_BUSY);
		}
		Tools.createWorkDir();
		Tools.createDirs(WELL_KNOWN_DIR);
		Tools.createDirs(CERT_DIR);
		Tools.createDirs(WORK_DIR);
		Tools.createFileFromDefault(OPENSSL_CONFIG_FILE);
		try {
			Tools.getFile(ACME_CLIENT);
		} catch (Exception e) {
			Tools.createFileFromDefault(ACME_CLIENT);
		}
		File workDir = Tools.getWorkDir();
		try {
			Tools.getFile(LE_ACCOUNT_KEY_FILE);
		} catch (Exception e) {
			Tools.runCommand(workDir, new String[] {"openssl","genrsa","-out",workDir+"\\"+LE_ACCOUNT_KEY_FILE,"2048"});
			try {
				Tools.getFile(LE_ACCOUNT_KEY_FILE);
			} catch (Exception e2) {
				throw new ApiException(ResponseCodes.OPEN_SSL_ERROR);
			}
			Tools.runCommand(workDir, new String[] {"java","-jar","acme_client.jar","--command","register","-a",workDir+"\\"+LE_ACCOUNT_KEY_FILE,"--with-agreement-update","--email","admin@"+domain});
		}
		try {
			Tools.getFile(LE_ACCOUNT_KEY_FILE);
		} catch (Exception e2) {
			throw new ApiException(ResponseCodes.CANT_CREATE_LE_ACCOUNT);
		}
		Tools.runCommand(workDir, new String[] {"openssl","genrsa","-out",workDir+"\\"+domain+".key","2048"});
		Tools.runCommand(workDir, new String[] {
				"openssl",
				"req",
				"-config",workDir+"\\"+OPENSSL_CONFIG_FILE,
				"-new",
				"-key",workDir+"\\"+domain+".key",
				"-sha256",
				"-nodes",
				"-subj","\"/O=RaccoonVPN/CN="+domain+"/emailAddress=admin@"+domain+"\"",
				"-outform","PEM",
				"-out",workDir+"\\"+domain+".csr"});
		Tools.runCommand(workDir, new String[] {"java","-jar","acme_client.jar",
				"--command","order-certificate",
				"-a",LE_ACCOUNT_KEY_FILE,
				"-w",WORK_DIR,
				"-c",domain+".csr",
				"--well-known-dir",workDir+"\\"+WELL_KNOWN_DIR,
				"--one-dir-for-well-known"});
		Tools.runCommand(workDir, new String[] {"java","-jar","acme_client.jar",
				"--command","verify-domains",
				"-a",LE_ACCOUNT_KEY_FILE,
				"-w",WORK_DIR,
				"-c",domain+".csr"});
		Tools.runCommand(workDir, new String[] {"java","-jar","acme_client.jar",
				"--command","generate-certificate",
				"-a",workDir+"\\"+LE_ACCOUNT_KEY_FILE,
				"-w",workDir+"\\"+WORK_DIR,
				"--cert-dir",workDir+"\\"+CERT_DIR,
				"--csr",workDir+"\\"+domain+".csr"});
		//Application.main(null);
	}

}
