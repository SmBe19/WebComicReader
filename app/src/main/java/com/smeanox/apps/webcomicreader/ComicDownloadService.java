package com.smeanox.apps.webcomicreader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

public class ComicDownloadService extends IntentService {

	public static final String BROADCAST_PROGRESS = "com.smeanox.apps.webcomicreader.COMICDOWNLOAD";
	public static final String EXTRA_PROVIDER = "com.smeanox.apps.webcomicreader.COMICDOWNLOAD_PROVIDER";
	public static final String EXTRA_COMIC_COUNT = "com.smeanox.apps.webcomicreader.COMICDOWNLOAD_COMICCOUNT";
	public static final String EXTRA_PROGRESS = "com.smeanox.apps.webcomicreader.COMICDOWNLOAD_PROGRESS";
	public static final String EXTRA_FINISHED = "com.smeanox.apps.webcomicreader.COMICDOWNLOAD_FINISHED";
	private int notificationId;
	private NotificationManager notificationManager;

	public ComicDownloadService() {
		this("");
	}

	public ComicDownloadService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ComicProvider provider = ComicProvider.getComicProvider((ComicProvider.ComicProviders) intent.getSerializableExtra(EXTRA_PROVIDER));

		int prevId = ComicProvider.ID_END_FRONT;
		int curId = 1;
		String curUrl = provider.getStartUrl();
		Comic lastComic = provider.getLastComic();
		if(lastComic != null){
			prevId = lastComic.getPrevId();
			curId = lastComic.getId();
			curUrl = lastComic.getComicUrl();
		}

		int bad = 0;
		int downloadBadAllowed = getResources().getInteger(R.integer.downloadBadAllowed);

		notificationId = 1;
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setContentTitle(provider.getNotificationTitle())
				.setContentText(provider.getNotificationMessage(curId))
				.setOngoing(true);
		notificationManager.notify(notificationId, notBuilder.build());

		int downloaded = 0;
		int downloadCount = intent.getIntExtra(EXTRA_COMIC_COUNT, -1);
		int progressStep = getResources().getInteger(R.integer.downloadProgressNotificationStep);
		int lastProgress = -progressStep;

		while (true){
			String comicFile = getComicFile(curId, curUrl);
			String fileUrl = provider.extractFileUrl(curId, comicFile);
			String nextUrl = provider.extractNextUrl(curId, comicFile);
			String localFile = downloadFile(provider, curId, fileUrl);
			if (fileUrl != null && localFile != null) {
				if(provider.getComicById(curId) == null){
					SQLiteDatabase db = provider.getDbh().getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put(Comic.COLUMN_NAME_ID, curId);
					values.put(Comic.COLUMN_NAME_TITLE, provider.extractTitle(curId, comicFile));
					values.put(Comic.COLUMN_NAME_ALT_TEXT, provider.extractAltText(curId, comicFile));
					values.put(Comic.COLUMN_NAME_COMIC_URL, curUrl);
					values.put(Comic.COLUMN_NAME_FILE_URL, fileUrl);
					values.put(Comic.COLUMN_NAME_LOCAL_FILE, localFile);
					values.put(Comic.COLUMN_NAME_PREV, prevId);
					values.put(Comic.COLUMN_NAME_NEXT, ComicProvider.ID_END_BACK);

					long newRowId = db.insert(provider.getTableName(), null, values);

					if(newRowId != curId){
						System.err.println("Id mismatch: " + newRowId + " / " + curId);
						if(newRowId > curId) {
							curId = (int) newRowId;
						}
					}

					values.clear();
					values.put(Comic.COLUMN_NAME_NEXT, curId);
					String selection = Comic.COLUMN_NAME_ID + " LIKE ?";
					String[] selectionArgs = {String.valueOf(prevId)};
					int cnt = db.update(provider.getTableName(), values, selection, selectionArgs);

					notifyNewComic(curId, false);

					if(curId - lastProgress > progressStep){
						notBuilder.setContentText(provider.getNotificationMessage(curId));
						notificationManager.notify(notificationId, notBuilder.build());
						lastProgress = curId;
					}
				}
				prevId = curId;
				downloaded++;
				curId++;

				if(downloadCount > 0 && downloaded > downloadCount){
					break;
				}
			} else {
				bad++;
				if (nextUrl == null || bad > downloadBadAllowed) {
					break;
				}
			}
			// TODO check whether old and new url are the same (difficult right now for ruthe)
			curUrl = nextUrl;
		}

		notifyNewComic(-1, true);

		notBuilder
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				.setContentText(provider.getNotificationMessageDone())
				.setOngoing(false);
		notificationManager.notify(notificationId, notBuilder.build());
	}

	private void notifyNewComic(int id, boolean finished){
		Intent progressIntent = new Intent(BROADCAST_PROGRESS);
		progressIntent.putExtra(EXTRA_PROGRESS, id);
		progressIntent.putExtra(EXTRA_FINISHED, finished);
		LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);
	}

	@Override
	public void onDestroy() {
		//notificationManager.cancel(notificationId);
		super.onDestroy();
	}

	private String getComicFile(int id, String comicUrl) {
		if (comicUrl.length() == 0) {
			return null;
		}
		try {
			URL url = new URL(comicUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);

			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.err.println(id + " (" + comicUrl + ")" + ": Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
				return null;
			}

			InputStream input = new BufferedInputStream(connection.getInputStream());
			StringBuilder sb = new StringBuilder();

			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1) {
				sb.append(new String(data, 0, count));
			}

			input.close();
			return sb.toString();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String downloadFile(ComicProvider provider, int id, String fileUrl){
		try {
			System.err.println(fileUrl);
			URL url = new URL(fileUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);

			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.err.println(id + " (" + fileUrl + ")" + ": Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
				return null;
			}

			String fileName = provider.getFilePrefix() + String.format("%08d", id) + provider.getFileSuffix();
			File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
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

			return fileName;
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
