/**
 * 
 */
package com.alkaid.ojpl.view.ui;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.common.ViewUtil;

/**
 * @author Alkaid
 * 顶部导航栏
 */
public class NavgationLayout extends FrameLayout {
	/** 一屏要显示的控件数 */
	private int tabNumber=DEFAULT_TABNUMBER;
	private static final int DEFAULT_TABNUMBER=5;
	private List<String> data;
	private Context context;
	private MultiViewWorkSpace workspace;
	private LinkedList<TextView> tvNavBtns;
	private ImageView floatMove;
	private ImageView[] moves;
//	private Animation textOut;
//	private Animation textIn;
	private TranslateAnimation moveAnim;
	
	private int selected=0;
	private int preSelected=0;
	
	private DisplayMetrics dm;
	
	private OnClickNavBtnListenner onClickNavBtnListenner;

	/**
	 * 
	 * @param context
	 * @param data  titleBar 按钮文字
	 * @param tabNumber	一屏要分割成多少份
	 */
	public NavgationLayout(Context context,List<String> data,int tabNumber) {
		super(context);
		this.tabNumber=tabNumber;
		this.context=context;
		dm=new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		this.data=data;
		initSelection();
		initAnimation();
	}
	public NavgationLayout(Context context,List<String> data) {
		this(context,data,DEFAULT_TABNUMBER);
	}
	
	private void initSelection() {
		//导航栏背景
//		this.setBackgroundResource(R.drawable.lyriclist_button_unselect);
		workspace=new MultiViewWorkSpace(context, null);
		float fontSize = ViewUtil.getAdjustFontSize(context)-4;
		LayoutParams fiilParentsParam=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		this.tvNavBtns = new LinkedList<TextView>();
		moves=new ImageView[data.size()];
		//布局
		LayoutParams flNavLayoutParam=new LayoutParams(dm.widthPixels/tabNumber, LayoutParams.FILL_PARENT);
		FrameLayout.LayoutParams navSpecImgLayParam = new FrameLayout.LayoutParams(
				dm.widthPixels / 32 /tabNumber, (int) (25*dm.density));
		navSpecImgLayParam.gravity=Gravity.CENTER_VERTICAL;
		navSpecImgLayParam.leftMargin = dm.widthPixels / tabNumber-dm.widthPixels / 32 /tabNumber;
		LayoutParams textLayoutParams=new LayoutParams(dm.widthPixels / tabNumber-dm.widthPixels / 32 /tabNumber,LayoutParams.FILL_PARENT);
		//选中按钮的背景图
		Bitmap imgTemp = BitmapFactory.decodeResource(
				this.context.getResources(), R.drawable.tab_focus);
		int width = dm.widthPixels/tabNumber;
		int height = (int) (23*dm.density);	//28dp
		Bitmap imgMove = Bitmap.createScaledBitmap(imgTemp, width, height, true);
		imgTemp.recycle();
		LayoutParams moveParams=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		moveParams.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		
		for(int i=0;i<data.size();i++){
			FrameLayout flNav=new FrameLayout(context);
			//分隔符
			/*if(i<data.size()-1){
				ImageView ivNavSeperator=new ImageView(context);
				ivNavSeperator.setBackgroundResource(R.drawable.line3);
				flNav.addView(ivNavSeperator, navSpecImgLayParam);
			}*/
			//核心 TextView
			TextView navTxtVw = new TextView(context);
			navTxtVw.setId(i);
			navTxtVw.setText(data.get(i));
			navTxtVw.setGravity(Gravity.CENTER);
			navTxtVw.setTextSize(fontSize);
			navTxtVw.setTextColor(getResources().getColor(R.color.white));
			final int position=i;
			navTxtVw.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(null!=onClickNavBtnListenner)
						onClickNavBtnListenner.onClick(v, position);
				}
			});
			tvNavBtns.add(navTxtVw);
			flNav.addView(navTxtVw, textLayoutParams);
			//选中按钮
			ImageView move = new ImageView(this.context);
			move.setImageBitmap(imgMove);
			move.setScaleType(ScaleType.CENTER);
			move.setVisibility(View.INVISIBLE);	//TODO 必须用INVISIBLE 用GONE有动画完成后move不显示的bug 原因不明 待查
			moves[i]=move;
			flNav.addView(move, moveParams);
			
			workspace.addView(flNav, flNavLayoutParam);
		}
//		tvNavBtns.get(0).setTextColor(android.R.color.black);
		moves[0].setVisibility(View.VISIBLE);
		//漂浮按钮设置，是为了动画显示
		floatMove = new ImageView(this.context);
		floatMove.setImageBitmap(imgMove);
		floatMove.setScaleType(ScaleType.CENTER);
		floatMove.setVisibility(View.GONE);
		floatMove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.e(" i'm floatMove!");
			}
		});
		LayoutParams floatMoveParam = new LayoutParams(
				dm.widthPixels / tabNumber, height);
		floatMoveParam.gravity=Gravity.BOTTOM;
		this.addView(workspace,fiilParentsParam);
		this.addView(floatMove, floatMoveParam);
	}
	
	/**
	 * 选中导航栏其中一个按钮
	 * @param index
	 */
	public void setSelected(int index){
		workspace.snapToScreen(index);
		if(index==this.selected)
			return;
		else{
			preSelected=this.selected;
			this.selected=index;
			moveAnim = new TranslateAnimation(workspace.getChildX(preSelected),workspace.getChildX(selected), 0, 0);
			moveAnim.setAnimationListener(new MyAnimListener());
			moveAnim.setFillAfter(false);
			moveAnim.setDuration(250L);
			floatMove.setVisibility(View.VISIBLE);
//			moves[preSelected].setVisibility(View.GONE);
			floatMove.startAnimation(moveAnim);
		}
	}
	
	
	public void setOnClickNavBtnListenner(
			OnClickNavBtnListenner onClickNavBtnListenner) {
		this.onClickNavBtnListenner = onClickNavBtnListenner;
	}


	public static interface OnClickNavBtnListenner{
		public void onClick(View v,int position);
	}
	
	
	/**
	 * 动画监听
	 * @author Alkaid
	 *
	 */
	private class MyAnimListener implements Animation.AnimationListener {
		@Override
		public void onAnimationEnd(Animation anim) {
			if (anim == moveAnim) {
				moves[selected].setVisibility(View.VISIBLE);
				floatMove.setVisibility(GONE);
//				tvNavBtns.get(preSelected).startAnimation(textOut);
//				tvNavBtns.get(selected).setTextColor(android.R.color.black);
				return;
			}
//			if (anim == textOut) {
//				tvNavBtns.get(preSelected).setTextColor(
//						getResources().getColor(R.color.white));
//				TextView txtPre = tvNavBtns.get(preSelected);
//				txtPre.startAnimation(textIn);
//				return;
//			}
		}
		public void onAnimationRepeat(Animation anim) {}
		public void onAnimationStart(Animation anim) {
			if (anim == moveAnim) {
				moves[preSelected].setVisibility(View.GONE);
				return;
			}
		}
	}
	
	/** 初始化动画效果 */
	private void initAnimation() {
		MyAnimListener myAnimListenner = new MyAnimListener();
//		this.textOut = new AlphaAnimation(1.0F, 0.2F);
//		this.textOut.setDuration(50L);
//		this.textOut.setAnimationListener(myAnimListenner);
//		this.textIn = new AlphaAnimation(0.2F, 1.0F);
//		this.textIn.setDuration(50L);
//		this.textIn.setAnimationListener(myAnimListenner);
	}

}
