package org.catdroid.apps;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class AutoUpdate extends BroadcastReceiver {
	public static final String TAG = "AutoUpdate";
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean autoupdate = prefs.getBoolean("autoupdate", false);
		if(autoupdate && System.currentTimeMillis() - getLastUpdate() > AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
			new Updater().execute();
			setLastUpdate();
		}
	}
	
	private void notify(ArrayList<App> nuevas) {
		NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification noti = new Notification(R.drawable.icon, mContext.getString(R.string.updatesAvailable), System.currentTimeMillis());
		noti.defaults = Notification.DEFAULT_SOUND;
		CharSequence text = "";
		for(App app : nuevas) {
			if(text != "") text = text + ", ";
			text = text + app.getName().replace(" - app", "").replace(" - game", "");
		}
		Intent notiIntent = new Intent(mContext, List.class);
		notiIntent.putExtra("soloInstaladas", true);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notiIntent , 0);
		noti.setLatestEventInfo(mContext, mContext.getString(R.string.updateAvailable), text, contentIntent);

		mNotificationManager.notify(0, noti);
	}
	
	public class Updater extends AsyncTask<Void, Void, Void> {
		private ArrayList<App> apps;
		private PackageManager appInfo;

		@Override
		protected Void doInBackground(Void... params) {
			appInfo = mContext.getPackageManager();
			String xml = Downloader.download(Urls.getRepo());
			AppXmlParser axp = new AppXmlParser();
			apps = axp.Parse(xml);
			if(apps.size() > 0) {
				setXml(xml);
				ArrayList<App> nuevas = new ArrayList<App>();
				for(App app : apps) {
					if(isInstalled(app.getId())) {
						if(isNewer(app.getVersion(), getInstalledVersion(app.getId())))
							nuevas.add(app);
					}
				}
				AutoUpdate.this.notify(nuevas);
			}
			return null;
		}

		private boolean isInstalled(String packageName) {
			try {
				appInfo.getApplicationInfo(packageName, 0);
				return true;
			} catch (NameNotFoundException e) {
				return false;
			}
		}

		private String getInstalledVersion(String id) {
			try {
				return appInfo.getPackageInfo(id, 0).versionName;
			} catch (NameNotFoundException e) {
				return "0";
			}
		}

		private boolean isNewer(String version, String installedVersion) {
			if(version.length() == 0) return false;
			ArrayList<Integer> server = new ArrayList<Integer>();
			ArrayList<Integer> local = new ArrayList<Integer>();
			String[] vServer = version.split("\\.");
			for(String s : vServer) server.add(getInt(s));
			String[] vInst = installedVersion.split("\\.");
			for(String s : vInst) local.add(getInt(s));
			int i = 0;
			boolean masNuevo = false;
			boolean continuar = true;
			while(continuar) {
				if(server.size() > i && local.size() > i) {
					if(server.get(i) > local.get(i)) {
						masNuevo = true;
						continuar = false;
					} else if(server.get(i) < local.get(i)) {
						masNuevo = false;
						continuar = false;
					}
				} else {
					if(server.size() <= i) {
						masNuevo = false;
						continuar = false;
					} else {
						masNuevo = true;
						continuar = false;
					}
				}

				i++;
			}
			return masNuevo;
		}

		private int getInt(String string) {
			int entero = 0;
			boolean error = false;
			for(char c : string.toCharArray()) {
				if(!error) {
					String s = "" + c;
					try{
						int num = Integer.parseInt(s);
						entero = (entero * 10) + num;
					} catch(Exception ex) {
						error = true;
					}
				}
			}
			return entero;
		}
	}

	private void setXml(String xml) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Main.PREF_XML, xml);
		editor.commit();
	}

	private long getLastUpdate() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		return settings.getLong("lastUpdate", 0);
	}
	
	private void setLastUpdate() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("lastUpdate", System.currentTimeMillis());
		editor.commit();
	}

}
