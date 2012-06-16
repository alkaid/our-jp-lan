package com.alkaid.ojpl.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class LicenseManager {
	/**
	 * 获得license 由imei或mac生成，返回md5
	 * @param context
	 * @return
	 */
	private static String getLicense(Context context){
		String unique=SystemUtil.getImei(context);
		if(TextUtils.isEmpty(unique)){
			unique=SystemUtil.getLocalMacAddress(context);
		}
		if(TextUtils.isEmpty(unique)){
			LogUtil.e("imei and macAddress is null ");
			return null;
		}
		return Md5.toMd5(unique);
	}
	/**
	 * 验证license
	 * @param context
	 * @param target
	 * @return
	 */
	private static boolean authLicense(Context context,String target){
		String license=getLicense(context);
		return license.equals(target);
	}
	
	/**
	 * 验证license
	 * @param context
	 * @return
	 */
	public static boolean authLicense(Context context){
		SharedPreferences sp=context.getSharedPreferences(Constants.sharedPreference.bookConfig.name, Context.MODE_PRIVATE);
		String target=sp.getString(Constants.sharedPreference.bookConfig.license01, null);
		if(null==target)
			return false;
		else
			return authLicense(context,target);
		
	}
	/**
	 * 创建证书
	 * @param context
	 * @return 是否成功
	 */
	public static boolean creatLicense(Context context){
		String license=getLicense(context);
		if(null==license){
			LogUtil.e("creat license failed because license string is null");
			return false;
		}
		SharedPreferences sp=context.getSharedPreferences(Constants.sharedPreference.bookConfig.name, Context.MODE_PRIVATE);
		sp.edit().putString(Constants.sharedPreference.bookConfig.license01, license).commit();
		return true;
	}
}
