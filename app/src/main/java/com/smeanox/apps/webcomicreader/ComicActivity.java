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
import android.widget.TextView;

import java.io.File;
import java.util.Stack;

public class ComicActivity extends AppCompatActivity {

	private final String PREFS_CURRENT_COMIC = "currentComic";
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
		if(getResources().getString(R.string.MainButtonRuthe).equals(comicName)) {
			provider = new ComicProviderRuthe(this);
		} else if(getResources().getString(R.string.MainButtonCnH).equals(comicName)) {
			provider = new ComicProviderCnH(this);
		} else {
			provider = new ComicProviderXkcd(this);
		}
		getSupportActionBar().setTitle(comicName);

		prefs = getSharedPreferences(provider.getPrefsName(), MODE_PRIVATE);
		currentComic = provider.getComicById(prefs.getInt(PREFS_CURRENT_COMIC, 0));

		lastComics = new Stack<>();

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
			case android.R.id.home:
				this.finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
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
		TextView altView = (TextView) findViewById(R.id.altText);
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
			altView.setText(currentComic.getAltText());
		}

		prefs.edit().putInt(PREFS_CURRENT_COMIC, currentComic.getId()).commit();
	}

	public void prevComic(View view) {
		if (currentComic == null) {
			setCurrentComic(provider.getLastComic());
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
