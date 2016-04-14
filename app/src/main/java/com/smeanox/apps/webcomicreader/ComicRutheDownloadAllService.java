package com.smeanox.apps.webcomicreader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ComicRutheDownloadAllService extends IntentService {

	public static final String BROADCAST_PROGRESS = "com.smeanox.apps.webcomicreader.COMICRUTHEDOWNLOADALL";
	public static final String EXTRA_START = "com.smeanox.apps.webcomicreader.COMICRUTHEDOWNLOADALL_START";
	public static final String EXTRA_PROGRESS = "com.smeanox.apps.webcomicreader.COMICRUTHEDOWNLOADALL_PROGRESS";
	public static final String EXTRA_FINISHED = "com.smeanox.apps.webcomicreader.COMICRUTHEDOWNLOADALL_FINISHED";
	private int notificationId;
	private NotificationManager notificationManager;

	public ComicRutheDownloadAllService() {
		this("");
	}

	public ComicRutheDownloadAllService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int start = intent.getIntExtra(EXTRA_START, 0);
		int bad = 0;
		int lastOk = -1;

		int downloadPerClick = getResources().getInteger(R.integer.downloadPerClick);
		int downloadBadAllowed = getResources().getInteger(R.integer.downloadBadAllowed);
		int nextNotificationStep = getResources().getInteger(R.integer.downloadProgressNotificationStep);
		int nextNotification = nextNotificationStep;

		notificationId = 1;
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setContentTitle(getString(R.string.RutheNotTitle))
				.setContentText(getString(R.string.RutheNotText) + " " + start + "/" + (start + downloadPerClick-1))
				.setOngoing(true);
		notificationManager.notify(notificationId, notBuilder.build());

		for (int i = start; i < start + downloadPerClick; i++) {
			if(!downloadFile(i)){
				bad++;
				if(bad > downloadBadAllowed){
					break;
				}
				continue;
			}
			bad = 0;
			lastOk = i;

			Intent progressIntent = new Intent(BROADCAST_PROGRESS);
			progressIntent.putExtra(EXTRA_PROGRESS, i);
			progressIntent.putExtra(EXTRA_FINISHED, false);
			LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

			nextNotification--;
			if(nextNotification == 0) {
				notBuilder.setContentText(getString(R.string.RutheNotText) + " " + i + "/" + (start + downloadPerClick - 1));
				notificationManager.notify(notificationId, notBuilder.build());
				nextNotification = nextNotificationStep;
			}
		}

		Intent progressIntent = new Intent(BROADCAST_PROGRESS);
		progressIntent.putExtra(EXTRA_PROGRESS, lastOk);
		progressIntent.putExtra(EXTRA_FINISHED, true);
		LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

		notBuilder
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				.setContentText(getString(R.string.RutheNotTextFinished))
				.setOngoing(false);
		notificationManager.notify(notificationId, notBuilder.build());
	}



	@Override
	public void onDestroy() {
		notificationManager.cancel(notificationId);
		super.onDestroy();
	}

	private boolean downloadFile(int comic){
		try {
			URL url = new URL(getString(R.string.ruthe_url_prefix) + String.format("%04d", comic) + getString(R.string.ruthe_url_postfix));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);

			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.err.println(comic + ": Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
				return false;
			}

			File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), getString(R.string.rutheDownloadPrefix) + String.format("%04d", comic) + getString(R.string.rutheDownloadPostfix));
			InputStream input = new BufferedInputStream(connection.getInputStream());
			OutputStream output = new FileOutputStream(file);

			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
