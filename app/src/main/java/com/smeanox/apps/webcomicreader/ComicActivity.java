package com.smeanox.apps.webcomicreader;

import android.content.DialogInterface;
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

public class ComicActivity extends AppCompatActivity {

	private final String PREFS_CURRENT_COMIC = "currentComic";
	private Comic currentComic;
	private SharedPreferences prefs;

	private ComicProvider provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		provider = new ComicProviderRuthe(this);

		prefs = getSharedPreferences(provider.getPrefsName(), MODE_PRIVATE);
		currentComic = provider.getComicById(prefs.getInt(PREFS_CURRENT_COMIC, 0));

		updateCurrentComic();
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
				currentComic = provider.getFirstComic();
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
		if(currentComic == null){
			imageView.setImageURI(null);
			return;
		}
		File comicFile = currentComic.getLocalFile();
		if (!comicFile.exists()) {
			imageView.setImageURI(null);
		} else {
			imageView.setImageURI(Uri.fromFile(comicFile));
		}

		prefs.edit().putInt(PREFS_CURRENT_COMIC, currentComic.getId()).commit();
	}

	public void prevComic(View view) {
		if (currentComic == null) {
			currentComic = provider.getLastComic();
		} else if(currentComic.hasPrevious()){
			currentComic = currentComic.getPrevious();
			updateCurrentComic();
		}
	}

	public void randComic(View view) {
		currentComic = provider.getRandomComic();
		updateCurrentComic();
	}

	public void nextComic(View view) {
		if (currentComic == null) {
			currentComic = provider.getLastComic();
		} else if (currentComic.hasNext()) {
			currentComic = currentComic.getNext();
			updateCurrentComic();
		}
	}
}
