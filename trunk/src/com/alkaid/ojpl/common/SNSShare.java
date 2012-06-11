package com.alkaid.ojpl.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
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
    		}
    		@Override
    		public void onDataSendFinished(RETURN_STATUS
    		returnStatus, UMSnsService.SHARE_TO userPlatform) {
    		switch (returnStatus)
    		{
    		case UPDATED:
        		Log.i("Log", "Success!");
        		break;
    		case REPEATED:
        		Log.i("Log", "Repeated!");
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
}
