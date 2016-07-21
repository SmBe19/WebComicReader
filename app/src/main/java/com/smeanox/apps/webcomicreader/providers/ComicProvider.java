package com.smeanox.apps.webcomicreader.providers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;

import com.smeanox.apps.webcomicreader.Comic;
import com.smeanox.apps.webcomicreader.ComicDownloadService;
import com.smeanox.apps.webcomicreader.R;

import java.util.HashMap;
import java.util.Map;

public abstract class ComicProvider {

	public enum ComicProviders{
		ruthe,
		xkcd,
		cnh,
		commitstrip,
		cuek,
		itsthetie,
		lovenstein,
		loadingartist,
	}
	private static Map<ComicProviders, ComicProvider> comicProvidersComicProviderMap = new HashMap<>();

	public static final int ID_END_FRONT = Integer.MAX_VALUE - 1;
	public static final int ID_END_BACK = Integer.MAX_VALUE - 2;

	private Context context;
	private Comic firstComic, lastComic;
	private SharedPreferences prefs;
	private ComicDbHelper dbh;
	private boolean downloading;

	private Map<Integer, Comic> comics;

	public ComicProvider(Context context) {
		this.context = context;

		init();
	}

	// call init() afterwards!!!
	protected ComicProvider(Context context, String tableName, ComicProviders providers) {
		this.context = context;
	}

	// getTableName / getComicProviders might not yet return the correct value
	protected void init(){
		comics = new HashMap<>();

		prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE);
		dbh = new ComicDbHelper(context);
		dbh.createTable();

		IntentFilter intentFilter = new IntentFilter(ComicDownloadService.BROADCAST_PROGRESS);
		LocalBroadcastManager.getInstance(context).registerReceiver(new ResponseReceiver(), intentFilter);

