package com.alkaid.ojpl.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

public class SpannableStringUtil {
	public static final String B_END_TAG = "[/b]";
	public static final String B_START_TAG = "[b]";
	public static final String CN_END_TAG = "[/cn]";
	public static final String CN_START_TAG = "[cn]";
	public static final String EN_END_TAG = "[/en]";
	public static final String EN_START_TAG = "[en]";
	public static final String H1_END_TAG = "[/h1]";
	public static final String H1_START_TAG = "[h1]";
	public static final String H2_END_TAG = "[/h2]";
	public static final String H2_START_TAG = "[h2]";
	public static final String IMG_END_TAG = "[/img]";
	public static final String IMG_START_TAG = "[img]";
	public static Context context;

	public SpannableStringUtil(Context context) {
		this.context = context;
	}

	public static SpannableStringBuilder string2SpanStr(String paramString) {
		return replaceColor(replaceUrl(replaceH2(replaceB(replaceH1(replaceImg(replaceEN(replaceCN(new SpannableStringBuilder(
				paramString)))))))));
	}

	private static Bitmap getBitmap(String fileName) {
		return BitmapFactory.decodeFile(Constants.PATH_RES + "/" + fileName);
	}

	public static SpannableStringBuilder replaceB(
			SpannableStringBuilder spBuilder) {
		return replaceTAG(spBuilder, B_START_TAG, B_END_TAG, new MySpan() {
			@Override
			protected Object createSpan() {
				return new StyleSpan(Typeface.BOLD);
			}
		});
	}

	public static SpannableStringBuilder replaceCN(
			SpannableStringBuilder spBuilder) {
		return replaceTAG(spBuilder, CN_START_TAG, CN_END_TAG, new MySpan() {
			@Override
			protected Object createSpan() {
				return new ForegroundColorSpan(Color.parseColor("#006200"));
			}
		});
	}

	public static SpannableStringBuilder replaceEN(
			SpannableStringBuilder spBuilder) {
		return replaceTAG(spBuilder, EN_START_TAG, EN_END_TAG, new MySpan() {
			@Override
			protected Object createSpan() {
				return new ForegroundColorSpan(Color.parseColor("#000000"));
			}
		});
	}

	public static SpannableStringBuilder replaceH1(
			SpannableStringBuilder spBuilder) {
		// TODO h1尺寸暂定为38 待改
		return replaceTAG(spBuilder, H1_START_TAG, H1_END_TAG, new MySpan() {
			@Override
			protected Object createSpan() {
				return new AbsoluteSizeSpan(38);
			}
		});
	}

	public static SpannableStringBuilder replaceH2(
			SpannableStringBuilder spBuilder) {
		// TODO h1尺寸暂定为29 待改
		return replaceTAG(spBuilder, H2_START_TAG, H2_END_TAG, new MySpan() {
			@Override
			protected Object createSpan() {
				return new AbsoluteSizeSpan(29);
			}
		});
	}

