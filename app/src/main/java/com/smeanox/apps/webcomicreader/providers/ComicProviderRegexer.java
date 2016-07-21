package com.smeanox.apps.webcomicreader.providers;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ComicProviderRegexer extends ComicProvider {

	protected Pattern nextPattern, imgPattern, titlePattern, altPattern;

	public ComicProviderRegexer(Context context) {
		super(context);

		initPatterns();
	}

	// call init & initPatterns afterwards!!!
	protected ComicProviderRegexer(Context context, String tableName, ComicProviders providers) {
		super(context, tableName, providers);
	}

	protected void initPatterns() {

		nextPattern = Pattern.compile(getNextRegex());
		imgPattern = Pattern.compile(getImgRegex());
		titlePattern = Pattern.compile(getTitleRegex());
		altPattern = Pattern.compile(getAltRegex());
	}

	protected abstract String getNextRegex();
	protected abstract String getImgRegex();
	protected abstract String getTitleRegex();
	protected abstract String getAltRegex();
	protected abstract String getFullUrl(String nextUrl);
	protected abstract boolean isLastUrl(String nextUrl);

	@Override
	public String extractNextUrl(int id, String comicPage) {
		Matcher matcher = nextPattern.matcher(comicPage);
		if(matcher.find()){
			if(isLastUrl(matcher.group(1))){
				return null;
			}
			System.err.println("next " + getFullUrl(matcher.group(1)));
			return getFullUrl(matcher.group(1));
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
}
