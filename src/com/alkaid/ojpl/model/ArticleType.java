/**
 * 
 */
package com.alkaid.ojpl.model;


/**
 * @author Alkaid
 * 文章种类
 */
public enum ArticleType {
	shuangyu("danci#shuangyu","单词","danci"),
	huihua("huihua","会话","huihua"),
	juxing("juxing","文型","juxing"),
	liwen("liwen","例文","liwen"),
	yufa("yufa","语法",null);
	
	/** 类型的英文表示，唯一标识*/
	private String en;
	/** 类型的中文表示*/
	private String zh;
	/** 该类型的mp3名称 */
	private String mp3Name;
	ArticleType(String en,String zh,String mp3Name){
		this.en=en;
		this.zh=zh;
		this.mp3Name=mp3Name;
	}
	
	public static ArticleType getType(String en) {
		for (ArticleType a : ArticleType.values()) {
			if (en.equals(a.en))
				return a;
		}
		return null;
	} 
	/**
	 * 该种类的数据来源是否属于UBB文本<br/>
	 * 目前情况下：会话、文型、例文、语法属于UBB文本
	 * @param type
	 * @return
	 */
	public static boolean isUBBText(ArticleType type){
		return type==huihua || type==juxing || type==liwen || type==yufa;
	}
	/**
	 * 该种类的数据来源是否属于UBB文本<br/>
	 * 目前情况下：会话、文型、例文、语法属于UBB文本
	 * @return
	 */
	public boolean isUBBText(){
		return this==huihua || this==juxing || this==liwen || this==yufa;
	}

	public String getEn() {
		return en;
	}
	public String getZh() {
		return zh;
	}
	public String getMp3Name() {
		return mp3Name;
	}
}
