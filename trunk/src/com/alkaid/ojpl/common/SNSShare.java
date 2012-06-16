package com.alkaid.ojpl.common;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.alkaid.ojpl.view.ad.PointsManager;
import com.umeng.api.exp.UMSNSException;
import com.umeng.api.sns.UMSnsService;
import com.umeng.api.sns.UMSnsService.DataSendCallbackListener;
import com.umeng.api.sns.UMSnsService.RETURN_STATUS;

public class SNSShare {
//	/**分享里包含的内容*/
//	public static final String APPCONTENT="测试用。。。";
//	/**分享的下载地址*/
//	public static final String APPADD="http://xxxx.com";
	/**分享的网络出现问题*/
	public static final String SHARENETERROR="网络不给力啊亲，稍后再试";
/**
 * 	分享的内容包含图文，并且使用友盟的模板
 * @param context 
 * @param is 要分享的图片的流
 */
	public static void share(final Context context, InputStream is){
//    	HashMap<String,String> map = new HashMap<String, String>();
//    	map.put("AppContent",APPCONTENT);
//    	map.put("AppAdd", APPADD);
    	DataSendCallbackListener listener = new DataSendCallbackListener(){
    		@Override
    		public void onDataSendFailedWithException(
    		UMSNSException exception,
    		UMSnsService.SHARE_TO userPlatform) {
    			Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show();
    		}
    		@Override
    		public void onDataSendFinished(RETURN_STATUS
    		returnStatus, UMSnsService.SHARE_TO userPlatform) {
    		switch (returnStatus)
    		{
    		case UPDATED:
    			Toast.makeText(context, "发布成功", Toast.LENGTH_LONG).show();
    			//积分奖励机制
    			String date = getSharedDate(context, userPlatform);
				String now=new SimpleDateFormat("yy-MM-dd").format(new Date());
				if(!now.equals(date)){
					PointsManager pointsManager=new PointsManager(context);
					pointsManager.awardPoints(Constants.points.awardPerShare);
					writeSharedDate(context, userPlatform);
				}
        		break;
    		case REPEATED:
        		Toast.makeText(context, "不能发布重复内容", Toast.LENGTH_SHORT).show();
        		break;
    		case SEND_TIME_EXTENDS_LIMIT:
    			Toast.makeText(context, SHARENETERROR, Toast.LENGTH_SHORT);
    			break;
    		case NETWORK_UNAVAILABLE:
    			Toast.makeText(context, SHARENETERROR, Toast.LENGTH_SHORT);
    			break;
    		default:
    			break;
    			}
    		}
    	};
    	//获得图片的字节数
		try {
			byte[] picture = IOUtil.readInputStream2Byte(is);
			UMSnsService.share(context, picture, new HashMap<String, String>(), listener);
		} catch (IOException e) {
			LogUtil.e(e);
		}
    	
	}
	
	private static void writeSharedDate(Context context,UMSnsService.SHARE_TO userPlatform){
		SharedPreferences sp=context.getSharedPreferences(Constants.sharedPreference.snsShare.name, Context.MODE_PRIVATE);
		String key=null;
		switch (userPlatform) {
		case SINA:
			key=Constants.sharedPreference.snsShare.sinaDate;
			break;
		case RENR:
			key=Constants.sharedPreference.snsShare.renrenDate;
			break;
		case TENC:
			key=Constants.sharedPreference.snsShare.qqweiboDate;
			break;
		default:
			break;
		}
		String date=new SimpleDateFormat("yy-MM-dd").format(new Date());
		sp.edit().putString(key, date).commit(); 
	}
	
	private static String getSharedDate(Context context,UMSnsService.SHARE_TO userPlatform){
		SharedPreferences sp=context.getSharedPreferences(Constants.sharedPreference.snsShare.name, Context.MODE_PRIVATE);
		String key=null;
		switch (userPlatform) {
		case SINA:
			key=Constants.sharedPreference.snsShare.sinaDate;
			break;
		case RENR:
			key=Constants.sharedPreference.snsShare.renrenDate;
			break;
		case TENC:
			key=Constants.sharedPreference.snsShare.qqweiboDate;
			break;
		default:
			break;
		}
		return sp.getString(key, null);
	}
}
