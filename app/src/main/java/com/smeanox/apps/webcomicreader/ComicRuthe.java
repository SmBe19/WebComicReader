package com.smeanox.apps.webcomicreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class ComicRuthe extends AppCompatActivity {

	public static final String PREFS_NAME = "com.smeanox.apps.webcomicreader.ruthe";

	private int currentComic;
	private SharedPreferences prefs;

	private ComicRutheProvider provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_ruthe);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		provider = new ComicRutheProvider(this);

		prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		currentComic = prefs.getInt("currentComic", provider.getLastComic());

		updateCurrentComic();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_comic_ruthe, menu);
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
			case R.id.action_delete:
				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.rutheDeleteDialogTitle))
						.setMessage(getString(R.string.rutheDeleteDialogMessage))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(getString(R.string.rutheDeleteDialogPositive), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								provider.deleteAll();
								currentComic = 0;
								updateCurrentComic();
							}
						})
						.setNegativeButton(getString(R.string.rutheDeleteDialogNegative), null).show();
				return true;
			case R.id.action_first:
				currentComic = 1;
				updateCurrentComic();
				return true;
			case R.id.action_last:
				currentComic = provider.getLastComic();
				updateCurrentComic();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void updateCurrentComic() {
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		File comicFile = provider.getComicFile(currentComic);
		if (!comicFile.exists()) {
			imageView.setImageURI(null);
		} else {
			imageView.setImageURI(Uri.fromFile(comicFile));
		}

		prefs.edit().putInt("currentComic", currentComic).commit();
	}

	public void prevRuthe(View view) {
		if(currentComic > 1){
			currentComic--;
			updateCurrentComic();
		}
	}

	public void randRuthe(View view) {
		currentComic = (int) (Math.random() * provider.getLastComic())+1;
		updateCurrentComic();
	}

	public void nextRuthe(View view) {
		if(currentComic < provider.getLastComic()){
			currentComic++;
			updateCurrentComic();
		}
	}
}
