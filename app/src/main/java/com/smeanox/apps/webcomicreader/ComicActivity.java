package com.smeanox.apps.webcomicreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.smeanox.apps.webcomicreader.providers.ComicProvider;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

public class ComicActivity extends AppCompatActivity {

	private final String PREFS_CURRENT_COMIC = "currentComic";
	private final String STATE_LAST_COMICS = "lastComics";
	private Comic currentComic;
	private SharedPreferences prefs;

	private ComicProvider provider;

	private Stack<Comic> lastComics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String comicName = getIntent().getStringExtra(MainActivity.EXTRA_COMIC_NAME);

		for (ComicProvider.ComicProviders providers : ComicProvider.ComicProviders.values()) {
			if(getStringByName(this, providers.name() + "MainButton").equals(comicName)){
				try {
					String clazzName = "com.smeanox.apps.webcomicreader.providers.ComicProvider_" + providers.name();
					Class<?> clazz = Class.forName(clazzName);
					Constructor<?> constructor = clazz.getConstructor(Context.class);
					provider = (ComicProvider) constructor.newInstance(this);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		getSupportActionBar().setTitle(comicName);

		prefs = getSharedPreferences(provider.getPrefsName(), MODE_PRIVATE);
		currentComic = provider.getComicById(prefs.getInt(PREFS_CURRENT_COMIC, 0));

		lastComics = new Stack<>();

		if (savedInstanceState != null) {
			int[] last = savedInstanceState.getIntArray(STATE_LAST_COMICS);
			if(last != null) {
					for (int i = last.length - 1; i >= 0; i--) {
					lastComics.push(provider.getComicById(last[i]));
				}
			}
		}

		updateCurrentComic();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		int size = lastComics.size();
		int[] last = new int[size];
		for (int i = 0; i < size; i++) {
			last[i] = lastComics.pop().getId();
		}
		outState.putIntArray(STATE_LAST_COMICS, last);

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_comic, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				Snackbar.make(findViewById(R.id.imageView), "Nope", Snackbar.LENGTH_SHORT).show();
				return true;
			case R.id.action_download_all:
				provider.downloadAll();
				return true;
			case R.id.action_download_some:
				provider.downloadSome();
				return true;
			case R.id.action_delete:
				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.deleteDialogTitle))
						.setMessage(getString(R.string.deleteDialogMessage))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(getString(R.string.deleteDialogPositive), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								provider.deleteAll();
								currentComic = null;
								updateCurrentComic();
							}
						})
						.setNegativeButton(getString(R.string.eleteDialogNegative), null).show();
				return true;
			case R.id.action_first:
				setCurrentComic(provider.getFirstComic());
				return true;
			case R.id.action_last:
				setCurrentComic(provider.getLastComic());
				return true;
			case R.id.action_goto:
				gotoComic();
				return true;
			case android.R.id.home:
				this.finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public static String getStringByName(Context context, String name){
		try {
			int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
			return context.getResources().getString(resId);
		} catch (Resources.NotFoundException e) {
			throw e;
			//return "";
		}
	}

	private void gotoComic() {
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setHint(R.string.goto_dialog_hint);
		new AlertDialog.Builder(this)
				.setView(input)
				.setTitle(getString(R.string.goto_dialog_title))
				.setPositiveButton(getString(R.string.goto_dialog_positive), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Comic comic = provider.getComicById(Integer.parseInt(input.getText().toString()));
						if(comic != null) {
							setCurrentComic(comic);
						} else {
							Snackbar.make(findViewById(R.id.imageView), "Nope", Snackbar.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton(getString(R.string.goto_dialog_negative), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// i don't care
					}
				})
				.show();
	}

	private void setCurrentComic(Comic comic){
		if(currentComic != null){
			lastComics.push(currentComic);
		}
		currentComic = comic;
		updateCurrentComic();
	}

	private void updateCurrentComic() {
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		TextView titleView = (TextView) findViewById(R.id.titleText);
		TextView comicidView = (TextView) findViewById(R.id.comicidText);
		TextView altView = (TextView) findViewById(R.id.altText);
		assert imageView != null && titleView != null && comicidView != null && altView != null;
		if(currentComic == null){
			imageView.setImageURI(null);
			return;
		}
		File comicFile = currentComic.getLocalFile();
		if (!comicFile.exists()) {
			imageView.setImageURI(null);
		} else {
			imageView.setImageURI(Uri.fromFile(comicFile));
			titleView.setText(currentComic.getTitle());
			comicidView.setText(String.valueOf(currentComic.getId()));
			altView.setText(currentComic.getAltText());
		}

		prefs.edit().putInt(PREFS_CURRENT_COMIC, currentComic.getId()).commit();
	}

	public void prevComic(View view) {
		if (currentComic == null) {
			setCurrentComic(provider.getFirstComic());
		} else if(currentComic.hasPrevious()){
			setCurrentComic(currentComic.getPrevious());
		}
	}

	public void randComic(View view) {
		setCurrentComic(provider.getRandomComic());
	}

	public void nextComic(View view) {
		if (currentComic == null) {
			setCurrentComic(provider.getLastComic());
		} else if (currentComic.hasNext()) {
			setCurrentComic(currentComic.getNext());
		}
	}

	@Override
	public void onBackPressed() {
		if(lastComics.isEmpty()) {
			super.onBackPressed();
		} else {
			currentComic = lastComics.pop();
			updateCurrentComic();
		}
	}
}
