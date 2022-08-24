package com.petya136900.raccoonvpn.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpTools {
	
	public static final Pattern SUBNET_PATTERN = Pattern.compile("^((((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|(\\/([0-9]|[1-2][0-9]|3[0-2]))$)){4}))$");
	
	public static final Pattern IP_PATTERN = Pattern.compile("^((\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
	
	public static StoredString storeString(String text) {
		return new StoredString(text);
	}
	public static Boolean checkRegexp(Pattern keyP, String text) {
		Matcher matcher=keyP.matcher(text.trim().toLowerCase());
		if(matcher.find()) {
			return true;
		} else {
			return false;
		}
	}
	public static Boolean checkRegexp(String stringPattern, String text) {
		Pattern keyP = Pattern.compile(stringPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher=keyP.matcher(text.trim().toLowerCase());
		if(matcher.find()) {
			return true;
		} else {
			return false;
		}
	}
	public static String removeFirstString(String message, String remove) {
		return replaceRegexp(message, remove, "",false);
	}
	public static String removeAllString(String message, String remove) {
		return replaceRegexp(message, remove, "",true);
	}
	public static String replaceRegexp(String message, String what, String replacement, Boolean all) {
		if(all) {
			return Pattern.compile("(?i)"+what,Pattern.UNICODE_CASE).matcher(message).replaceAll(replacement);
		} else {
			return Pattern.compile("(?i)"+what,Pattern.UNICODE_CASE).matcher(message).replaceFirst(replacement);
		}
	}
	public static int rIndexOf(String whatRegexp, String text) {
		Pattern keyP = Pattern.compile(whatRegexp, Pattern.CASE_INSENSITIVE);
		Matcher matcher=keyP.matcher(text.trim().toLowerCase());
		if(matcher.find()) {
			return matcher.start();
		} else {
			return -1;
		}
	}		
}
