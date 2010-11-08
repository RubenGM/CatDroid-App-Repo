package org.catdroid.apps;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class UpdateStarter extends Activity {
	public static final String TAG = "UpdateStarter";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		startUpdater(this);
		finish();
		super.onCreate(savedInstanceState);
	}
	
	public static void startUpdater(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean autoupdate = prefs.getBoolean("autoupdate", false);
		int freq = Integer.parseInt(prefs.getString("frecuencia", "24"));

		if(autoupdate) {
			AlarmManager mgr=(AlarmManager)context.getSystemService(ALARM_SERVICE);
			Intent i=new Intent(context, AutoUpdate.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
			
			long interval = freq * AlarmManager.INTERVAL_HOUR;

			if(System.currentTimeMillis() - prefs.getLong("lastUpdate", 0) > interval) {
				mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pi);
			}
			else {
				mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, pi);
			}
		}
	}

}
