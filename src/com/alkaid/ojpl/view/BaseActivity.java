package com.alkaid.ojpl.view;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.alipay.AliPay;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.common.HttpUtils;
import com.alkaid.ojpl.common.LicenseManager;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.common.SystemUtil;
import com.alkaid.ojpl.view.ad.PointsManager;
import com.alkaid.ojpl.view.ui.CustAlertDialog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

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
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.msgWhat.toast:
				String str=msg.getData().getString(Constants.bundleKey.toastMag);
				Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
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
//	         调用反馈提供的接口，进入反馈界面
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
//	        case R.id.itemShare:
//	        	InputStream is = context.getResources().openRawResource(R.drawable.share_pic_s);
//	        	SNSShare.share(context, is);
//	        	return true;
	        case R.id.itemUpdate:
	        	UmengUpdateAgent.update(context);
	        	return true;
	        case R.id.itemMore:
	        	PointsManager.showOffers(context);
	        	return true;
	        case R.id.itemCost:
	        	if(LicenseManager.authLicense(context)){
	        		Toast.makeText(context, "该版本已经是去广告免积分版本", Toast.LENGTH_SHORT).show();
	        		return true;
	        	}
	        	new CustAlertDialog.Builder(context).setMessage(R.string.subjectAlert)
	        		.setPositiveButton("确定支付", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							AliPay pay = new AliPay((Activity) context);
							pay.pay();
						}
					})
	        		.setNegativeButton("再看看", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create().show();
	        	return true;
	        case R.id.itemlicense:
	        	if(LicenseManager.authLicense(context)){
	        		Toast.makeText(context, "该版本已经是去广告免积分版本", Toast.LENGTH_SHORT).show();
	        		return true;
	        	}
	        	LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        	LinearLayout llContentView=(LinearLayout) inflater.inflate(R.layout.input_license, null);
	        	final EditText etLicense=(EditText) llContentView.findViewById(R.id.etLicense);
	        	Button btnLicense=(Button) llContentView.findViewById(R.id.btnLicense);
	        	btnLicense.setText("领取礼包");
	        	btnLicense.setBackgroundResource(R.drawable.sel_dialog_button);
	        	btnLicense.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(!TextUtils.isEmpty(etLicense.getText())){
							final ProgressDialog pd= ProgressDialog.show(context, null, "联网验证中,请稍候...", true, true,null);
							new Thread(){
								public void run() {
									HttpUtils.setConnectionTimeout(3000);
									HttpUtils.setConnectionTimeout(3000);
									HttpUtils.setRetryCount(0);
									String url=Constants.license.uri;
									Map<String,String> params=new HashMap<String, String>();
									params.put(Constants.license.paramkeyLicense, etLicense.getText().toString());
									params.put(Constants.license.paramkeyDeviceid, SystemUtil.getImei(context)+"|"+SystemUtil.getLocalMacAddress(context));
									params.put(Constants.license.paramkeyDeviceinfo, SystemUtil.getMobilePhoneInfo());
									String param=HttpUtils.encodeUrl(params, true);
									url+=param;
									url=URLEncoder.encode(url);
									String result=null;
									Message msg = handler.obtainMessage(Constants.msgWhat.toast);
									try {
										result = HttpUtils.getContent(url, HttpUtils.METHOD_GET, new HashMap<String, String>(), "utf-8");
									} catch (Exception e) {
										msg.getData().putString(Constants.bundleKey.toastMag, "验证失败，网络异常Orz");
										handler.sendMessage(msg);
										pd.dismiss();
										return;
									}
									if("true".equals(result)&&LicenseManager.creatLicense(context)){
										msg.getData().putString(Constants.bundleKey.toastMag, "验证成功，您已经获得永久去广告并且无限制使用本软件的权利");
										
									}else{
										msg.getData().putString(Constants.bundleKey.toastMag, "验证失败，License无效");
									}
									handler.sendMessage(msg);
									pd.dismiss();
								};
							}.start();
						}
					}
				});
	        	new CustAlertDialog.Builder(context)
	        		.setContentView(llContentView).create().show();
	        	return true;
	        default:
	        	return false;
	            	   
	     }
	}
}
