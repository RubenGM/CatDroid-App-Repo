package org.catdroid.apps;

import java.util.ArrayList;
import java.util.Collections;

import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ListView;

public class List extends ListActivity {
	private AppAdapter adapter;
	private ProgressDialog pd;
	private App app;
	private String query;
	private boolean soloInstaladas = false;
	private boolean ordenarFecha = false;
	public static final String TAG = "List";
	private int lastId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
		Intent intent = getIntent();
		if(intent.getExtras() != null) {
			soloInstaladas = intent.getExtras().getBoolean("soloInstaladas");
			ordenarFecha = intent.getExtras().getBoolean("date");
		}
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			soloInstaladas = false;
		} else query = "";
		new CargaApps().execute();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		if(adapter != null)
			adapter.notifyDataSetChanged();
		super.onResume();
	}

	private String getXml() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getString(Main.PREF_XML, "");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		app = (App)getListAdapter().getItem(position);
		if(!app.isInstalled())
			new DescargaApp().execute();
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.alreadyInstalled)
			.setCancelable(false)
			.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					new DescargaApp().execute();
				}
			})
			.setNegativeButton(getString(R.string.uninstall), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Uri uninstallUri = Uri.fromParts("package", app.getId(), null);  
					Intent intent = new Intent(Intent.ACTION_DELETE, uninstallUri);
					startActivity(intent);
				}
			})
			.setNeutralButton(getString(R.string.doNothing), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		super.onListItemClick(l, v, position, id);
	}

	private class DescargaApp extends AsyncTask<Void, Void, Void> {
		Intent intent;
		Notification noti;
		int id;
		App miApp;

		@Override
		protected Void doInBackground(Void... params) {
			intent = Interact.download(List.this, miApp, noti, id);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			noti = List.notify(noti, "Descarga completada", "Descarga completada", "Haz click para instalar " + miApp.getName(), id, List.this, intent);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			id = lastId +1;
			lastId++;
			miApp = app;
			noti = List.notify(noti, "Preparando descarga", "Preparando descarga", app.getName(), id, getApplicationContext(), new Intent());
			super.onPreExecute();
		}
	}

	private class CargaApps extends AsyncTask<Void, Void, Void> {
		private boolean error = false;
		@Override
		protected Void doInBackground(Void... params) {
			MyContentHandler mch = new MyContentHandler();
			try {
				Xml.parse(getXml(), mch);
				ArrayList<App> apps = mch.getApps();
				if(query != "") {
					query = query.toLowerCase();
					ArrayList<App> buenas = new ArrayList<App>();
					for(App app : apps) {
						if(app.getName().toLowerCase().contains(query) || app.getId().toLowerCase().contains(query)) {
							buenas.add(app);
						}
					}
					apps = buenas;
					buenas = null;
				}
				if(ordenarFecha) {
					Collections.sort(apps);
				}
				adapter = new AppAdapter(apps, List.this, soloInstaladas);
			} catch (SAXException e) {
				error = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			pd = null;
			if(error) {
				finish();
			} else {
				setListAdapter(adapter);
				getListView().setFastScrollEnabled(true);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(List.this);
			pd.setTitle(R.string.espere);
			pd.setMessage(getString(R.string.cargandoApps));
			pd.show();
			super.onPreExecute();
		}

	}

	public static Notification notify(Notification noti, String title, String body, String subText, int idNotificacion, Context mContext, Intent notiIntent) {
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notiIntent , 0);
		if(noti == null) {
			Log.i(TAG, "Nueva notificacion");
			noti = new Notification(R.drawable.icon, title, System.currentTimeMillis());
			noti.setLatestEventInfo(mContext, body, subText, contentIntent);
			notifica(idNotificacion, noti, mContext);
		} else {
			Log.i(TAG, "Reaprovechando notificacion");
			noti.setLatestEventInfo(mContext, body, subText, contentIntent);
			notifica(idNotificacion, noti, mContext);
		}
		return noti;
	}

	public static void notifica(int id, Notification notificacion, Context mContext) {
		NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, notificacion);
	}

}
