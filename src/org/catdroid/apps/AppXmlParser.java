package org.catdroid.apps;

import java.util.ArrayList;

import org.xml.sax.SAXException;

import android.util.Log;
import android.util.Xml;

public class AppXmlParser {
	public static final String TAG = "AXP";

	public ArrayList<App> Parse(String xml) {
		try {
			MyContentHandler mch = new MyContentHandler();
			Xml.parse(xml, mch);
			return mch.getApps();
		} catch (SAXException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
		return new ArrayList<App>();
	}
}
