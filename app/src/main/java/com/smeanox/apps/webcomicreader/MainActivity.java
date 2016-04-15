package com.smeanox.apps.webcomicreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

	public static final String EXTRA_COMIC_NAME = "com.smeanox.apps.webcomicreader.COMIC_NAME";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
			case R.id.action_settings:
				Intent intent = new Intent(this, Settings.class);
				startActivity(intent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void openComic(View view) {
		Intent intent = new Intent(this, ComicActivity.class);
		intent.putExtra(EXTRA_COMIC_NAME, ((Button) view).getText());
		startActivity(intent);
	}
}
