/**
 * 
 */
package com.alkaid.ojpl.common;

import android.os.Environment;

/**
 * @author Alkaid
 *  存放公用常量
 */
public class Constants {
	/** SD卡路径 */
	public static final String PATH_SD=Environment.getExternalStorageDirectory().getAbsolutePath();
	/** 组织路径 */
	public static final String PATH_COM=PATH_SD+"/AlkaidApp";
	/** 应用路径 */
	public static final String PATH_APP=PATH_COM+"/OurJpLang";
	/** 资源路径 */
	public static final String PATH_RES=PATH_APP+"/res";
	//测试用
//	public static final String PATH_RES=PATH_SD+"/HJApp/HJDjdry1/res";
	
	/** 解码key */
	public static final String AESKEY="@www.hujiang.com";
	/**SD不存在或者加载出错提示*/
	public static final String SDERROR="您的SD卡不存在或者存在错误加载,请查看!!";
	/**服务器连接出错的提示*/
	public static final String CONNECTERROR="获取网络内容失败,请稍后再试!!";
	/**网络不存在的提示*/
	public static final String NETERROR="亲,你的网络不大好,请稍后再试!!";
	/**下载出错的提示*/
	public static final String DOWNERROR="ORZ,下载出错,正在尝试重新下载!!";
	/**下载出错的提示*/
	public static final String DOWNSIZEWRONG="ORZ,下载出错,请尝试重新下载";
	/**退出应用提示*/
	public static final String EXITREMIND="再按一次返回键退出程序!!";
	/**五十音图书的ID*/
	public static final String FIFTYMAPID="book0";
	/**说明的ID*/
	public static final String INTRODUCTION="introduction";
	/**关于的内容*/
	public static final String ABOUT = "版权声明：\n"+
		    "1.本应用是免费应用，应用仅供个人学习使用；\n"+
		    "2.部分内容来自互联网，如侵害您的权益，请联系我们；\n"+
		    "\n"+
		    "版本号：1.0.0\n"+
		    "coodroid 版权所有\n"+
		    "交流QQ群211029208\n"+
		    "反馈邮箱：ancoodroid@gmail.com\n";

	/** bundle传递数据时的key */
	public static class bundleKey{
		/** 书本标识**/
//		public static final String bookId="bookId";
		/** 书本信息 **/
		public static final String bookItem="bookItem";
		/** 课文标识 */
		public static final String lessonId="lessonId";
		/** 课文标题 */
		public static final String lessonTitle="lessonTitle";
		/** 异常信息 */
		public static final String errorMsg="errorMsg";
		/** 需要弹出的信息*/
		public static final String toastMag="toastMsg";
		/** 书本id **/
		public static final String bookItemId="bookItemId";
		/** 视频播放地址 */
		public static final String videoUri="videoUri";
		/** 是否指在线视频*/
		public static final String videoIsOnline="videoIsOnline";
	}
	/** message.what */
	public static class msgWhat{
		/** 异常消息 */
		public static final int error=-200;
		/** 下载状态改变*/
		public static final int downstate_changed=2000;
		/** toast*/
		public static final int toast=3000;
	}
	/** 积分相关常量*/
	public static class points{
		/** 初始积分数量 */
		public static final int total=200;
		/** 每次要消耗的积分数量 */
		public static final int spendPerAction=20;
		/** 每次分享奖励多少积分*/
		public static final int awardPerShare=10;
	}
	/** sharedPreference 相关key */
	public static class sharedPreference{
		/** 书本配置信息 */
		public static class bookConfig{
			/** sharedPreference名称 */
			public static final String name="bookConfig";
			/** 书本大小 key的后缀*/
			public static final String size_suffix="_size";
			/** 是否需要积分 key的后缀*/
			public static final String needPoints_suffix="_needPoints";
			/** key 免费证书*/
			public static final String license01="license01";
		}
		/** 课文配置信息 */
		public static class lessonConfig{
			/** sharedPreference名称 */
			public static final String name="lessonConfig";
			/** 视频大小 key的后缀*/
			public static final String huihua_video_size_suffix="_huihua_video_size";
		}
		/** sns分享 是否分享过，值为分享日期*/
		public static class snsShare{
			/** sharedPreference名称 */
			public static final String name="snsShare";
			public static final String sinaDate="sinaDate";
			public static final String qqweiboDate="qqweiboDate";
			public static final String renrenDate="renrenDate";
		}
		
		public static class aliPay{
			public static final String successCostAlert="支付成功，您将获得去广告，免费获取所有资源等功能。";
			public static final String failCostAlert="支付失败，请重新尝试，如果有不明之处，请联系我们。";
		}
	}
	
	public static class license{
		public static final String uri="http://coodroid.com/android/index.php?route=ourjplan/license";
		public static final String paramkeyLicense="license";
		public static final String paramkeyDeviceid="deviceid";
		public static final String paramkeyDeviceinfo="deviceinfo";
	}
}
