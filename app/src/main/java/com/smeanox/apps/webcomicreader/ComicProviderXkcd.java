package com.smeanox.apps.webcomicreader;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComicProviderXkcd extends ComicProviderRegexer {

	public ComicProviderXkcd(Context context) {
		super(context);
	}

	@Override
	protected String getNextRegex() {
		return getContext().getResources().getString(R.string.xkcdNextRegex);
	}

	@Override
	protected String getImgRegex() {
		return getContext().getResources().getString(R.string.xkcdImgRegex);
	}

	@Override
	protected String getTitleRegex() {
		return getContext().getResources().getString(R.string.xkcdTitleRegex);
	}

	@Override
	protected String getAltRegex() {
		return getContext().getResources().getString(R.string.xkcdAltRegex);
	}

	@Override
	protected String getFullUrl(String nextUrl) {
		return getContext().getString(R.string.xkcd_url_prefix) + nextUrl + getContext().getString(R.string.xkcd_url_postfix);
	}

	@Override
	protected boolean isLastUrl(String nextUrl) {
		return "#".equals(nextUrl);
	}

	@Override
	public boolean needComicFile() {
		return true;
	}

	@Override
	public String getPrefsName() {
		return "com.smeanox.apps.webcomicreader.xkcd";
	}

	@Override
	public String getTableName() {
		return "xkcd";
	}

	@Override
	public ComicProviders getComicProviders() {
		return ComicProviders.xkcd;
	}

	@Override
	public String getNotificationTitle() {
		return getContext().getString(R.string.xkcdNotTitle);
	}

	@Override
	public String getNotificationMessage(int id) {
		return getContext().getString(R.string.xkcdNotText) + " " + id;
	}

	@Override
	public String getNotificationMessageDone() {
		return getContext().getString(R.string.xkcdNotTextFinished);
	}

	@Override
	public String getStartUrl() {
		return getContext().getString(R.string.xkcd_first_url);
	}

	@Override
	public String getFilePrefix() {
		return getContext().getResources().getString(R.string.xkcdDownloadPrefix);
	}

	@Override
	public String getFileSuffix() {
		return getContext().getResources().getString(R.string.xkcdDownloadPostfix);
	}
}
