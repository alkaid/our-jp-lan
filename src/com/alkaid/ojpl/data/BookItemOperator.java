package com.alkaid.ojpl.data;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.res.AssetManager;

import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.model.LessonItem;


/**
 * @author Lelouch
 *
 */
public class BookItemOperator{
	public static String BOOKXML = "config/bookitems.xml";
	private List<BookItem> bookItems;
	private BookItem bookItem;
	
	//获得所有课本的实例
	public List<BookItem> getAllBookItems(Activity a){
		
		SaxParseService sax = new SaxParseService();
		InputStream is = null;
		try {
			 AssetManager am = a.getAssets();
			 is = am.open(BOOKXML);
			bookItems = sax.getBookItems(is);
		} catch (IOException e) {
			
			LogUtil.e(e);
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				LogUtil.e(e);
			}
		}
//for(BookItem bookItem : bookItems){
//	Log.e("BookItems", bookItem.toString());
//}
		return bookItems;
	}
	
	//根据id获得某本书的实例
	public BookItem getBookItemById(String id,Activity a){
		int index = Integer.parseInt(id.substring(id.length()-1));
		bookItem = getAllBookItems(a).get(index);
		return bookItem;
	}
		
	
}

//解析XML数据
class SaxParseService extends DefaultHandler{
	
	private List<BookItem> bookItems;
	private BookItem bookItem;
	private LessonItem lessonItem;
	private String perTag = null;//记录正在解析的节点
		
	public List<BookItem> getBookItems(InputStream xmlStream){
		SaxParseService handler = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parse = factory.newSAXParser();
			handler = new SaxParseService();
			parse.parse(xmlStream, handler);
		} catch (FactoryConfigurationError e) {
			LogUtil.e(e);
		} catch (ParserConfigurationException e) {
			LogUtil.e(e);
		} catch (SAXException e) {
			LogUtil.e(e);
		} catch (IOException e) {
			LogUtil.e(e);
		}
		return handler.bookItems;
	}
	
	@Override
	public void startDocument() throws SAXException {
		bookItems = new ArrayList<BookItem>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if("BookItem".equals(localName)){
			bookItem = new BookItem();
			bookItem.setId(attributes.getValue(0));
		}else if("Lesson".equals(localName)){
			lessonItem = new LessonItem();
			lessonItem.setId(attributes.getValue(0));		
		}
		perTag = localName;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("BookItem".equals(localName)){
			bookItems.add(bookItem);
			bookItem = null;
		}else if("Lesson".equals(localName)){
			bookItem.getLessonItems().add(lessonItem);
			lessonItem = null;
		}
		perTag = null;
	};
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(perTag!=null){
			String content = new String(ch,start,length);
			if("Name".equals(perTag)){
				bookItem.setName(content);
			}else if("ImageAdd".equals(perTag)){
				bookItem.setImageAdd(content);
			}else if("DownLoadAdd".equals(perTag)){
				bookItem.setDownLoadAdd(content);
			}else if("Lessons".equals(perTag)){
				ArrayList<LessonItem> lessonItems = new ArrayList<LessonItem>();
				bookItem.setLessonItems(lessonItems);
			}else if("title".equals(perTag)){
				lessonItem.setTitle(content);
			}else if("AudioAdd".equals(perTag)){
				lessonItem.setAudioAdd(content);
			}
		}
	}
	
	
}



