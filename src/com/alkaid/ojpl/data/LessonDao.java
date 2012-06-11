/**
 * 
 */
package com.alkaid.ojpl.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Xml;

import com.alkaid.ojpl.common.AES;
import com.alkaid.ojpl.common.AlkaidException;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.common.IOUtil;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.model.Article;
import com.alkaid.ojpl.model.ArticleType;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.model.Lesson;

/**
 * @author Alkaid
 *
 */
public class LessonDao {
	Context ctx;
	public LessonDao(Context ctx) {
		this.ctx=ctx;
	}

	/**
	 * 获得一个Lesson模板 主要是加载 {@link com.alkaid.ojpl.model.Article}的类别信息
	 * @return
	 */
	public Lesson getLessonTemplate(){
		Lesson lesson=null;
		Article article=null;
		InputStream is=null;
		try {
			is=ctx.getAssets().open("config/article_type.xml");
		} catch (IOException e) {
			LogUtil.e(e);
		}
		XmlPullParser xpp = Xml.newPullParser();
		try {
			xpp.setInput(is,"UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().equalsIgnoreCase(Article.XmlTag.rootTag)) {
						lesson=Lesson.getEmptyLesson();
					} else if (lesson != null) {
						if (xpp.getName().equalsIgnoreCase(Article.XmlTag.article)) {
							article=Article.getEmptyInstance(lesson);
						} else if (article!=null) {
							if (xpp.getName().equalsIgnoreCase(Article.XmlTag.typeEn)) {
								article.setType(ArticleType.getType(xpp.nextText()));
								lesson.getArticles().add(article);
//								article.setTypeEn(xpp.nextText());
//							}else if(xpp.getName().equalsIgnoreCase(Article.XmlTag.typeZh)){
//								article.setTypeZh(xpp.nextText());
//							}else if(xpp.getName().equalsIgnoreCase(Article.XmlTag.mp3Name)){
//								article.setMp3Name(xpp.nextText());
//								lesson.getArticles().add(article);
							}
						}
					}
				}
				eventType = xpp.next();
			}
		} catch (UnsupportedEncodingException e) {
			LogUtil.e(e);
		} catch (XmlPullParserException e) {
			LogUtil.e(e);
		} catch (IOException e) {
			LogUtil.e(e);
		}
		return lesson;
	}
	
	public Lesson getById(int id,BookItem bookItem) throws AlkaidException {
		Lesson lesson=new Lesson(id,bookItem);
		Lesson lessonTemplate=((Global)ctx.getApplicationContext()).getLessonTemplate();
		//TODO setTitleJp 留空
		List<StringBuilder> textsList= this.parseLessonDatFile(lesson.getDatFilePath());
//		if(textsList.size()!=lessonTemplate.getArticles().size()){
//			LogUtil.e("配置文件与数据文件不匹配");
//		}
		//初始化articles数量
		for(int i=0;i<lessonTemplate.getArticles().size();i++){
			lesson.getArticles().add(null);
		}
		//解析
		Article article=null;
		for(StringBuilder text:textsList){
			int firstIndexOfLine=text.indexOf("\n");
			//去掉 *号
			String typeEnFromDat=text.substring(text.indexOf("*")+1,firstIndexOfLine).trim();
			for(Article articleTemplate :lessonTemplate.getArticles()) {
//				if(articleTemplate.getTypeEn().equals(typeEnFromDat)){
				if(articleTemplate.getType().getEn().equals(typeEnFromDat)){
					text.delete(0, firstIndexOfLine+1)
						.delete(text.lastIndexOf("\n"),text.length());
//					article=new Article(lesson,articleTemplate.getTypeEn(),articleTemplate.getTypeZh(),articleTemplate.getMp3Name());
					article=new Article(lesson, articleTemplate.getType());
					article.setText(text.toString());
					//根据模板的类别顺序插入文章实体
					lesson.getArticles().set(lessonTemplate.getArticles().indexOf(articleTemplate),article);
					break;
				}
			}
		}
		return lesson;
	}
	/**
	 * 解析数据文件.dat 返回texts数组
	 * @param filePath
	 * @return
	 * @throws AlkaidException
	 */
	private List<StringBuilder> parseLessonDatFile(String filePath) throws AlkaidException{
		if(!IOUtil.existsFile(filePath)){
			throw new AlkaidException("文件不存在！");
		}
		//解析dat文件
		List<StringBuilder> texts=new ArrayList<StringBuilder>();
		String contents = AES.decode(filePath, Constants.AESKEY);
		String regex = "\\*[^\\*]*\\n\\*";  
	    Pattern p = Pattern.compile(regex);  
	    Matcher m = p.matcher(contents);  
	    while (m.find()){  
	    	texts.add( new StringBuilder(m.group()) ); 
	    } 
	    return texts;
	}
	
}
