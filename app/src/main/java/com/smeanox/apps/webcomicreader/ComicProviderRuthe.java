package com.smeanox.apps.webcomicreader;

import android.content.Context;

public class ComicProviderRuthe extends ComicProvider {

	public ComicProviderRuthe(Context context) {
		super(context);
	}

	@Override
	public String getPrefsName() {
		return "com.smeanox.apps.webcomicreader.ruthe";
	}

	@Override
	public String getTableName() {
		return "ruthe";
	}

	@Override
	public ComicProvider.ComicProviders getComicProviders() {
		return ComicProviders.ruthe;
	}

	@Override
	public String getNotificationTitle() {
		return getContext().getString(R.string.RutheNotTitle);
	}

	@Override
	public String getNotificationMessage() {
		return getContext().getString(R.string.RutheNotText);
	}

	@Override
	public String getNotificationMessageDone() {
		return getContext().getString(R.string.RutheNotTextFinished);
	}

	@Override
	public String getStartUrl() {
		return "";
	}

	@Override
	public String extractNextUrl(int id, String comicPage) {
		return "";
	}

	@Override
	public String extractFileUrl(int id, String comicPage) {
		return getContext().getString(R.string.ruthe_url_prefix) + String.format("%04d", id) + getContext().getString(R.string.ruthe_url_postfix);
	}

	@Override
	public String extractTitle(int id, String comicPage) {
		return "Ruthe " + id;
	}

	@Override
	public String extractAltText(int id, String comicPage) {
		return "Ruthe " + id;
	}

	@Override
	public String getFilePrefix() {
		return "ruthe";
	}

	@Override
	public String getFileSuffix() {
		return ".jpg";
	}
}
