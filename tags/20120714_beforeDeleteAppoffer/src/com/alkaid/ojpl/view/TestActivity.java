/**
 * 
 */
package com.alkaid.ojpl.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.R.layout;
import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.data.FiftyMapDao;
import com.waps.AppConnect;

/**
 * @author Alkaid
 * 课文列表
 *
 */
public class TestActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
//				WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setContentView(R.layout.main);
//		final ViewGroup layMain=(ViewGroup)findViewById(R.id.layMain);
//		WorkSpace wp=new WorkSpace(this, null);
//		Global.initApp(this);
//		Global global=Global.getGlobal(this);
		AppConnect.getInstance(this).showOffers(this);
		
//		Dao<Lesson> lessonDao=new LessonDao(this);
//		try {
//			Lesson lesson=lessonDao.getById(1);
//			int i=0;
//			for(Article a : lesson.getArticles()){
//				i+=1;
////				Article a=lesson.getArticles().get(0);
////				LogUtil.e(a.getTypeZh()+i, a.getText());
//				ScrollView mScrollView = new ScrollView(this);
//				TextView txt=new TextView(this);
//				LinearLayout lay=new LinearLayout(this);
//				lay.setBackgroundDrawable(new ColorDrawable(0xffffffff));
//			    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
//			    int width=getWindowManager().getDefaultDisplay().getWidth();
//			    mLayoutParams.setMargins(width/20, 20, width/20, 0);
////			    setTextProperty(txt);
//			    Typeface localTypeface = Typeface.createFromAsset(this.getAssets(), "font/micross.ttf");
//			    txt.setTypeface(localTypeface);
//			    txt.setTextColor(getResources().getColor(R.color.textcolor));
////			    txt.setBackgroundDrawable(new ColorDrawable(0xffffffff));
//			    lay.addView(txt, mLayoutParams);
//			    mScrollView.addView(lay);
//			    String txtTest="[h1]ooxx[/h1]\n我是[color=red]传说[/color]中的[b]ooxx[/b]\n求单挑[url=http://www.baidu.com]单挑[/url]\n求[color=#ff00ff00]搞基[/color]";
////			    SpannableStringBuilder sp= SpannableStringUtil.string2SpanStr(a.getText());
////			    txt.setText(sp);
////			    String testStr="私（わたし）\n我\nわたしたち\n我们\nあなた\n你，妳\n";
////			    setColorForShuangyu(txt, a.getText());
////			    wp.addView(mScrollView);
//				
//				
//			    layMain.addView(mScrollView,new LayoutParams(getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight()));
//			}
//		} catch (AlkaidException e) {
//			// TODO Auto-generated catch block
//			LogUtil.e(e);
//		}
	}
	
}
