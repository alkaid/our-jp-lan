package com.alkaid.ojpl.model;

import java.util.ArrayList;

public class BookItem extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2834439826499774284L;
	
	private String id;
	private String name;
	private String imageAdd;
	private ArrayList<LessonItem> lessonItems;
	private String downLoadAdd;
	private long fileSize;
	
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getDownloadAdd() {
		return downLoadAdd;
	}
	public void setDownLoadAdd(String downLoadAdd) {
		this.downLoadAdd = downLoadAdd;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId(){
		return id;
	}
	public String getImageAdd() {
		return imageAdd;
	}
	public void setImageAdd(String imageAdd) {
		this.imageAdd = imageAdd;
	}
	public ArrayList<LessonItem> getLessonItems() {
		return lessonItems;
	}
	public void setLessonItems(ArrayList<LessonItem> lessonItems) {
		this.lessonItems = lessonItems;
	}
	
//	@Override
//	public String toString() {
//		return this.id+this.name+this.imageAdd+this.downLoadAdd+this.lessonItems.get(0).getId()+this.lessonItems.get(1).getId();
//	}

	
}