		comicProvidersComicProviderMap.put(getComicProviders(), this);
	}

	public static ComicProvider getComicProvider(ComicProviders comicProviders) {
		return comicProvidersComicProviderMap.get(comicProviders);
	}

	public abstract boolean needComicFile();

	public abstract String getPrefsName();

	public abstract String getTableName();

	public abstract ComicProviders getComicProviders();

	public abstract String getNotificationTitle();

	public abstract String getNotificationMessage(int id);

	public abstract String getNotificationMessageDone();

	public abstract String getStartUrl();

	public abstract String extractNextUrl(int id, String comicPage);

	public abstract String extractFileUrl(int id, String comicPage);

	public abstract String extractTitle(int id, String comicPage);

	public abstract String extractAltText(int id, String comicPage);

	public abstract String getFilePrefix();

	public abstract String getFileSuffix();

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public ComicDbHelper getDbh() {
		return dbh;
	}

	public Context getContext() {
		return context;
	}

	public Comic getFirstComic(){
		if(firstComic == null){
			SQLiteDatabase db = dbh.getReadableDatabase();
			String[] projection = {Comic.COLUMN_NAME_ID};
			String selection = Comic.COLUMN_NAME_PREV + " LIKE ?";
			String[] selectionArgs = {String.valueOf(ID_END_FRONT)};
			Cursor c = db.query(getTableName(), projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			if(c.getCount() > 0){
				firstComic = getComicById(c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_ID)));
			}
			c.close();
		}
		return firstComic;
	}

	public Comic getLastComic() {
		if(lastComic == null){
			SQLiteDatabase db = dbh.getReadableDatabase();
			String[] projection = {Comic.COLUMN_NAME_ID};
			String selection = Comic.COLUMN_NAME_NEXT + " LIKE ?";
			String[] selectionArgs = {String.valueOf(ID_END_BACK)};
			Cursor c = db.query(getTableName(), projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			if(c.getCount() > 0){
				lastComic = getComicById(c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_ID)));
			}
			c.close();
		}
		return lastComic;
	}

	public Comic getRandomComic(){
		SQLiteDatabase db = dbh.getReadableDatabase();
		String[] projection = {Comic.COLUMN_NAME_ID};
		String selection = "";
		String[] selectionArgs = {};
		Cursor c = db.query(getTableName(), projection, selection, selectionArgs, null, null, null);
		c.moveToFirst();
		if(c.getCount() == 0){
			c.close();
			return null;
		}
		int element = (int) (Math.random() * c.getCount());
		for (int i = 1; i < element; i++) {
			c.moveToNext();
		}
		Comic comic = getComicById(c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_ID)));
		c.close();
		return comic;
	}

	public Comic getComicById(int id){
		if(!comics.containsKey(id)){
			SQLiteDatabase db = dbh.getReadableDatabase();
			String[] projection = {
					Comic.COLUMN_NAME_ID,
					Comic.COLUMN_NAME_TITLE,
					Comic.COLUMN_NAME_ALT_TEXT,
					Comic.COLUMN_NAME_COMIC_URL,
					Comic.COLUMN_NAME_FILE_URL,
					Comic.COLUMN_NAME_LOCAL_FILE,
					Comic.COLUMN_NAME_PREV,
					Comic.COLUMN_NAME_NEXT,
			};
			String selection = Comic.COLUMN_NAME_ID + " LIKE ?";
			String[] selectionArgs = {String.valueOf(id)};
			Cursor c = db.query(getTableName(), projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			if(c.getCount() > 0){
				comics.put(id, new Comic(this,
						c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_ID)),
						c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_PREV)),
						c.getInt(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_NEXT)),
						c.getString(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_COMIC_URL)),
						c.getString(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_FILE_URL)),
						c.getString(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_TITLE)),
						c.getString(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_ALT_TEXT)),
						c.getString(c.getColumnIndexOrThrow(Comic.COLUMN_NAME_LOCAL_FILE))
						));
			}
			c.close();
		}
		return comics.get(id);
	}

	public void downloadAll(){
		downloadSome(-1);
	}

	public void downloadSome(){
		int downloadCount = getContext().getResources().getInteger(R.integer.downloadPerClick);
		downloadSome(downloadCount);
	}

	private void downloadSome(int count){
		if(downloading){
			return;
		}
		downloading = true;
		Intent intent = new Intent(context, ComicDownloadService.class);
		intent.putExtra(ComicDownloadService.EXTRA_PROVIDER, getComicProviders());
		intent.putExtra(ComicDownloadService.EXTRA_COMIC_COUNT, count);
		context.startService(intent);
	}

	public void deleteAll(){
		Comic c = getLastComic();
		if (c != null) {
			do {
				c.getLocalFile().delete();
			} while ((c = c.getPrevious()) != null);
		}

		lastComic = null;
		dbh.deleteTable();
	}

	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getBooleanExtra(ComicDownloadService.EXTRA_FINISHED, false)){
				downloading = false;
				lastComic = null;

				System.err.println("Finished Downloading");
			} else {
				int id = intent.getIntExtra(ComicDownloadService.EXTRA_PROGRESS, -1);
				if(id >= 0) {
				}
			}
		}
	}

	public class ComicDbHelper extends SQLiteOpenHelper {

		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "Comics.db";

		public ComicDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

		public void createTable(){
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL("CREATE TABLE IF NOT EXISTS " + getTableName() + " ("
							+ Comic.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
							+ Comic.COLUMN_NAME_TITLE + " TEXT, "
							+ Comic.COLUMN_NAME_ALT_TEXT + " TEXT, "
							+ Comic.COLUMN_NAME_COMIC_URL + " TEXT, "
							+ Comic.COLUMN_NAME_FILE_URL + " TEXT, "
							+ Comic.COLUMN_NAME_LOCAL_FILE + " TEXT, "
							+ Comic.COLUMN_NAME_PREV + " INTEGER, "
							+ Comic.COLUMN_NAME_NEXT + " INTEGER)"
			);
		}

		public void deleteTable(){
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL("DROP TABLE IF EXISTS " + getTableName());
			createTable();
		}
	}

}
