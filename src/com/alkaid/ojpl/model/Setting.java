package com.alkaid.ojpl.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.Gravity;

import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.common.ViewUtil;

public class Setting {
	private Context context;
	Global global;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private static final String STORE_NAME = "Settings";

	public static final int PLAYMODE_ONE = 2;
	public static final int PLAYMODE_SELECT = 0;
	public static final int PLAYMODE_SEQUENCE = 1;
	private int playMode;

	private int DEFAULT_FONT=0;
	private int fontId;
	private Typeface font;
	private Typeface[] fonts;

	public int DEFAULT_LINESPACING;
	private int lineSpacing;

	public int DEFAULT_SIZE;
	private int textSize;

	private int align;
	private int alignId;
	private int[] aligns;
	private int previous_align;
	private int previous_font;
	private int previous_play;
	
	public Setting(Context context){
		this.context=context;
		global=Global.getGlobal(context);
		initParameter(context);
	}
	
	
	public void initParameter(Context context)
	  {
	    this.context = context;
	    this.settings =  context.getSharedPreferences(STORE_NAME, 0);
	    this.editor = this.settings.edit();
	    this.DEFAULT_SIZE = ViewUtil.getAdjustFontSize(context);
	    this.DEFAULT_LINESPACING =ViewUtil.getAdjustLineSpacing(context);
	    this.playMode = this.settings.getInt("playMode", PLAYMODE_SELECT);
	    this.fontId =this.settings.getInt("font", 0);
	    this.textSize = settings.getInt("textSize", DEFAULT_SIZE);
	    this.lineSpacing = settings.getInt("lineSpacing", DEFAULT_LINESPACING);;
	    this.alignId  =this.settings.getInt("align", DEFAULT_FONT);
	    fonts=new Typeface[3];
	    fonts[0] = Typeface.SANS_SERIF;
	    fonts[1] =  Typeface.SERIF;
	    fonts[2] = Typeface.MONOSPACE;
	    this.font = fonts[fontId];
	    aligns=new int[3];
	    this.aligns[0] = Gravity.LEFT;
	    this.aligns[1] = Gravity.RIGHT;
	    this.aligns[2] = Gravity.CENTER;
	    this.align = aligns[alignId];
	  }

	public void saveSetting() {
		this.editor.putInt("font", fontId).putInt("textSize", textSize)
				.putInt("align", alignId).putInt("lineSpacing", lineSpacing)
				.putInt("playMode", playMode).commit();
	}

	/**
	 * 获得默认日文字体
	 * @return
	 */
	public static Typeface getJpFontType(Context context){
		return Typeface.createFromAsset(context.getAssets(),
				"font/micross.ttf");
	}
	


	public int getPlayMode() {
		return playMode;
	}


	public void setPlayMode(int playMode) {
		this.playMode = playMode;
	}


	public Typeface getFont() {
		return font;
	}


	public void setFont(Typeface font) {
		this.font = font;
	}
	
	public void setFont(int fontId){
		this.fontId=fontId;
		this.font=fonts[fontId];
	}


	public int getLineSpacing() {
		return lineSpacing;
	}


	public void setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
	}


	public int getTextSize() {
		return textSize;
	}


	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}


	public int getAlign() {
		return align;
	}
	
	public void setAlign(int alighId){
		this.alignId=alighId;
		this.align=aligns[alighId];
	}

}
