package com.smeanox.apps.webcomicreader;

import android.os.Environment;

import java.io.File;

public class Comic {
	private ComicProvider comicProvider;
	private int id, prevId, nextId;
	private String comicUrl, fileUrl;
	private String title, altText;
	private File localFile;

	public Comic(ComicProvider comicProvider, int id, int prevId, int nextId, String comicUrl, String fileUrl, String title, String altText, String localFile) {
		this.comicProvider = comicProvider;
		this.id = id;
		this.prevId = prevId;
		this.nextId = nextId;
		this.comicUrl = comicUrl;
		this.fileUrl = fileUrl;
		this.title = title;
		this.altText = altText;
		this.localFile = new File(comicProvider.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), localFile);
	}

	public ComicProvider getComicProvider() {
		return comicProvider;
	}

	public int getId() {
		return id;
	}

	public int getPrevId() {
		return prevId;
	}

	public int getNextId() {
		return nextId;
	}

	public String getComicUrl() {
		return comicUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getAltText() {
		return altText;
	}

	public File getLocalFile() {
		return localFile;
	}

	public void setPrevId(int prevId) {
		this.prevId = prevId;
	}

	public void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public boolean hasPrevious(){
		return getPrevious() != null;
	}

	public Comic getPrevious() {
		return comicProvider.getComicById(prevId);
	}

	public boolean hasNext(){
		return getNext() != null;
	}

	public Comic getNext() {
		return comicProvider.getComicById(nextId);
	}

	public static final String COLUMN_NAME_ID = "_ID";
	public static final String COLUMN_NAME_TITLE = "title";
	public static final String COLUMN_NAME_ALT_TEXT = "alttext";
	public static final String COLUMN_NAME_COMIC_URL = "comicurl";
	public static final String COLUMN_NAME_FILE_URL = "fileurl";
	public static final String COLUMN_NAME_LOCAL_FILE = "localfile";
	public static final String COLUMN_NAME_PREV = "prev";
	public static final String COLUMN_NAME_NEXT = "next";
}
