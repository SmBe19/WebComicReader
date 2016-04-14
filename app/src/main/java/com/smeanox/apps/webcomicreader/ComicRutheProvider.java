package com.smeanox.apps.webcomicreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;

public class ComicRutheProvider {
	private Context context;
	private int lastComic;
	private SharedPreferences prefs;
	private boolean downloading;

	public ComicRutheProvider(Context context) {
		this.context = context;

		prefs = context.getSharedPreferences(ComicRuthe.PREFS_NAME, Context.MODE_PRIVATE);
		lastComic = prefs.getInt("provider_lastComic", -1);

		IntentFilter intentFilter = new IntentFilter(ComicRutheDownloadAllService.BROADCAST_PROGRESS);
		LocalBroadcastManager.getInstance(context).registerReceiver(new ResponseReceiver(), intentFilter);
	}

	public int getLastComic() {
		return lastComic;
	}

	public void downloadAll(){
		if(downloading){
			return;
		}
		downloading = true;
		Intent intent = new Intent(context, ComicRutheDownloadAllService.class);
		intent.putExtra(ComicRutheDownloadAllService.EXTRA_START, lastComic);
		context.startService(intent);
	}

	public void deleteAll(){
		for (int i = 0; i <= lastComic; i++) {
			boolean delete = getComicFile(i).delete();
		}
		lastComic = 0;
		prefs.edit().putInt("provider_lastComic", lastComic).commit();
	}

	public File getComicFile(int comic) {
		return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				context.getString(R.string.rutheDownloadPrefix) + String.format("%04d", comic) + context.getString(R.string.rutheDownloadPostfix));
	}

	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int newLastComic = intent.getIntExtra(ComicRutheDownloadAllService.EXTRA_PROGRESS, lastComic);
			if(newLastComic >= 0) {
				lastComic = newLastComic;
				prefs.edit().putInt("provider_lastComic", lastComic).commit();
			}
			if(intent.getBooleanExtra(ComicRutheDownloadAllService.EXTRA_FINISHED, false)){
				downloading = false;

				System.err.println("Downloaded until " + lastComic);
			}
		}
	}

}
