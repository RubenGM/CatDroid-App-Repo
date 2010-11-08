package org.catdroid.apps;

import java.sql.Date;

import android.app.Activity;

public class App implements Comparable<App> {
	public static final String TAG = "App";
	private boolean installed = false;
	private String name;
	private String path;
	private String version;
	private int codeVersion;
	private String id;
	private String date;
	private String icon;
	private UrlImageView iv;
	private long time = -1;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getVersion() {
		if(version == null) return "";
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getCodeVersion() {
		return codeVersion;
	}
	public void setCodeVersion(int codeVersion) {
		this.codeVersion = codeVersion;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getIcon() {
		return icon;
	}
	public String getIconUrl() {
		return Urls.base + icon;
	}
	public void setIv(UrlImageView iv) {
		this.iv = iv;
	}
	public UrlImageView getIv(Activity context) {
		if(iv == null) {
			iv = new UrlImageView(context);
			iv.setImageURL(Urls.base + getIcon());
		}
		return iv;
	}
	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
	public boolean isInstalled() {
		return installed;
	}
	public long getTime() {
		if(time == -1) {
			try{
				int year = 0;
				int month = 0;
				int day = 0;
				String[] temp = date.split("-");
				day = Integer.parseInt(temp[2]);
				month = Integer.parseInt(temp[1]);
				year = Integer.parseInt(temp[0]);
				month -= 1;
				year -= 1900;
				Date d = new Date(0);
				d.setDate(day);
				d.setMonth(month);
				d.setYear(year);
				time = d.getTime();
				return d.getTime();
			}catch(Exception ex) {
				return 0;
			}
		} else return time;
	}
	@Override
	public int compareTo(App another) {
		long res = getTime() - another.getTime();
		if(res == 0) return 0;
		if(res < 0) return 1;
		if(res > 0) return -1;
		else return 0;
	}
}
