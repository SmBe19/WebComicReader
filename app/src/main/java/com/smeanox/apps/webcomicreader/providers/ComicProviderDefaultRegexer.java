package com.smeanox.apps.webcomicreader.providers;

import android.content.Context;
import android.content.res.Resources;

public class ComicProviderDefaultRegexer extends ComicProviderRegexer{

	private String prefix;

	public ComicProviderDefaultRegexer(Context context, String prefix) {
		super(context, prefix, ComicProviders.valueOf(prefix));
		this.prefix = prefix;

		init();
		initPatterns();
	}

	private String getStringByName(String name){
		try {
			int resId = getContext().getResources().getIdentifier(prefix + name, "string", getContext().getPackageName());
			return getContext().getResources().getString(resId);
		} catch (Resources.NotFoundException e) {
			throw e;
			//return "";
		}
	}

	@Override
	protected String getNextRegex() {
		return getStringByName("NextRegex");
	}

	@Override
	protected String getImgRegex() {
		return getStringByName("ImgRegex");
	}

	@Override
	protected String getTitleRegex() {
		return getStringByName("TitleRegex");
	}

	@Override
	protected String getAltRegex() {
		return getStringByName("AltRegex");
	}

	@Override
	protected String getFullUrl(String nextUrl) {
		return getStringByName("UrlPrefix") + nextUrl + getStringByName("UrlPostfix");
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
		return "com.smeanox.apps.webcomicreader." + prefix;
	}

	@Override
	public String getTableName() {
		return prefix;
	}

	@Override
	public ComicProviders getComicProviders() {
		return ComicProviders.valueOf(prefix);
	}

	@Override
	public String getNotificationTitle() {
		return getStringByName("NotTitle");
	}

	@Override
	public String getNotificationMessage(int id) {
		return getStringByName("NotText") + " " + id;
	}

	@Override
	public String getNotificationMessageDone() {
		return getStringByName("NotTextFinished");
	}

	@Override
	public String getStartUrl() {
		return getStringByName("FirstUrl");
	}

	@Override
	public String getFilePrefix() {
		return getStringByName("DownloadPrefix");
	}

	@Override
	public String getFileSuffix() {
		return getStringByName("DownloadPostfix");
	}
}
