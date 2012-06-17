package com.alkaid.ojpl.view;

import java.io.IOException;
import java.io.InputStream;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.IOUtil;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.data.BookItemOperator;
import com.alkaid.ojpl.model.BookItem;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Lelouch
 * 说明的Activity
 *
 */
public class Introduction extends BaseActivity{
	/**小生赠言课本的id*/
	public static final String XSZENGYAN="xszengyan";
	private Typeface fontType=Typeface.DEFAULT;
	private Context context;
	private BookItem bookItem;
	private String strLessonId;
	//头部控件
	private TextView tvTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.introduction);
		this.context = this;
		bookItem=(BookItem) global.getData(Constants.bundleKey.bookItem);
		if(null==bookItem){
			String bookItemId=savedInstanceState.getString(Constants.bundleKey.bookItemId);
			bookItem=new BookItemOperator().getBookItemById(bookItemId, this);
		}
		//如果是说明的课本,获取课文内容
		if(bookItem.getId().equals(Constants.INTRODUCTION)){
				strLessonId=getIntent().getStringExtra(Constants.bundleKey.lessonId);
			if(null==strLessonId){
				strLessonId=savedInstanceState.getString(Constants.bundleKey.lessonId);
			}
		}
		//获得头部空间对象
		findView();
		initTitle();		
		//根据所选课本做不同的处理
			TextView textContent = (TextView) findViewById(R.id.textContent);
			setTextProperty(textContent);
//			textContent.setMovementMethod(ScrollingMovementMethod.getInstance());
			textContent.setText(getXszyContent(strLessonId));		
	}
	
	/** findViewById */
	private void findView(){
		this.tvTitle = (TextView) findViewById(R.id.tvTitle);
	}
	
	/** 初始化标题栏 */
	private void initTitle() {
		//标题栏
		tvTitle.setText("建议栏");
		tvTitle.setTextSize(15*global.dm.scaledDensity);
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});
	}
	
	private void goBack() {
		finish();
	}
	
	/**
	 * 	获得小生赠言的课文内容
	 * 
	 */
	private String getXszyContent(String lessonId){
		String url = Constants.INTRODUCTION+"/"+lessonId+".dat" ;
		String text = null;
		InputStream is;
		try {
			is = context.getAssets().open(url);
			text = IOUtil.readInputStrem2Str(is, null);
		} catch (IOException e) {
			LogUtil.e(e);
		}
		
		return text;
	}

	/**
	 * 默认的文本字体,格式设置
	 * @param tv
	 */
	private void setTextProperty(TextView tv){
//		tv.setGravity(Gravity.CENTER);
		tv.setTypeface(fontType);
		tv.setTextColor(context.getResources()
				.getColor(R.color.textcolor));
		tv.setTextSize(12*global.dm.scaledDensity);
	}
	
}
