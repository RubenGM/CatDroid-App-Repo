package org.catdroid.apps;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {
	private Button btnTest;
	private Button btnBrowse;
	private Button btnBrowseDate;
	private Button btnInstaladas;
	public static final String PREF_XML = "xml";
	public static final String TAG = "LoginForm";
	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		config();
	}

	@Override
	protected void onResume() {
		UpdateStarter.startUpdater(this);
		super.onResume();
	}

	public void config() {
		settings = PreferenceManager.getDefaultSharedPreferences(this);

		btnTest = (Button)findViewById(R.id.btnCheck);
		btnBrowse = (Button)findViewById(R.id.btnBrowse);
		btnBrowseDate = (Button)findViewById(R.id.btnBrowseDate);
		btnInstaladas = (Button)findViewById(R.id.btnInstaladas);

		btnTest.setEnabled(true);
		btnBrowse.setEnabled(browseOk());
		btnBrowseDate.setEnabled(browseOk());
		btnInstaladas.setEnabled(browseOk());

		btnTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Updater().execute();
			}
		});

		btnBrowse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, List.class);
				startActivity(intent);
			}
		});

		btnBrowseDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, List.class);
				intent.putExtra("date", true);
				startActivity(intent);
			}
		});

		btnInstaladas.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, List.class);
				intent.putExtra("soloInstaladas", true);
				startActivity(intent);
			}
		});
		
		((Button)findViewById(R.id.btnSettings)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, Opciones.class);
				startActivity(intent);
			}
		});
	}

	private boolean browseOk() {
		return getXml() != "";
	}

	private String getXml() {
		return settings.getString(PREF_XML, "");
	}

	private void setXml(String xml) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_XML, xml);
		editor.commit();
	}

	public class Updater extends AsyncTask<Void, Void, Void> {
		private ProgressDialog pd;
		private ArrayList<App> apps;

		@Override
		protected void onPostExecute(Void result) {
			if(pd != null) {
				pd.dismiss();
				pd = null;
			}

			btnBrowse.setEnabled(apps.size() > 0);
			btnBrowseDate.setEnabled(apps.size() > 0);
			btnInstaladas.setEnabled(apps.size() > 0);
			Log.i(TAG, "Lista de apps descargada.");
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(Main.this);
			pd.setTitle(R.string.espere);
			pd.setMessage(getString(R.string.connecting));
			pd.show();
			Log.i(TAG, "Descargando lista de apps");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String xml = Downloader.getUrlContent(Urls.getRepo(), pd, Main.this);
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					if(pd != null) {
						pd.dismiss();
						pd = null;
					}
					if(Downloader.isHasNewDialog()) {
						Downloader.getNewDialog().dismiss();
					}
					pd = new ProgressDialog(Main.this);
					pd.setTitle(R.string.espere);
					pd.setMessage(getString(R.string.readingXml));
					pd.show();
				}});
			AppXmlParser axp = new AppXmlParser();
			apps = axp.Parse(xml);
			if(apps.size() > 0) {
				setXml(xml);
			}
			return null;
		}

	}
}