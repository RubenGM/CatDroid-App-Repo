package org.catdroid.apps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.util.Log;

public class Downloader {
	public static final String TAG = "Downloader";
	private static boolean cancelled = false;
	public static void cancel() { cancelled = true; }
	public boolean isCancelled() { return cancelled; }
	private static final int HTTP_STATUS_OK = 200;
	private static String sUserAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.125 Safari/533.4";
	private static byte[] sBuffer = new byte[8 * 1024];
	private static int totalReadBytes = 0;
	private static int readBytes = 0;
	private static ProgressDialog pd;
	private static InputStream inputStream;
	private static long length;
	private static ByteArrayOutputStream content;
	private static boolean failed = false;
	private static Activity activity;
	private static boolean hasNewDialog = true;
	private static Runnable descargaConTexto = new Runnable() {
		public void run() {
			try {
				while ((readBytes = inputStream.read(sBuffer)) != -1) {
					content.write(sBuffer, 0, readBytes);
					totalReadBytes += readBytes;
					activity.runOnUiThread(new Runnable() {
						public void run() {
							pd.setMessage(activity.getString(R.string.descargado) + " " + Math.round(totalReadBytes/1024) + "KB");
						}
					});
				}
			} catch (IOException e) {
				failed = true;
			}
		}
	};

	public static ProgressDialog getNewDialog() {
		setHasNewDialog(false);
		return pd;
	}

	public static String getUrlContent(String url, ProgressDialog _pd, Activity _activity) {
		totalReadBytes = 0;
		pd = _pd;
		activity = _activity;
		if (sUserAgent  == null) {
			return "";
		}

		// Create client and set our specific user-agent string
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent", sUserAgent);

		try {
			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				return "";
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			inputStream = entity.getContent();

			content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			readBytes = 0;
			descargaConTexto.run();
			if(failed) {
				client = null;
				request = null;
				System.gc();
				return "";
			}
			client = null;
			request = null;
			response = null;
			status = null;
			entity = null;
			inputStream = null;
			System.gc();
			return new String(content.toByteArray());
		} catch (IOException e) {
			client = null;
			request = null;
			System.gc();
			return "";
		}
	}

