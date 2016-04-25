package com.smeanox.apps.webcomicreader;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComicProviderCnH extends ComicProviderRegexer {

	Pattern nextPattern, imgPattern, titlePattern, altPattern;

	public ComicProviderCnH(Context context) {
		super(context);
	}

	@Override
	protected String getNextRegex() {
		return getContext().getResources().getString(R.string.cnhNextRegex);
	}

	@Override
	protected String getImgRegex() {
		return getContext().getResources().getString(R.string.cnhImgRegex);
	}

	@Override
	protected String getTitleRegex() {
		return "";
	}

	@Override
	protected String getAltRegex() {
		return getContext().getResources().getString(R.string.cnhAltRegex);
	}

	@Override
	protected String getFullUrl(String nextUrl) {
		return getContext().getString(R.string.cnh_url_prefix) + nextUrl + getContext().getString(R.string.cnh_url_postfix);
	}

	@Override
	protected boolean isLastUrl(String nextUrl) {
		return false;
	}

	@Override
	public boolean needComicFile() {
		return true;
	}

	@Override
	public String getPrefsName() {
		return "com.smeanox.apps.webcomicreader.cnh";
	}

	@Override
	public String getTableName() {
		return "cnh";
	}

	@Override
	public ComicProviders getComicProviders() {
		return ComicProviders.cnh;
	}

	@Override
	public String getNotificationTitle() {
		return getContext().getString(R.string.cnhNotTitle);
	}

	@Override
	public String getNotificationMessage(int id) {
		return getContext().getString(R.string.cnhNotText) + " " + id;
	}

	@Override
	public String getNotificationMessageDone() {
		return getContext().getString(R.string.cnhNotTextFinished);
	}

	@Override
	public String getStartUrl() {
		return getContext().getString(R.string.cnh_first_url);
	}

	@Override
	public String extractFileUrl(int id, String comicPage) {
		return "http:" + super.extractFileUrl(id, comicPage);
	}

	@Override
	public String extractTitle(int id, String comicPage) {
		return "Cyanide & Happiness " + id;
	}

	@Override
	public String getFilePrefix() {
		return getContext().getResources().getString(R.string.cnhDownloadPrefix);
	}

	@Override
	public String getFileSuffix() {
		return getContext().getResources().getString(R.string.cnhDownloadPostfix);
	}
}
