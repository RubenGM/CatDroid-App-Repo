package org.catdroid.apps;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Opciones extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
	}

	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
		CheckBoxPreference checkBoxPref = new CheckBoxPreference(this);
		checkBoxPref.setKey("autoupdate");
		checkBoxPref.setTitle(R.string.autoupdate);
		root.addPreference(checkBoxPref);

		ListPreference listPref = new ListPreference(this);
		listPref.setKey("frecuencia");
		listPref.setTitle(R.string.autoupdateFreq);
		listPref.setEntries(R.array.tiempos);
		listPref.setEntryValues(R.array.tiemposValores);
		root.addPreference(listPref);
		
		/*
		AlarmManager mgr=(AlarmManager)getSystemService(ALARM_SERVICE);
		Intent i=new Intent(this, OnAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), PERIOD, pi);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String strSetting = prefs.getString("username", "");
		*/

		return root;
	}
	
	
	
}
