package com.petya136900.raccoonvpn.tools;

import java.util.regex.Pattern;

public class StoredString {
	private String text;
	public StoredString(String text) {
		this.text=text;
	}
	public Boolean check(Pattern regexpPattern) {
		return RegexpTools.checkRegexp(regexpPattern,this.text);
	}
	public Boolean check(String stringRegexp) {
		return RegexpTools.checkRegexp(stringRegexp,this.text);
	}
	public String removeFirstString(String remove) {
		return RegexpTools.replaceRegexp(this.text, remove, "",false);
	}
	public String removeAllString(String remove) {
		return RegexpTools.replaceRegexp(this.text, remove, "",true);
	}
	public String replace(String regexpWhat, String replacement, Boolean all) {
		return RegexpTools.replaceRegexp(this.text, regexpWhat, replacement, all);
	}
	public int rIndexOf(String whatRegexp) {
		return RegexpTools.rIndexOf(whatRegexp, this.text);
	}		
}
