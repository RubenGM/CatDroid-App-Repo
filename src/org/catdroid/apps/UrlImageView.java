package org.catdroid.apps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

public class UrlImageView extends ImageView {
	public static final String TAG = "UrlImageView";
	protected String url;
	protected Downloader downloader;
	protected Bitmap bm;
	protected Activity activity;
	protected String path;
	protected String hash;
	protected String fullpath;
	protected boolean isCached = false;
	protected int defaultIcon;
	protected boolean hasDefaultIcon = false;
	protected boolean isDone = false;
	protected int parentId;
	protected int height = -1;
	protected int width = -1;
	protected boolean isScaled = false;
	protected Runnable pinta = new Runnable() {
		public void run() {
			if(!isScaled) setImageBitmap(bm);
			else setImageBitmap(Bitmap.createScaledBitmap(bm, width, height, true));
			isDone = true;
		}
	};

	public UrlImageView(Activity context) {
		super(context);
		activity = context;
		path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/" + context.getPackageName() + "/cache/";
		File f = new File(path);
		f.mkdirs();
	}

	public void setImageURL(String uri) {
		url = uri;
		if(uri == null) {
			setImageResource(defaultIcon);
			return;
		}
		else if(uri.equals("") && hasDefaultIcon) {
			setImageResource(defaultIcon);
			return;
		}
		try {
			hash = getHash(uri);
			fullpath = path + hash;
		} catch (NoSuchAlgorithmException e) {
			bm = getError();
			isDone = true;
			setImageBitmap(getError());
			return;
		}
		isCached = isCached();
		if(isCached) {
			drawFromCache();
			return;
		}
		setImageBitmap(getDownloading());
		downloader = new Downloader();
		try{
			downloader.execute();
		}
		catch(Exception ex) {
			bm = getError();
			isDone = true;
			setImageBitmap(getError());
			return;
		}
	}

	public void stopDownloader() {
		if(downloader != null) {
			downloader.cancel(true);
			downloader = null;
		}
	}

	private void Download(String uri) throws MalformedURLException {
		URL url = new URL(uri);
		try{
			bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
		}catch(IOException ex) {
			bm = getError();
		}
	}

	private Bitmap getError() {
		int i = 0;
		i++;
		if(hasDefaultIcon) return BitmapFactory.decodeResource(getResources(), defaultIcon);
		else return BitmapFactory.decodeResource(getResources(), R.drawable.error_de_descarga);
	}

	private Bitmap getDownloading() {
		if(hasDefaultIcon) return BitmapFactory.decodeResource(getResources(), defaultIcon);
		else return BitmapFactory.decodeResource(getResources(), R.drawable.descargando_imagen);
	}

	private class Downloader extends AsyncTask<Void, Void, Void> {
		private boolean isCancelled = false;
		@Override
		protected void onCancelled() {
			isCancelled = true;
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Download(url);
				if(!isCached && !isCancelled) {
					saveToCache();
				}
			} catch (MalformedURLException e) {
				bm = getError();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(!isCancelled)
				activity.runOnUiThread(pinta);
		}

	}

	private boolean isCached() {
		if(fullpath == null) return false;
		File cache = new File(fullpath);
		return cache.length() > 0;
	}

	private void drawFromCache() {
		bm = BitmapFactory.decodeFile(fullpath);
		activity.runOnUiThread(pinta);
	}

	protected void saveToCache() throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(new File(fullpath));
		try{
			bm.compress(Bitmap.CompressFormat.PNG, 70, out);
		}catch(NullPointerException ex) {
			//No hagamos nada :/
			setBm(getError());
		activity.runOnUiThread(pinta);
		}
	}

	private String getHash(String uri) throws NoSuchAlgorithmException {
		MessageDigest mDigest=MessageDigest.getInstance("MD5");

		mDigest.update(uri.getBytes());

		byte d[]=mDigest.digest();
		StringBuffer hash=new StringBuffer();

		for (int i=0; i<d.length; i++) {
			hash.append(Integer.toHexString(0xFF & d[i]));
		}
		return hash.toString();
	}

	public void setBm(Bitmap b) {
		bm = b;
	}

}