package com.alkaid.ojpl.view.ad;

import android.content.Context;

import com.alkaid.ojpl.common.LicenseManager;
/**
 * 积分管理类
 * @author Alkaid
 *
 */
public class PointsManager {
	/** 由于获取积分是异步的，该常量标识获取积分的状态:成功*/
	public static final int GETPOINTS_SUCCESS=1;
	/** 由于获取积分是异步的，该常量标识获取积分的状态:服务器还未返还数据*/
	public static final int GETPOINTS_LOADING=2;
	/** 由于获取积分是异步的，该常量标识获取积分的状态:发生错误*/
	public static final int GETPOINTS_ERROR=3;
	
	private Context context;
	/** 是否开启积分墙 若手机没联网 则默认不开启 联网状态下，它的值等同于{@link #pointsEnable} */
	private boolean offersEnable=false;
	/** 是否开启积分系统 由服务器获得 由于是下载需要积分，则可以不考虑没联网的状态*/
	private boolean pointsEnable=false;
	private static final String KEY_POINTS_ENABLE="pointsEnable";
	/** 是否拥有免费证书*/
	private boolean authLicense=false;
	public boolean isAuthLicense() {
		return authLicense;
	}

	public PointsManager(Context context){
		this.context=context;
		authLicense=LicenseManager.authLicense(context);
		if(authLicense) return;
		init(context);
	}
	
	//初始化万普
	public static void init(Context context){
	}
	
	public static boolean offersEnable(Context context){
		return false;
	}
	/**
	 * 判断积分是否充足并弹窗
	 * @param context
	 * @return 积分是否充足
	 */
	public boolean isPointsEnough(){
		return true;
	}
	
	
	/** 获得货币名称*/
	public String getCurrencyName() {
		return null;
	}
	/** 获得积分 */
	public int getTotalPoints() {
		return 0;
	}
	/** 获得错误信息 */
	public String getError() {
		return null;
	}
	/** 判断获取积分的状态 结果为{@link #GETPOINTS_SUCCESS},{@link #GETPOINTS_LOADING},{@link #GETPOINTS_ERROR}*/
	public int getGetpointsStatus(){
		return GETPOINTS_ERROR;
	}
	/** 消费积分 */
	public void spendPoints(int amount){
	}
	/** 奖励积分 */
	public void awardPoints(int amount){
	}
	/** 展示积分墙 */
	public void showOffers(){
	}
	/** 回收资源*/
	public void finalize(){
	}
	/** 展示积分墙 */
	public static void showOffers(Context context){
	}
}