	/**
	 * 替换普通标签 适用场合：1.根据标签2.根据标签容器里的具体内容来生成span</br> 
	 * 形如1. [h1][/h1]  2.  [img]app/icon.jpg[/img]</br>
	 * 传入的mySpan须实现{@link MySpan#createSpan()} 或 {@link MySpan#createSpan(String)}方法，方法参数为标签容器内的具体内容
	 * @param spBuilder SpannableStringBuilder
	 * @param tagStart 开始标签
	 * @param tagEnd 结束标签
	 * @param mySpan 自定义span
	 * @return
	 */
	public static SpannableStringBuilder replaceTAG(
			SpannableStringBuilder spBuilder, String tagStart, String tagEnd,
			MySpan mySpan) {
		int startIndex = 0;
		int endIndex = 0;
		while (true) {
			startIndex = spBuilder.toString().indexOf(tagStart, endIndex);
			if (startIndex < 0)
				break;
			endIndex = spBuilder.toString().indexOf(tagEnd, startIndex);
			mySpan.setText(spBuilder.toString().substring(
					startIndex + tagStart.length(), endIndex));
			spBuilder.setSpan(mySpan.getSpan(), startIndex + tagStart.length(),
					endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startIndex = spBuilder.toString().indexOf(tagStart, startIndex);
			spBuilder.replace(startIndex, startIndex + tagStart.length(), "");
			endIndex = spBuilder.toString().indexOf(tagEnd, startIndex);
			spBuilder.replace(endIndex, endIndex + tagEnd.length(), "");
		}
		return spBuilder;
	}

	/**
	 * 替换带属性的UBB标签 适用场合:根据标签属性来生成span </br>形如[url=http://alkaid.com]alkaid[/url]
	 * 
	 * @param spBuilder
	 * @param tagStart
	 * @param tagEnd
	 * @param mySpan
	 * @return
	 */
	public static SpannableStringBuilder replaceTAGWidthAttr(
			SpannableStringBuilder spBuilder, String tagStart, String tagEnd,
			MySpan mySpan) {
		// String tagStart="[url=";
		// String tagEnd="[/url]";
		int startIndex = 0;
		int endIndex = 0;
		while (true) {
			startIndex = spBuilder.toString().indexOf(tagStart, endIndex);
			if (startIndex < 0)
				break;
			int startTagEndIndex = spBuilder.toString()
					.indexOf("]", startIndex);
			String attrText = spBuilder.toString().substring(
					startIndex + tagStart.length(), startTagEndIndex);
			endIndex = spBuilder.toString().indexOf(tagEnd, startIndex);
			mySpan.setText(attrText);
			spBuilder.setSpan(mySpan.getSpan(), startTagEndIndex + 1, endIndex,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startIndex = spBuilder.toString().indexOf(tagStart, startIndex);
			spBuilder.replace(startIndex, startIndex + tagStart.length()
					+ attrText.length() + 1, "");
			endIndex = spBuilder.toString().indexOf(tagEnd, startIndex);
			spBuilder.replace(endIndex, endIndex + tagEnd.length(), "");
		}
		return spBuilder;
	}

	public static SpannableStringBuilder replaceImg(
			SpannableStringBuilder spBuilder) {
		MySpan mySpan = new MySpan() {

			@Override
			public Object createSpan(String text) {
				ImageSpan imageSpan = new ImageSpan(getBitmap(text),
						DynamicDrawableSpan.ALIGN_BASELINE);
				return imageSpan;
			}
		};
		return replaceTAG(spBuilder, IMG_START_TAG, IMG_END_TAG, mySpan);
	}

	public static SpannableStringBuilder replaceUrl(
			SpannableStringBuilder spBuilder) {
		MySpan mySpan = new MySpan() {
			@Override
			protected Object createSpan(String text) {
				return new URLSpan(text);
			}
		};
		return replaceTAGWidthAttr(spBuilder, "[url=", "[/url]", mySpan);
	}

	public static SpannableStringBuilder replaceColor(
			SpannableStringBuilder spBuilder) {
		MySpan mySpan = new MySpan() {
			@Override
			protected Object createSpan(String text) {
				return new ForegroundColorSpan(Color.parseColor(text));
			}
		};
		return replaceTAGWidthAttr(spBuilder, "[color=", "[/color]", mySpan);
	}

	/**
	 * 自定义span 主要用于根据标签属性或标签内容动态生成span
	 * @author Alkaid
	 *
	 */
	static abstract class MySpan {
		private String text;
		/** 创建并返回span 子类根据需要选择实现该方法还是实现{@link #createSpan(String)}方法 */
		protected Object createSpan() {
			return null;
		};
		/**
		 * 创建并返回span 子类根据需要选择实现该方法还是实现{@link #createSpan()} 方法
		 * @param text 标签之间的具体内容
		 * @return
		 */
		protected Object createSpan(String text) {
			return null;
		};
		/**
		 * 将标签内容赋值到{@link MySpan}
		 * @param text
		 */
		public void setText(String text) {
			this.text = text;
		}
		/**
		 * 获得span 该方法会回调{@link #createSpan()}或{@link #createSpan(String)}
		 * @return
		 */
		public Object getSpan() {
			Object span=createSpan(text);
			return null==span?createSpan():span;
		}
	}
}
