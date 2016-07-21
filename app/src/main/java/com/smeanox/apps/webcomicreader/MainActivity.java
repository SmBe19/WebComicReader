package com.smeanox.apps.webcomicreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.smeanox.apps.webcomicreader.providers.ComicProvider;

public class MainActivity extends AppCompatActivity {

	public static final String EXTRA_COMIC_NAME = "com.smeanox.apps.webcomicreader.COMIC_NAME";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		initButtons();
	}

	private void initButtons(){
		LinearLayout buttonList = (LinearLayout) findViewById(R.id.mainComicsList);
		assert buttonList != null;

		for (ComicProvider.ComicProviders providers : ComicProvider.ComicProviders.values()) {
			Button button = new Button(this);
			button.setText(ComicActivity.getStringByName(this, providers.name() + "MainButton"));
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openComic(v);
				}
			});

			buttonList.addView(button);
			//LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buttonList.getLayoutParams();
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			button.setLayoutParams(layoutParams);
		}
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
