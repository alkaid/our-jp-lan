package com.alkaid.ojpl.view;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.common.SNSShare;
import com.alkaid.ojpl.view.ad.PointsManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;

/**
 * 所有Activity的基类 统一菜单栏
 * @author Alkaid
 *
 */
public abstract class BaseActivity extends Activity {
	protected Context context;
	protected Global global;
	/** 是否需要初始化global 是欢迎界面专用参数，因为欢迎界面的初始化放在线程里*/
	protected boolean needInitApp=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context=this;
		global=Global.getGlobal(context,needInitApp);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(context);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(context);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		//若积分墙展示未开启 则移除该项
		boolean offersEnable=PointsManager.offersEnable(context);
		LogUtil.i("积分墙是否开启:"+offersEnable);
		if(!offersEnable){
			menu.removeItem(R.id.itemMore);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.itemFeedback:
	        // 调用反馈提供的接口，进入反馈界面
	        	UMFeedbackService.openUmengFeedbackSDK(context);
	            return true;
	        case R.id.itemAbout:
	        	AlertDialog.Builder about = new AlertDialog.Builder(context);
	        	about.setMessage(Constants.ABOUT)
	        		 .setPositiveButton("确定", null)
	        		 .create().show();
//	        	new CustAlertDialog.Builder(context)
//	        		.setMessage(Constants.ABOUT)
//	        		.setPositiveButton("确定", null)
//	        		.setCanceledOnTouchOutside(true)
//	        		.create().show();
	        	return true;
	        case R.id.itemShare:
	        	InputStream is = context.getResources().openRawResource(R.drawable.share_pic_s);
	        	SNSShare.share(context, is);
	        	return true;
	        case R.id.itemMore:
	        	PointsManager.showOffers(context);
	        	return true;
	        default:
	        	return false;
	            	   
	     }
	}
}
