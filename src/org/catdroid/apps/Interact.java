package org.catdroid.apps;

import java.io.File;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;


public class Interact {
	public static final String TAG = "Interact";

	public static void downloadAndInstall(Activity activity, ProgressDialog pd, App app) {
		/*
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/" + activity.getPackageName() + "/apk/tmp.apk";
		File f = new File(path);
		f.delete();
		File ffs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/" + activity.getPackageName() + "/apk");
		ffs.mkdirs();
		Downloader.downloadBinary(activity, pd, Urls.download(app.getPath()), path);
		if(!Downloader.isCancelled()) {
			if(Downloader.isHasNewDialog()) {
				Downloader.getNewDialog().dismiss();
			}
			Intent instala = new Intent(Intent.ACTION_VIEW);
			instala.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
			activity.startActivity(instala);
		}
		*/
	}

	public static Intent download(Activity activity, App app, Notification noti, int idNotificacion) {
		Intent instala = new Intent();
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/" + activity.getPackageName() + "/apk/" + app.getId() + ".apk";
		File f = new File(path);
		f.delete();
		File ffs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/" + activity.getPackageName() + "/apk");
		ffs.mkdirs();
		Downloader d = new Downloader();
		d.downloadBinaryNotification(activity, Urls.download(app.getPath()), path, idNotificacion, noti, app);
		if(!d.isCancelled()) {
			instala = new Intent(Intent.ACTION_VIEW);
			instala.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
		}
		return instala;
	}
}
