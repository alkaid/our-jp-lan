package com.alkaid.ojpl.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.data.FiftyMapDao;
import com.alkaid.ojpl.model.Setting;
import com.alkaid.ojpl.view.ui.NavgationLayout;
import com.alkaid.ojpl.view.ui.WorkSpace;

public class FiftyMap extends BaseActivity {
	//全局、content、数据实体、进度框
	private Context context;
	private ProgressDialog progressDialog;
	
	//头部控件
	private RelativeLayout rlTitle;
	private TextView tvTitle;
	private PopupWindow modelDialog;	//模式选择框
	//导航栏
	private LinearLayout llTab;
	private NavgationLayout navgationLayout;
	private List<String> tabs=new ArrayList<String>();
	
	//数据展示区域
	private int tabNumber;
	private int previousView;
	private int currentView = 0;
	private LinearLayout llWorkSpace;
	private WorkSpace workspace;
	private Typeface fontType=Typeface.DEFAULT;
	/** 罗马音标 */
	private List<String[][]> romes=new ArrayList<String[][]>();
	List<TextView> tvRomes=new ArrayList<TextView>();
	/** 平假名 */
	private List<String[][]> hiraganas=new ArrayList<String[][]>();
	List<TextView> tvHiraganas=new ArrayList<TextView>();
	/** 片假名 */
	private List<String[][]> katakanas=new ArrayList<String[][]>();
	List<TextView> tvKataganas=new ArrayList<TextView>();
	/** 行头 */
	private List<String[]> spanHeads=new ArrayList<String[]>();
	/** 列头 */
	private List<String[]> colHeads=new ArrayList<String[]>();
	//音频路径和数据路径
	private final String assets_audio="fiftymap/audio";
	
	private static final String[] modelItems={"显示平假名","显示片假名","显示罗马音标"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//全屏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lesson_contents);
		this.context = this;
		//音量键改为默认调整媒体音量
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//初始化导航栏按钮数量也是workspace屏幕数量
		tabs.add("清音");
		tabs.add("浊音·半浊音");
		tabs.add("坳音");
		this.tabNumber = tabs.size();
		//隐藏播放器
		ViewGroup rlAudio=(ViewGroup)findViewById(R.id.rlAudio);
		rlAudio.setVisibility(View.GONE);
		//初始化默认设置
		findView();
		initModelSettingView();
		initTitle();
		initSelection();
		fontType =Setting.getJpFontType(context);
		//加载数据
		new GetLessonTask().execute();
	}
	
	/** findViewById */
	private void findView(){
		this.rlTitle = (RelativeLayout) findViewById(R.id.rlTitle);
		this.llTab = (LinearLayout) findViewById(R.id.llTab);
		this.llWorkSpace = (LinearLayout) findViewById(R.id.llArticle);
		this.tvTitle = (TextView) findViewById(R.id.tvTitle);
	}
	
	/** 初始化WorkSpace 加载数据 */
	private void initWorkSpace() {
		workspace=new WorkSpace(context, null);
		//workspace切换完成后的动作
		workspace
				.setOnScreenChangedListenner(new WorkSpace.OnScreenChangedListenner() {
					@Override
					public void onChanged(int preScreen, int currentScreen) {
						//导航栏设置选中
						navgationLayout.setSelected(currentScreen);
						FiftyMap.this.previousView = preScreen;
						FiftyMap.this.currentView = currentScreen;
					}
				});
		//为workspace的每个View加载数据
		for (int i = 0; i < tabNumber; i++) {
			initWorkspaceScreenView(workspace, spanHeads.get(i), colHeads.get(i), hiraganas.get(i), katakanas.get(i), romes.get(i));
		}
		workspace.setToScreen(currentView);
		//将workspace动态添加到父视图
		llWorkSpace.addView(workspace, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	/**
	 * 异步加载课文数据 加载完成后更新界面
	 * @author Alkaid
	 *
	 */
	private class GetLessonTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... voids) {
			return loadData();
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (FiftyMap.this.progressDialog == null) {
				FiftyMap.this.progressDialog = new ProgressDialog(
						context);
				progressDialog
						.setMessage(context.getText(R.string.loading));
			}
			FiftyMap.this.progressDialog.show();
		}
		@Override
		protected void onPostExecute(Boolean success) {
			if (!success)
				FiftyMap.this.goBack();
			//SetData
			if(null==workspace)
				initWorkSpace();
			if(null!=progressDialog){
				progressDialog.dismiss();
			}
			super.onPostExecute(success);
		}
	}
	/** 加载数据 */
	private boolean loadData() {
		FiftyMapDao fiftyMapDao=new FiftyMapDao(this);
		fiftyMapDao.getHeads(spanHeads, colHeads);
		fiftyMapDao.getData(hiraganas, katakanas, romes);
		return true;
	}
	
