package com.smeanox.apps.webcomicreader.providers;

import android.content.Context;

public class ComicProvider_cnh extends ComicProviderDefaultRegexer {

	public ComicProvider_cnh(Context context) {
		super(context, "cnh");
	}

	@Override
	protected String getImgRegex() {
		return "";
	}

	@Override
	protected String getTitleRegex() {
		return "";
	}

	@Override
	public String extractFileUrl(int id, String comicPage) {
		return "http:" + super.extractFileUrl(id, comicPage);
	}

	@Override
	public String extractTitle(int id, String comicPage) {
		return "Cyanide & Happiness " + id;
	}

}
