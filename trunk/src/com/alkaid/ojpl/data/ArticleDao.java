/**
 * 
 */
package com.alkaid.ojpl.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.alkaid.ojpl.common.SpannableStringUtil;
import com.alkaid.ojpl.model.Article;

/**
 * @author Alkaid
 *
 */
public class ArticleDao extends Dao<Article> {
	public ArticleDao(Context ctx) {
		super(ctx);
	}

	/*
	 * 得到分段列表  
	 * @return
	 * @throws AlkaidException 
	 */
	/*public Map<String, String> getSectionsEnum() throws AlkaidException{
		Map<String,String> map=new HashMap<String, String>();
		InputStream is=null;
		String str=null;
		try {
			is = ctx.getAssets().open("config/sections.dat");//TODO 暂时放在assets 待改变
			str=IOUtil.readInputStrem2Str(is, "UTF-8");
		} catch (IOException e) {
			LogUtil.e(e);
			throw new AlkaidException(e);
		}
		String[] strlist=str.split("\n");
		for (String dat : strlist) {
			String[] keyvalue=dat.split("=");
			map.put(keyvalue[0], keyvalue[1]);
		}
		return map;
	}*/
	
	/*@Override
	public List<Article> getAll(int lessonId) throws AlkaidException {
		List<Article> articles=new ArrayList<Article>();
		Lesson lessonTemplate=((Global)ctx.getApplicationContext()).getLessonTemplate();
		return articles;
	}*/
	/**
	 * 对文章的文本内容（article.getText())进行修饰
	 * @param article
	 * @return
	 */
	public static void decorateArticle(Article article){
		String text=article.getText();
		switch (article.getType()) {
		case shuangyu:
			text = decorateShuangyu(text);
			break;
		case huihua:
			text = decorateHuihua(text);
			break;
		default:
			break;
		}
		article.setText(text);
	}
	
	/**
	 * 修饰双语文本 包括以下内容：<br/>
	 * 1.为双语添加列表前缀 形如 1.xxxxx  ooo  2. xxxx  ooo</br>
	 * 2.为双语换行符处再多添加一个换行符
	 * @param text
	 */
	private static String decorateShuangyu(String text){
		StringBuilder sb=new StringBuilder();
		String reglex=".+\n";
		int i=1;
		int index=1;
		Pattern p=Pattern.compile(reglex);
		Matcher m=p.matcher(text);
		while(m.find()){
			if(i%2!=0){
				sb.append(index++).append(".");
			}
			sb.append(m.group()).append("\n");
			i+=1;
		}
		return sb.toString();
	}
	/**
	 * 修饰会话文本 包括以下内容：<br/>
	 * 1.标题加粗<br/>
	 * 2.换行符处再多添加一个换行符<br/>
	 * @param text
	 * @return
	 */
	private static String decorateHuihua(String text){
		StringBuilder sb=new StringBuilder();
		//行尾加换行符
		String reglex=".+[\\:：].+\n";
		int i=-1;
		Pattern p=Pattern.compile(reglex);
		Matcher m=p.matcher(text);
		while(m.find()){
			i=i==-1?m.start():i;
			sb.append(m.group()).append("\n");
		}
		//标题加粗
		if(i>0){
			StringBuilder title=new StringBuilder().append(SpannableStringUtil.B_START_TAG).append(text.substring(0,i)).append(SpannableStringUtil.B_END_TAG).append("\n");
			sb.insert(0, title.toString());
		}
		return sb.toString();
	}
}
