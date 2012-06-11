/**
 * 
 */
package com.alkaid.ojpl.model;

import android.text.TextUtils;

/**
 * @author Alkaid
 * 具体课文类
 */
public class Article extends Model {

	private static final long serialVersionUID = 6734717527025431514L;
	
	/**
	 * xml 标签名称
	 * @author Alkaid
	 *
	 */
	public static class XmlTag{
		public static final String rootTag="ArticleType";
		public static final String article="Article";
		public static final String typeEn="TypeEn";
		public static final String typeZh="TypeZh";
		public static final String mp3Name="Mp3Name";
	}
	
	/** type前缀 与type组合用于解析dat文件*/
	private static final String TYPE_PREFIX="*";
	
	private Article(Lesson lesson){this.lesson=lesson;}
	public static Article getEmptyInstance(Lesson lesson){
		return new Article(lesson);
	}
//	public Article(Lesson lesson,String typeEn,String typeZh,String mp3Name){
//		this.lesson=lesson;
//		this.typeEn=typeEn;
//		this.typeZh=typeZh;
//		this.mp3Name=mp3Name;
//		this.mp3Url=lesson.getPath()+"/"+(lesson.getPositionInGroup()+1)+"_"+this.mp3Name+".mp3";
//	}
	public Article(Lesson lesson,ArticleType type){
		this.lesson=lesson;
		this.type=type;
		if(TextUtils.isEmpty(type.getMp3Name()))
			this.mp3Url=null;
		else
			this.mp3Url=lesson.getPath()+"/"+(lesson.getPositionInGroup()+1)+"_"+this.type.getMp3Name()+".mp3";
	}
	/** 课文->文章  一对多 */
	private Lesson lesson;
//	/** 类型英文 具体有danci、yufa等*/
//	private String typeEn;
//	/** 类型中文 具体有单词、例文等 */
//	private String typeZh;
//	/** mp3文件名 */
//	private String mp3Name;
	/** 文章类型 */
	private ArticleType type;
	/** mp3Url */
	private String mp3Url;
	/** 文章具体内容 */
	private String text;
	
	public String getTypeEnAddPrefix(){
//		return TYPE_PREFIX+typeEn;
		return TYPE_PREFIX+type.getEn(); 
	}
	
	public ArticleType getType() {
		return type;
	}
	public void setType(ArticleType type) {
		this.type = type;
	}
	public String getMp3Url() {
		return mp3Url;
	}
	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Lesson getLesson() {
		return lesson;
	}
	
}
