package com.smeanox.apps.webcomicreader.providers;

import android.content.Context;

public class ComicProvider_lovenstein extends ComicProviderDefaultRegexer {

	public ComicProvider_lovenstein(Context context) {
		super(context, "lovenstein");
	}

	@Override
	public String extractFileUrl(int id, String comicPage) {
		String file = super.extractFileUrl(id, comicPage);
		if(file != null){
			file = getFullUrl(file);
		}
		return file;
	}

	@Override
	protected String getTitleRegex() {
		return "";
	}

	@Override
	protected String getAltRegex() {
		return "";
	}

	@Override
	public String extractTitle(int id, String comicPage) {
		return "Mr. Lovenstein " + id;
	}

	@Override
	public String extractAltText(int id, String comicPage) {
		return "";
	}
}