	public static String download(String uri) {
		String resultado = "";
		try {
			URL url = new URL(uri);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();
				resultado = convertStreamToString(in);
			}
		}
		catch(Exception ex) {
		}
		return resultado;
	}

	private static long getFileSize(String url) {
		// Create client and set our specific user-agent string
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent", sUserAgent);
		try {
			HttpResponse response = client.execute(request);

			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				return -1;
			}

			HttpEntity entity = response.getEntity();
			length = entity.getContentLength();
			entity = null;
			response = null;
			status = null;
			client = null;
			return length;
		} catch (IOException e) {
			return -1;
		}
	}

	public static File downloadBinary(Activity activity, ProgressDialog pd, String uri, String destino) {
		Downloader.activity = activity;
		cancelled = false;
		int count = 0;
		try{
			Downloader.pd = pd;
			java.io.BufferedInputStream in = new java.io.BufferedInputStream(new  java.net.URL(uri).openStream());
			java.io.FileOutputStream fos = new java.io.FileOutputStream(destino);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,8192);
			readBytes = 0;
			long init = System.currentTimeMillis();
			long tiempo;
			long speed;
			int size = 32768;
			byte data[] = new byte[size];
			long total = getFileSize(uri);
			if(total == -1) {
				while(count >= 0 && !cancelled)
				{
					count = in.read(data,0,size);
					if(count != -1) {
						bout.write(data,0,count);
						readBytes += count;
						tiempo = (System.currentTimeMillis() - init) / 1000;
						if(tiempo == 0) tiempo = 1;
						speed = Math.round((readBytes/1024) / tiempo);
						pdMessage = activity.getString(R.string.descargado) + " " + Math.round(readBytes/1024) + "KB @ " + speed + " KBps";
						activity.runOnUiThread(changeDialogText);
					}
				}
			}
			else {
				activity.runOnUiThread(addProgressDialog);
				long falta;
				String velocidad = "?";
				String remaining = "?";
				long ultimoSec = System.currentTimeMillis();
				int ultimoReadBytes = 0;
				long ultimoTiempo = 0;
				pdMessage = activity.getString(R.string.espere);
				while(count >= 0 && !cancelled)
				{
					count = in.read(data,0,size);
					if(count != -1) {
						bout.write(data,0,count);
						readBytes += count;
						ultimoTiempo = System.currentTimeMillis() - ultimoSec;
						if(ultimoTiempo > 1000) {
							speed = (readBytes - ultimoReadBytes) / (ultimoTiempo / 1000);
							speed = speed / 1024;
							falta = (total - readBytes) / 1024;
							if(speed == 0) speed = 1;
							velocidad = Math.round(speed) + "";
							remaining = Math.round(falta / speed) + "";
							ultimoSec = System.currentTimeMillis();
							ultimoReadBytes = readBytes;
							pdMessage = velocidad + " KBps, " + activity.getString(R.string.remaining) + " " + remaining + " " + activity.getString(R.string.seconds);
						}
						activity.runOnUiThread(changeDialogProgress);
					}
				}
			}
			bout.close();
			fos.close();
			in.close();
		}catch(Exception ex) {
		}
		return new File(destino);
	}

	private static Runnable changeDialogProgress = new Runnable() {
		@Override
		public void run() {
			if(pd != null) {
				try{
					pd.setProgress(readBytes);
					pd.setTitle(pdMessage);
				}catch(Exception ex) {

				}
			}
		}
	};

	private static Runnable addProgressDialog = new Runnable() {
		@Override
		public void run() {
			pd.dismiss();
			pd = null;
			hasNewDialog = true;
			pd = new ProgressDialog(activity);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax((int)length);
			pd.setTitle(R.string.descargando);
			pd.setOnCancelListener(new OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					Downloader.cancel();
				}
			});
			pd.show();
		}
	};

	private static String pdMessage = "";

	private static Runnable changeDialogText = new Runnable() {
		@Override
		public void run() {
			pd.setMessage(pdMessage);
		}
	};

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static void setHasNewDialog(boolean hasNewDialog) {
		Downloader.hasNewDialog = hasNewDialog;
	}

	public static boolean isHasNewDialog() {
		return hasNewDialog;
	}



	public void downloadBinaryNotification(Context mContext, String url, String filename, int idNotificacion, Notification noti, App app) {
		int bufferSize = 32768;
		cancelled = false;
		File f;
		boolean copiado = false;
		long fSize = getFileSize(url);
		long rSize = 0;
		int count = 0;
		int progreso = 0;
		Intent intent = new Intent();
		long time = System.currentTimeMillis();
		BufferedInputStream in = null;
		noti = List.notify(noti, "Preparando descarga", "Preparando descarga", app.getName(), idNotificacion, mContext, intent);
		try{
			boolean sigueIntentando = true;
			int intentosStream = 0;
			while(sigueIntentando && intentosStream < 10) {
				try{
					Log.i(TAG, "Intentando abrir el input stream");
					intentosStream++;
					in = new BufferedInputStream(new URL(url).openStream(), bufferSize);
					sigueIntentando = false;
				}catch(Exception ex) {
					Log.e(TAG, "Error!: " + ex.getLocalizedMessage());
					try{
						Thread.sleep(1000);
					}catch(Exception exx) {}
				}
			}
			Log.i(TAG, "Input stream listo");
			f = new File(filename);
			if(f.exists()) {
				f.delete();
				f = new File(filename);
			}
			if(!f.exists()) f.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename, copiado);
			BufferedOutputStream bout = new BufferedOutputStream(fos,bufferSize);
			byte data[] = new byte[bufferSize];
			filename = f.getName();
			intent = new Intent(); //TODO: Un intent mejor?
			//intent.putExtra(BackgroundDownloader.PARAM_ID, descarga.getId());
			intent.setAction("actionstring" + System.currentTimeMillis());

			readBytes = 0;
			long ultimoReadBytes = 0;
			int i = 0;
			while(count >= 0 && !cancelled && rSize < fSize)
			{
				int sinConexionErr = 0;
				boolean continua = false;
				while(sinConexionErr <= 10 && !continua) {
					try{
						count = in.read(data,0,bufferSize);
						continua = count > -1;
					}catch(Exception ex) {
						Log.e(TAG, "Error descargando: " + ex.getLocalizedMessage());
					}
					if(rSize < fSize) {
						if(!continua) {
							sinConexionErr++;
							try{
								Log.i(TAG, "Esperaremos 5 segundos.");
								Thread.sleep(5000);
								in = new BufferedInputStream(new URL(url).openStream(), bufferSize);
								in.skip(rSize);
								Log.i(TAG, "Reabierto stream, saltado hasta " + rSize);
							}catch(Exception exx) {
								Log.e(TAG, "Error: " + exx.getLocalizedMessage());
							}
						}
					} else {
						Log.i(TAG, "Completado!");
						continua = true;
					}
				}
				if(count != -1) {
					bout.write(data,0,count);
				}
				rSize += count;
				if(System.currentTimeMillis() - time >= 1000){
					long tiempo = System.currentTimeMillis() - time;
					double speed = ((rSize - ultimoReadBytes) / (tiempo)) * 1000;
					if(speed == 0) speed = 1;
					long tiempoEstimado = Math.round((fSize - rSize) / speed);
					speed = speed / 1024;
					if(speed == 0) speed = 1;
					String velocidad = Math.round(speed) + " KB/s";
					try{
						time = System.currentTimeMillis();
						ultimoReadBytes = rSize;
						i++;
					}catch(Exception ex) {

					}
					if(!cancelled) {
						if(fSize <= 0) progreso = 0;
						else progreso = (int) ((rSize * 100) / fSize);
						velocidad = velocidad + " | " + progreso + "% | " + corrigeTiempo(tiempoEstimado) + " | " + Math.round(rSize / (1024)) + "KB/" + Math.round(fSize / (1024)) + "KB";
						noti = List.notify(noti, mContext.getString(R.string.descargando) + ": " + app.getName(), app.getName(), velocidad, idNotificacion, mContext, intent);
					}
					bout.flush();
				}
			}
			bout.flush();
			bout.close();
			fos.close();
			in.close();
			if(cancelled) {
				if(f.exists()) {
					f.delete();
				}
				NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(idNotificacion);
			} else {
				if(!cancelled) {
					if(fSize <= 0) progreso = 0;
					else progreso = (int) ((rSize * 100) / fSize);
					noti = List.notify(noti, mContext.getString(R.string.descargando) + ": " + app.getName(), app.getName(), progreso + "%", idNotificacion, mContext, intent);
				}
			}
		}catch(Exception ex) {
			Log.e(TAG, "Error Download Binary Notification: " + ex.getLocalizedMessage());
			Intent intentVerDescarga = new Intent();
			noti = List.notify(noti, "Error", "Error: " + filename, "0 KB/s", idNotificacion, mContext, intentVerDescarga);
			return;
		}
		Log.i(TAG, "Fin de la descarga: " + url + " :: " + idNotificacion);
		if(!cancelled) {
			noti = List.notify(noti, mContext.getString(R.string.descargaCompletada), app.getName(), "", idNotificacion, mContext, intent);
		}
	}

	private String corrigeTiempo(long tiempoEstimado) {
		String tiempo = tiempoEstimado + "s";
		if(tiempoEstimado >= 3600) {
			int horas = Math.round(tiempoEstimado / 3600);
			tiempoEstimado -= horas * 3600;
			int minutos = Math.round(tiempoEstimado / 60);
			tiempoEstimado -= minutos * 60;
			tiempo = horas + "h, " + minutos + "m, " + tiempoEstimado + "s";
		}
		else if(tiempoEstimado >= 60) {
			int minutos = Math.round(tiempoEstimado / 60);
			tiempoEstimado -= minutos * 60;
			tiempo = minutos + "m, " + tiempoEstimado + "s";
		}
		return tiempo;
	}
}
