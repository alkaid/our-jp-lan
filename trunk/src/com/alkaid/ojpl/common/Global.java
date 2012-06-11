package com.alkaid.ojpl.common;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;

import com.alkaid.ojpl.data.LessonDao;
import com.alkaid.ojpl.model.Lesson;
import com.alkaid.ojpl.model.Model;

/**
 * @author Alkaid
 *
 */

// ConstantData.PARAMETERS_NUM[i]
// i=2 contains("#shuangyu"))
// i=3 contains("#lrc"))
// i=4 equals("*exercise")
// i=1  其他
public class Global extends Application{
	/** 课文模板 主要设置articles类别信息 */
	public Lesson lessonTemplate;
	
	public int width;
	public int height;
//	private float widthRate;
//	private float heightRate;
	public DisplayMetrics dm;
	/** 全局数据 类似session*/
	private Map<String, Model> session=new HashMap<String, Model>();
	
	/** 课文索引 */
//	public int lessonId=1;
	public static Global getGlobal(Context context){
		return (Global)context.getApplicationContext();
	}

	public Lesson getLessonTemplate() {
		return lessonTemplate;
	}
	/** 初始化应用*/
	public static void initApp(Activity context){
		Global global=(Global)context.getApplicationContext();
		Display display = context.getWindowManager().getDefaultDisplay();
		DisplayMetrics dm=new DisplayMetrics();
		display.getMetrics(dm);
		global.setDm(dm);
		global.setWidth(display.getWidth());
		global.setHeight(display.getHeight());
//		global.setWidthRate(global.getWidth()/320.0f);
//		global.setHeightRate(global.getHeight()/480.0f);
		global.setLessonTemplate(new LessonDao(context).getLessonTemplate());
		//TODO 丢到具体Activity去
		AnimationLoader.load(context);
	}
	
	public void putData(String key,Model data){
		session.put(key, data);
	}
	
	public Model getData(String key){
		Model m=session.get(key);
		session.remove(key);
		return m;
	}

	private void setDm(DisplayMetrics dm) {
		this.dm = dm;
	}

	private void setWidth(int width) {
		this.width = width;
	}

	private void setHeight(int height) {
		this.height = height;
	}

	private void setLessonTemplate(Lesson lessonTemplate) {
		this.lessonTemplate = lessonTemplate;
	}
	
}
