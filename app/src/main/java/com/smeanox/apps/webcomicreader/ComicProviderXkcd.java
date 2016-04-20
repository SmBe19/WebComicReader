package com.smeanox.apps.webcomicreader;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComicProviderXkcd extends ComicProvider {

	Pattern nextPattern, imgPattern, titlePattern, altPattern;

	public ComicProviderXkcd(Context context) {
		super(context);

		String nextRegex = context.getResources().getString(R.string.xkcdNextRegex);
		String imgRegex = context.getResources().getString(R.string.xkcdImgRegex);
		String titleRegex = context.getResources().getString(R.string.xkcdTitleRegex);
		String altRegex = context.getResources().getString(R.string.xkcdAltRegex);

		System.err.println(nextRegex);
		System.err.println(imgRegex);
		System.err.println(titleRegex);
		System.err.println(altRegex);

		nextPattern = Pattern.compile(nextRegex);
		imgPattern = Pattern.compile(imgRegex);
		titlePattern = Pattern.compile(titleRegex);
		altPattern = Pattern.compile(altRegex);
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
		return getContext().getString(R.string.xkcd_url_prefix) + "/" + 1 + "/" + getContext().getString(R.string.xkcd_url_postfix);
	}

	@Override
	public String extractNextUrl(int id, String comicPage) {
		Matcher matcher = nextPattern.matcher(comicPage);
		if(matcher.find()){
			return getContext().getString(R.string.xkcd_url_prefix) + matcher.group(1) + getContext().getString(R.string.xkcd_url_postfix);
		}
		return null;
	}

	@Override
	public String extractFileUrl(int id, String comicPage) {
		Matcher matcher = imgPattern.matcher(comicPage);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}

	@Override
	public String extractTitle(int id, String comicPage) {
		Matcher matcher = titlePattern.matcher(comicPage);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}

	@Override
	public String extractAltText(int id, String comicPage) {
		Matcher matcher = altPattern.matcher(comicPage);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
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
