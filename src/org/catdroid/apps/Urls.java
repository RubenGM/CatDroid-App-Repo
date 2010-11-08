package org.catdroid.apps;

public class Urls {
	//http://www.blapkmarket.com/getrepo.php?name=rubengm&pass=rubengm
	public static final String base = "http://rocboronat.net/aptoide/";
	public static final String list = "info.xml";
	
	public static String getRepo() {
		return base + list;
	}
	public static String download(String path) {
		return base + path;
	}
}
