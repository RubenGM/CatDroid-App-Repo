package org.catdroid.apps;

import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppAdapter extends BaseAdapter {
	public static final String TAG = "AppAdapter";
	public ArrayList<App> apps;
	private Activity mContext;
	LinearLayout layoutImagen;
	UrlImageView uiv;
	private PackageManager appInfo;

	public AppAdapter(ArrayList<App> aplicaciones, Activity context, boolean soloInstaladas) {
		apps = aplicaciones;
		mContext = context;
		appInfo = mContext.getPackageManager();
		if(soloInstaladas) {
			ArrayList<App> nuevo = new ArrayList<App>();
			ArrayList<App> updated = new ArrayList<App>();
			for(App app : apps) {
				if(isInstalled(app.getId())) {
					if(isNewer(app.getVersion(), getInstalledVersion(app.getId()))) updated.add(app);
					else nuevo.add(app);
				}
			}
			updated.addAll(nuevo);
			apps = updated;
			nuevo = null;
			updated = null;
		}
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
	
	private String haceDias(long time) {
		int dias = Math.round((System.currentTimeMillis() - time) / (24 * 60 * 60 * 1000));
		switch(dias) {
		case 0:
			return mContext.getString(R.string.hoy);
		case 1:
			return mContext.getString(R.string.ayer);
		default:
			return dias + " " + mContext.getString(R.string.diasAtras);
		}
	}

	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public Object getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		App app = (App) getItem(position);
		View v;

		if (convertView==null) {
			v = View.inflate(mContext, R.layout.app_row, null);
			layoutImagen = (LinearLayout) v.findViewById(R.id.linearImagen);
			uiv = new UrlImageView(mContext);
			layoutImagen.addView(uiv);
		}
		else {
			v = convertView;
			layoutImagen = (LinearLayout) v.findViewById(R.id.linearImagen);
			uiv = (UrlImageView) layoutImagen.getChildAt(0);
			uiv.stopDownloader();
		}

		uiv.setImageURL(app.getIconUrl());

		((TextView)v.findViewById(R.id.tvNombre)).setText(app.getName());
		((TextView)v.findViewById(R.id.tvFecha)).setText(haceDias(app.getTime()));
		((TextView)v.findViewById(R.id.tvPaquete)).setText(app.getId());
		((TextView)v.findViewById(R.id.tvVersion)).setText(mContext.getString(R.string.serverVersion) + ": " + app.getVersion());
		((TextView)v.findViewById(R.id.tvVersion)).setTextColor(Color.WHITE);
		if(isInstalled(app.getId())){
			app.setInstalled(true);
			((TextView)v.findViewById(R.id.tvNombre)).setTextColor(Color.YELLOW);
			String installedVersion = getInstalledVersion(app.getId());
			((TextView)v.findViewById(R.id.tvVersionLocal)).setText(mContext.getString(R.string.installedVersion) + ": " + installedVersion);
			boolean noAct = true;
			try{noAct = installedVersion.equals(app.getVersion());}catch(Exception ex){}
			if(noAct) {
				((TextView)v.findViewById(R.id.tvVersionLocal)).setTextColor(Color.WHITE);
			} else {
				if(isNewer(app.getVersion(), installedVersion)) {
					((TextView)v.findViewById(R.id.tvVersionLocal)).setTextColor(Color.GREEN);
					((TextView)v.findViewById(R.id.tvVersion)).setTextColor(Color.GREEN);
				} else {
					((TextView)v.findViewById(R.id.tvVersionLocal)).setTextColor(Color.RED);
					((TextView)v.findViewById(R.id.tvVersion)).setTextColor(Color.RED);
				}
			}
			((TextView)v.findViewById(R.id.tvVersionLocal)).setVisibility(TextView.VISIBLE);
		}
		else {
			app.setInstalled(false);
			((TextView)v.findViewById(R.id.tvNombre)).setTextColor(Color.WHITE);
			((TextView)v.findViewById(R.id.tvVersionLocal)).setText("");
			((TextView)v.findViewById(R.id.tvVersionLocal)).setVisibility(TextView.GONE);
		}

		return v;
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

}
