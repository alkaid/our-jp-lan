package com.alkaid.ojpl.view.ad;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cn.domob.android.ads.DomobAdListener;
import cn.domob.android.ads.DomobAdView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.LicenseManager;
import com.waps.AppConnect;

/**
 * 横幅广告管理类
 * @author Alkaid
 *
 */
public class BannerAdManager {
	private Context context;
	private DomobAdView mAdview320x50;
	private ViewGroup mAdContainer;
	/** 服务器配置参数：是否显示横幅广告的key 暂时由万普服务器定义*/
	private static final String KEY_SHOW_BANNER="showBanner";
	/** 服务器配置参数：是否显示横幅广告 暂时由万普服务器获得*/
	private boolean showBanner=true;
	
	
	public static final String PUBLISHER_ID="56OJzwHIuMwLtc3L0d";
	public BannerAdManager(Context context) {
		this.context=context;
		boolean isFree=LicenseManager.authLicense(context);
		if(!isFree)
			showBanner=!"false".equals(AppConnect.getInstance(context).getConfig(KEY_SHOW_BANNER));
	}
	
	public void creatAd(){
		if(!showBanner){
			return;
		}
		mAdContainer=(ViewGroup)((Activity)context).findViewById(R.id.llAd);
		//创建一个320x50的广告View
		mAdview320x50 = new DomobAdView(context, PUBLISHER_ID, DomobAdView.INLINE_SIZE_320X50);
		mAdview320x50.setKeyword("game");
		mAdview320x50.setUserGender("male");
		mAdview320x50.setUserBirthdayStr("2000-08-08");
		mAdview320x50.setUserPostcode("123456");
		
		//设置广告view的监听器。
		mAdview320x50.setOnAdListener(new DomobAdListener() {
			@Override
			public void onReceivedFreshAd(DomobAdView adview) {
				mAdContainer.setVisibility(View.VISIBLE);
			}
			@Override
			public void onFailedToReceiveFreshAd(DomobAdView adview) {
				mAdContainer.setVisibility(View.GONE);
			}
			@Override
			public void onLandingPageOpening() {
				mAdContainer.setVisibility(View.VISIBLE);
			}
			@Override
			public void onLandingPageClose() {
				mAdContainer.setVisibility(View.VISIBLE);
			}
		});
		//将广告View增加到视图中。
		mAdContainer.addView(mAdview320x50);
	}
}
