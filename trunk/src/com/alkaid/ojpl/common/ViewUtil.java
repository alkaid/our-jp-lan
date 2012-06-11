package com.alkaid.ojpl.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ViewUtil {
	public static StateListDrawable getBtnStateListDrawable(Context context, int drawId_enabled, int drawId_pressed)
	  {
	    StateListDrawable stateListDrawable = new StateListDrawable();
	    Drawable imgPressed = context.getResources().getDrawable(drawId_pressed);
	    Drawable imgEnabled = context.getResources().getDrawable(drawId_enabled);
	    stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, imgPressed);
	    stateListDrawable.addState(new int[]{android.R.attr.state_enabled}, imgEnabled);
	    return stateListDrawable;
	  }
	
	public static int dp2px(int dp,Context context){
	    return (int)(dp * context.getResources().getDisplayMetrics().density);
	}
	
	/**
	 * 适配默认字体尺寸
	 * @return
	 */
	public static int getAdjustFontSize(Context context) {
		int width = ((Global) context.getApplicationContext())
				.width;
		if (width <= 320)
			return 16;
		if (width <= 480)
			return 18;
		if (width <= 540)
			return 21;
		if (width <= 800)
			return 23;
		return 26;
	}
	
	/**
	 * 适配默认行间距
	 * @return
	 */
	public static int getAdjustLineSpacing(Context context) {
		int width = ((Global) context.getApplicationContext())
				.width;
		if (width <= 240)
			return 0;
		if (width <= 320)
			return 5;
		if (width <= 480)
			return 7;
		if (width <= 540)
			return 9;
		if (width <= 800)
			return 11;
		return 13;
	}
	
	/**
	 * 格式化时间为   分：秒   格式
	 * @param timeMs 单位 milliseconds
	 * @return
	 */
	public static String formatTimeInmmss(int timeMs) {
		StringBuilder timeDisplay = new StringBuilder();
		int timeMin = timeMs / 60000;
		int timeRestSec = timeMs / 1000 % 60;
		if (timeMin <= 0) {
			timeDisplay.append("00");
		} else if (timeMin < 10) {
			timeDisplay.append("0");
			timeDisplay.append(timeMin);
		} else {
			timeDisplay.append(timeMin);
		}
		timeDisplay.append(":");
		if (timeRestSec <= 0) {
			timeDisplay.append("00");
		} else if (timeRestSec < 10) {
			timeDisplay.append("0");
			timeDisplay.append(timeRestSec);
		} else {
			timeDisplay.append(timeRestSec);
		}
		return timeDisplay.toString();
	}
	
}