	/** 初始化标题栏 */
	private void initTitle() {
		//标题栏
		tvTitle.setText("五十音图");
		tvTitle.setTextSize(15*global.dm.scaledDensity);
		Button btnSetting = (Button) findViewById(R.id.btnSetting);
		btnSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				modelDialog.showAsDropDown(v);
			}
		});
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});
	}
	/** 初始化导航栏 */
	private void initSelection() {
		navgationLayout=new NavgationLayout(context, tabs,3);
		navgationLayout.setOnClickNavBtnListenner(new NavgationLayout.OnClickNavBtnListenner() {
			@Override
			public void onClick(View v, int position) {
				flipperMove(position);
			}
		});
		llTab.addView(navgationLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	/** 切屏 */
	private void flipperMove(int whichScreen) {
		this.workspace.snapToScreen(whichScreen);
	}

	private void goBack() {
		finish();
	}

	

	
	/**
	 * 初始化workspace视图的其中一屏
	 * @param workspace
	 * @param textView
	 */
	private void initWorkspaceScreenView(WorkSpace workspace,String[] mspanHeads,String[] mcolHeads,String[][] mhiraganas,String[][] mkatakanas,String[][] mromes) {
		int fill=LayoutParams.FILL_PARENT;
		ScrollView scrollView = new ScrollView(context);
		LinearLayout layBody=new LinearLayout(context);
		layBody.setOrientation(LinearLayout.VERTICAL);
		int cellWidth=global.width/(mcolHeads.length+1);
		int cellHeight=(int) (cellWidth*0.9);
		int headWidth=(int) (cellWidth*0.8);
		int headHeight=(int) (cellHeight*0.7);
		cellWidth=(global.width-headWidth)/mcolHeads.length;
		int hiraganaWidth=cellWidth/2;
		int hiraganaHeight=cellHeight/2;
		for(int i=-1;i<mspanHeads.length;i++){
			LinearLayout row=new LinearLayout(context);
			row.setOrientation(LinearLayout.HORIZONTAL);
			for(int j=-1;j<mcolHeads.length;j++){
				LinearLayout layCell=new LinearLayout(context);
				layCell.setOrientation(LinearLayout.VERTICAL);
				layCell.setBackgroundResource(R.drawable.sel_cell);
				//第一行时时设置列标题
				if(i==-1){
					if(j!=-1){
						TextView tvColHead=new TextView(context);
						tvColHead.setText(mcolHeads[j]);
						setHeadTextProperty(tvColHead);
						layCell.addView(tvColHead,new LayoutParams(fill,fill));
					//2.0版本以下有个bug:
						//如果你将LinearLayout作为一个View添加到根目录中，但是这个LinearLayout没有子View的话，
						//mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.
					}else{
						layCell.addView(new View(context));
					}
				}else if(j==-1){	//第一列时设置行标题
					TextView tvSpanHead=new TextView(context);
					tvSpanHead.setText(mspanHeads[i]);
					setHeadTextProperty(tvSpanHead);
					layCell.addView(tvSpanHead,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				}else{
					TextView tvHiragana=new TextView(context);
					TextView tvKatagana=new TextView(context);
					TextView tvRome=new TextView(context);
					tvHiragana.setText(mhiraganas[i][j]);
					tvKatagana.setText(mkatakanas[i][j]);
					tvRome.setText(mromes[i][j]);
					setTextProperty(tvKatagana);
					setTextProperty(tvHiragana);
					setRomaTextProperty(tvRome);
					LinearLayout layGana=new LinearLayout(context);
					layGana.addView(tvHiragana,new LayoutParams(hiraganaWidth,hiraganaHeight));
					layGana.addView(tvKatagana,new LayoutParams(hiraganaWidth,hiraganaHeight));
//					((LinearLayout.LayoutParams)tvHiragana.getLayoutParams()).weight=1;
//					((LinearLayout.LayoutParams)tvKatagana.getLayoutParams()).weight=1;
					layCell.addView(layGana);
					layCell.addView(tvRome,new LayoutParams(cellWidth,hiraganaHeight));
//					TextView tvCell=new TextView(context);
//					setTextProperty(tvCell);
//					tvCell.setText(mhiraganas[i][j]+"  "+mkatakanas[i][j]+"\n"+mromes[i][j]);
//					layCell.addView(tvCell);
					if(!"-".equals(mromes[i][j])){
						layCell.setOnClickListener(new OnButtonClick(mromes[i][j]));
					}
					tvHiraganas.add(tvHiragana);
					tvKataganas.add(tvKatagana);
					tvRomes.add(tvRome);
				}
				int pw=j==-1?headWidth:cellWidth;
				int ph=i==-1?headHeight:cellHeight;
				row.addView(layCell, new LinearLayout.LayoutParams(pw,ph));
			}
			LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(global.width,i==-1?headHeight:cellHeight);
			rowParams.setMargins(0, 0, 0, (int) (5*global.dm.density));
			layBody.addView(row, rowParams);
		}
		scrollView.addView(layBody);
		workspace.addView(scrollView);
	}
	
	private void setTextProperty(TextView tv){
		tv.setGravity(Gravity.CENTER);
		tv.setTypeface(fontType);
		tv.setTextColor(context.getResources()
				.getColor(R.color.textcolor));
		tv.setTextSize(12*global.dm.scaledDensity);
	}
	private void setHeadTextProperty(TextView tv){
		tv.setGravity(Gravity.CENTER);
		tv.setTypeface(fontType);
		tv.setTextColor(context.getResources()
				.getColor(R.color.green_classic));
		tv.setTextSize(11*global.dm.scaledDensity);
	}
	private void setRomaTextProperty(TextView tv){
		tv.setGravity(Gravity.CENTER);
		tv.setTypeface(fontType);
		tv.setTextColor(context.getResources()
				.getColor(R.color.orange));
		tv.setTextSize(11*global.dm.scaledDensity);
	}
	
	private class OnButtonClick implements View.OnClickListener{
		String mp3;
		public OnButtonClick(String mp3Name) {
			this.mp3=mp3Name;
		}
		@Override
		public void onClick(View v) {
			String url=assets_audio+"/"+mp3+".mp3";
			MediaPlayer player=new MediaPlayer();
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			});
			AssetFileDescriptor afd;
			try {
				afd = context.getAssets().openFd(url);
			} catch (IOException e) {
				LogUtil.e(e);
				return;
			}
			try {
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),afd.getLength());
			} catch (IllegalArgumentException e) {
				LogUtil.e(e);
				return;
			} catch (IllegalStateException e) {
				LogUtil.e(e);
				return;
			} catch (IOException e) {
				LogUtil.e(e);
				return;
			}
			try {
				player.prepare();
			} catch (IllegalStateException e) {
				LogUtil.e(e);
				return;
			} catch (IOException e) {
				LogUtil.e(e);
				return;
			}
			player.start();
		}
	}
	
	private void displayTextView(List<TextView> tvs,boolean isShow){
		int visibility=isShow?View.VISIBLE:View.GONE;
		for(TextView tv:tvs){
			tv.setVisibility(visibility);
		}
	}
	
	private void initModelSettingView(){
		ListView modelList=new ListView(context);
		modelList.setAdapter(new SettingListAdapter(context, modelItems) {
			@Override
			protected void onChecked(CompoundButton buttonView, boolean isChecked,
					int position) {
				List<TextView> tvs=null;
				switch (position) {
				case 0:
					tvs=tvHiraganas;
					break;
				case 1:
					tvs=tvKataganas;
					break;
				case 2:
					tvs=tvRomes;
					break;
				default:
					break;
				}
				displayTextView(tvs, isChecked);
			}
		});
		modelDialog=new PopupWindow(modelList,(int) (130*global.dm.density),LayoutParams.WRAP_CONTENT);
		modelDialog.setFocusable(true);
		modelDialog.setOutsideTouchable(true);
		Drawable background=getResources().getDrawable(R.drawable.dialog);
		background.setAlpha(200);
		modelDialog.setBackgroundDrawable(background);
		modelDialog.update();
	}
	
	private abstract class SettingListAdapter extends BaseAdapter{
		LayoutInflater inflater;
		String[] items;
		boolean[] checks;
		public SettingListAdapter(Context context,String[] items){
			this.items=items;
			inflater = LayoutInflater.from(context);
			checks=new boolean[items.length];
			for(int i=0;i<checks.length;i++)
				checks[i]=true;
		}
		@Override
		public int getCount() {
			return items.length;
		}
		@Override
		public Object getItem(int position) {
			return items[position];
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder=null;
			if(null==convertView){
				viewHolder=new ViewHolder();
				convertView=inflater.inflate(R.layout.fiftymap_setting_item, null);
				viewHolder.tvModleName=(TextView) convertView.findViewById(R.id.tvModleName);
				viewHolder.cbModle=(CheckBox) convertView.findViewById(R.id.cbModle);
				convertView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder) convertView.getTag();
			}
			viewHolder.cbModle.setOnCheckedChangeListener(null);
			viewHolder.tvModleName.setText(items[position]);
			viewHolder.cbModle.setChecked(checks[position]);
			final int p=position;
			viewHolder.cbModle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checks[p]=isChecked;
					onChecked(buttonView, isChecked, p);
				}
			});
			return convertView;
		}
		/**
		 * checkbox选中改变时
		 * @param buttonView  checkbox
		 * @param isChecked  是否选中
		 * @param position	在list中位置
		 */
		protected abstract void onChecked(CompoundButton buttonView, boolean isChecked,int position);
		
		class ViewHolder{
			TextView tvModleName;
			CheckBox cbModle;
		}
	}
}
