package org.catdroid.apps;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyContentHandler extends DefaultHandler {
	private static final int INAME = 0;
	private static final int IPATH = 1;
	private static final int IVER = 2;
	private static final int IVERCODE = 3;
	private static final int IAPKID = 4;
	private static final int IICON = 5;
	private static final int IDATE = 6;

	private static final String NAME = "name";
	private static final String PATH = "path";
	private static final String VER = "ver";
	private static final String VERCODE = "vercode";
	private static final String APKID = "apkid";
	private static final String ICON = "icon";
	private static final String DATE = "date";
	private static final String START_APP = "package";
	private static final String END_APP = "package";

	private String name;
	private String path;
	private String version;
	private int codeVersion;
	private String id;
	private String date;
	private String icon;
	private boolean added = false;

	private ArrayList<App> apps;
	private int currentTag;

	public void initApp() {
		name = "";
		path = "";
		version = "";
		codeVersion = 0;
		id = "";
		date = "";
		icon = "";
		added = false;
	}

	public void endApp() {
		if(!added) {
			App a = new App();
			a.setCodeVersion(codeVersion);
			a.setDate(date);
			a.setId(id);
			a.setName(name);
			a.setPath(path);
			a.setVersion(version);
			a.setIcon(icon);
			apps.add(a);
			added = true;
		}
	}

	public ArrayList<App> getApps() {
		return apps;
	}

	public static final String TAG = "MyContentHandler";

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(currentTag != -1) {
			String s = new String(ch, start, length);
			switch(currentTag) {
			case INAME:
				name = s;
				break;
			case IPATH:
				path = s;
				break;
			case IVER:
				version = s;
				break;
			case IVERCODE:
				codeVersion = Integer.parseInt(s);
				break;
			case IAPKID:
				id = s;
				break;
			case IICON:
				icon = s;
				break;
			case IDATE:
				date = s;
				break;
			}
		}
		super.characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentTag = -1;
		if(localName.equals(END_APP)) {
			endApp();
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startDocument() throws SAXException {
		apps = new ArrayList<App>();
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		endApp();
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(localName.equals(APKID)) currentTag = IAPKID;
		else if(localName.equals(DATE)) currentTag = IDATE;
		else if(localName.equals(NAME)) currentTag = INAME;
		else if(localName.equals(PATH)) currentTag = IPATH;
		else if(localName.equals(ICON)) currentTag = IICON;
		else if(localName.equals(VER)) currentTag = IVER;
		else if(localName.equals(VERCODE)) currentTag = IVERCODE;
		else currentTag = -1;

		if(localName.equals(START_APP)) initApp();
		super.startElement(uri, localName, qName, attributes);
	}
}