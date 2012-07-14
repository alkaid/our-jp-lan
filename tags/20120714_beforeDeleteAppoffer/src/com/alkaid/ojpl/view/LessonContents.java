package com.alkaid.ojpl.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.AlkaidException;
import com.alkaid.ojpl.common.AnimationLoader;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.DownLoader;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.common.SpannableStringUtil;
import com.alkaid.ojpl.common.ViewUtil;
import com.alkaid.ojpl.data.ArticleDao;
import com.alkaid.ojpl.data.BookItemOperator;
import com.alkaid.ojpl.data.LessonDao;
import com.alkaid.ojpl.model.Article;
import com.alkaid.ojpl.model.ArticleType;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.model.Lesson;
import com.alkaid.ojpl.model.Setting;
import com.alkaid.ojpl.view.ui.NavgationLayout;
import com.alkaid.ojpl.view.ui.Player;
import com.alkaid.ojpl.view.ui.SettingDialog;
import com.alkaid.ojpl.view.ui.WorkSpace;

public class LessonContents extends BaseActivity {
	//全局、content、数据实体、进度框
	private BookItem bookItem;
	private Lesson lesson;
	private int lessonId=1;
	private ProgressDialog progressDialog;
	
	//配置信息
	private Setting setting;
	
	//头部控件
	private RelativeLayout rlTitle;
	private TextView tvTitle;
	//导航栏
	private LinearLayout llTab;
	private NavgationLayout navgationLayout;
	/*private RelativeLayout rlNav;
	private LinearLayout llNav;
	private LinkedList<TextView> tvNavBtns;
	private ImageView[] ivNavSeperators;
	private ImageView move;
	private Animation textOut;
	private Animation textIn;
	private TranslateAnimation moveAnim;*/
	
	//数据展示区域
	private int tabNum;
	private int articlesNum;
	private int previousView;
	private int currentView = 0;
	private LinearLayout llWorkSpace;
	private TextView[] tvArticles;
	private WorkSpace workspace;
	private Animation open;
	private Animation close;
	
	//音频播放相关
	private Player player;
	private int playMode;
	private int currentMp3 = 0;
	private boolean isPlaying;
	private RelativeLayout rlAudio;
	private LinearLayout llSeekbar;
	private SeekBar sbAudio;
	private Button btnPlay;
	private TextView tvCurrentType;
	private TextView tvCurrentTime;
	private TextView tvTotalTime;
	private Button btnReplay;
	private Button btnNext;
	private Button btnPrevious;
	private int[] seekPosition;
	private String[] currentTimeJ;
	private String[] finalTimeJ;
	private String nonMp3Url;	//当aticle没有mp3url时使用这个url
	private String nonMp3TypeZh;//当aticle没有mp3url时使用这个typeZh
	
	//视频相关
	private TextView tvVideoInfo;
	private Button btnVideoDelete;
	private Button btnVideoDownload;
	private Button btnVideoPlay;
	private ProgressBar pbVideoDownload;
	private DownLoader downLoader;
	private int videoDownStatus;//视频下载状态
	private View videoOpView;
	
	// private TextView exercise;
	// private LyricAdapter[] lrcAdapters;
	// private ListView[] lyricLists;
	// private boolean ownExercise;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//全屏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
//				WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setContentView(R.layout.lesson_contents);
		this.context = this;
		//音量键改为默认调整媒体音量
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		String strLessonId=getIntent().getStringExtra(Constants.bundleKey.lessonId);
		if(null==strLessonId){
			strLessonId=savedInstanceState.getString(Constants.bundleKey.lessonId);
		}else{
			lessonId=Integer.parseInt(strLessonId);
		}
		bookItem=(BookItem) global.getData(Constants.bundleKey.bookItem);
		if(null==bookItem){
			String bookItemId=savedInstanceState.getString(Constants.bundleKey.bookItemId);
			bookItem=new BookItemOperator().getBookItemById(bookItemId, this);
		}
		this.articlesNum = global.getLessonTemplate().getArticles().size();
		//初始化导航栏按钮数量也是workspace屏幕数量  +1是视频功能
		tabNum=articlesNum+1;
		this.currentTimeJ = new String[articlesNum];
		this.isPlaying = false;
		this.seekPosition = new int[articlesNum];
		this.finalTimeJ = new String[articlesNum];
		this.tvArticles = new TextView[articlesNum];
		// this.lyricLists = new ListView[tabNumber];
		// this.lrcAdapters = new LyricAdapter[tabNumber];
		// this.index = 1;
		// this.ownExercise = true;
		//初始化默认设置
		this.setting = new Setting(context);
		this.playMode = this.setting.getPlayMode();
		findView();
		initTitle();
		initSelection();
		initController();
		initAnimation();
		//加载数据
		new GetLessonTask().execute();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(Constants.bundleKey.bookItemId, bookItem.getId());
		outState.putString(Constants.bundleKey.lessonId, lessonId+"");
		super.onSaveInstanceState(outState);
	}
	
	/** findViewById */
	private void findView(){
		this.rlTitle = (RelativeLayout) findViewById(R.id.rlTitle);
		this.llTab = (LinearLayout) findViewById(R.id.llTab);
		this.llWorkSpace = (LinearLayout) findViewById(R.id.llArticle);
		this.rlAudio = (RelativeLayout) findViewById(R.id.rlAudio);
		
		this.tvTitle = (TextView) findViewById(R.id.tvTitle);

		this.llSeekbar = (LinearLayout) findViewById(R.id.llSeekBar);
		this.btnPlay = (Button) findViewById(R.id.btnPlay);
		this.sbAudio = (SeekBar) findViewById(R.id.sbAudio);
		this.tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
		this.tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
		this.tvCurrentType = (TextView) findViewById(R.id.tvCurrentType);

		this.btnNext = (Button) findViewById(R.id.btnNext);
		this.btnPrevious = (Button) findViewById(R.id.btnPrevious);
		this.btnReplay = (Button) findViewById(R.id.btnReplay);
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
						LessonContents.this.previousView = preScreen;
						LessonContents.this.currentView = currentScreen;
						//记录前一屏音频面板状态
						if(preScreen<=articlesNum-1){
							seekPosition[preScreen] = sbAudio.getProgress();
							currentTimeJ[preScreen] = tvCurrentTime.getText().toString();
							finalTimeJ[preScreen] = tvTotalTime.getText().toString();
							Article preArticle=lesson.getArticles().get(preScreen);
							if(!TextUtils.isEmpty(preArticle.getMp3Url())){
								currentMp3=preScreen;
							}
						}
						//若当前屏幕索引超出文章长度，隐藏Mps controller
						if(currentScreen>articlesNum-1){
							rlAudio.setVisibility(View.GONE);
							if(null!=player)
								player.stopmusic();
							isPlaying = false;
							player = null;
							return;
						}
						
						
						rlAudio.setVisibility(View.VISIBLE);
						//更新mp3栏目信息
						Article article = lesson.getArticles().get(
								currentScreen);
						//TODO 这里有个小bug 直接跳到语法的话由于player没有初始化无法播放
						if (TextUtils.isEmpty(article.getMp3Url())){
							if(player==null){
								Article preArticle=lesson.getArticles().get(currentMp3);
								tvCurrentType.setText(preArticle.getType().getZh());
								isPlaying = false;
								btnPlay.setBackgroundResource(R.drawable.sel_play);
								player = new Mp3Player();
								player.setDataSource(preArticle.getMp3Url());
								player.mediaplayer.seekTo(seekPosition[currentMp3]
										* player.mediaplayer.getDuration()
										/ sbAudio.getMax());
							}
							return;
						}
						if(currentMp3==currentView&&player!=null){
							return;
						}
						currentMp3=currentView;
						tvCurrentType.setText(article.getType().getZh());
						btnNext.setVisibility(View.GONE);
						btnPrevious.setVisibility(View.GONE);
						btnReplay.setVisibility(View.GONE);
						llSeekbar.setVisibility(View.VISIBLE);
						btnPlay.setBackgroundResource(R.drawable.sel_play);
						if(null!=player)
							player.stopmusic();
						isPlaying = false;
						player = null;
						player = new Mp3Player();
						sbAudio.setProgress(seekPosition[currentScreen]);
						player.setDataSource(article.getMp3Url());
						// player.play();
						player.mediaplayer.seekTo(seekPosition[currentScreen]
								* player.mediaplayer.getDuration()
								/ sbAudio.getMax());
						// player.pause();

					}
				});
		//为workspace的每个View加载数据
		// TODO 只考虑双语和UBBText情况先
		for (int i = 0; i < articlesNum; i++) {
			Article article = global.getLessonTemplate().getArticles().get(i);
			if (article.getType() == ArticleType.shuangyu
					|| article.getType().isUBBText())
			tvArticles[i] = new TextView(context);
			initWorkspaceScreenView(workspace, tvArticles[i]);
		}
		//初始化视频页面
		initVideoForWorkspaceScreen(workspace);
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
			if (LessonContents.this.progressDialog == null) {
				LessonContents.this.progressDialog = new ProgressDialog(
						context);
				progressDialog
						.setMessage(context.getText(R.string.loading));
			}
			LessonContents.this.progressDialog.show();
			if(null!=player)
				player.stopmusic();
		}
		@Override
		protected void onPostExecute(Boolean success) {
			if (!success)
				LessonContents.this.goBack();
			//SetData
			if(null==workspace)
				initWorkSpace();
			setData();
			super.onPostExecute(success);
		}
	}
	/** 更新界面 主要是课文标题和mp3相关信息 */
	private void setData(){
		if (player != null)
			player.stopmusic();
		player = null;
		player = new Mp3Player();
		player.setDataSource(lesson.getArticles().get(currentMp3).getMp3Url());
		tvTitle.setText(lesson.getTitleJp());
		// TODO 只考虑双语情况和UBBText先
		for (int i = 0; i < articlesNum; i++) {
			Article article = lesson.getArticles().get(i);
			decorateArticle(tvArticles[i], article);
//			tvArticles[i].setText("ooxxsdfsdfsdfsdfswerfsdfsdfafsdfsafsdafasdfsdfsdfsdfsdooxxwekrjlsdfsooxx\nwer");
		}
		if (isPlaying)
			player.play();
		if (progressDialog != null)
			progressDialog.dismiss();
		llWorkSpace.startAnimation(open);
	}
	/** 加载数据 */
	private boolean loadData() {
		try {
			lesson = new LessonDao(context).getById(lessonId,bookItem);
			return true;
		} catch (AlkaidException e) {
			LogUtil.e(e);
			return false;
		}
		// TODO exicise
		// if (this.article.getExercise().size() == 0)
		// this.ownExercise = 0;
	}
	
	/** 初始化标题栏 */
	private void initTitle() {
		Button btnSetting = (Button) findViewById(R.id.btnSetting);
		btnSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				initSettingDialog();
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
		List<String> data=new ArrayList<String>();
		for(Article a:global.getLessonTemplate().getArticles()){
			data.add(a.getType().getZh());
		}
		//添加视频页签
		data.add("视频");
		navgationLayout=new NavgationLayout(context, data);
		navgationLayout.setOnClickNavBtnListenner(new NavgationLayout.OnClickNavBtnListenner() {
			@Override
			public void onClick(View v, int position) {
				flipperMove(position);
			}
		});
		llTab.addView(navgationLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	/** 初始化音乐播放器及其mp3信息 */
	private void initController() {
		for (int i = 0; i < this.articlesNum; i++) {
			this.currentTimeJ[i] = "00:00";
		}
		tvCurrentTime.setText("00:00");
		this.tvCurrentType.setText(global.getLessonTemplate().getArticles()
				.get(0).getType().getZh());
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (LessonContents.this.isPlaying) {
					pause();
					return;
				} else {
					v.setBackgroundResource(R.drawable.sel_pause);
					if (player == null) {
						Toast.makeText(context, R.string.initNotFinished,
								Toast.LENGTH_SHORT).show();
						return;
					}
					player.play();
					LessonContents.this.isPlaying = true;
				}
			}
		});
		btnReplay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disappear(btnPrevious);
				disappear(btnNext);
				disappear(btnReplay);
				if (player != null) {
					player.mediaplayer.seekTo(0);
					player.play();
					LessonContents.this.isPlaying = true;
					btnPlay.setBackgroundResource(R.drawable.sel_pause);
				}
				appear(llSeekbar);
			}
		});
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// global.setLessonPosition(global.getLessonPosition() - 1);
				// if (global.getLessonPosition() < 0)
				// global.setLessonPosition(global.getLessonGroupSize() - 1);
				lessonId=lesson.getGroupPreId();
				alterLesson();
			}
		});
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// global.setLessonPosition(global.getLessonPosition() + 1);
				// if (global.getLessonPosition() >
				// global.getLessonGroupSize()-1)
				// global.setLessonPosition(0);
				lessonId=lesson.getGroupNextId();
				alterLesson();
			}
		});
	}
	
	/**
	 * 修饰article的文本数据，生成SpannableStringBuilder并添加给TextView
	 * TODO 暂时只考虑双语和UBB文本的情况
	 * @param txtView
	 * @param article
	 */
	private void decorateArticle(TextView txtView, Article article) {
		ArticleDao.decorateArticle(article);
		String text = article.getText();
		switch (article.getType()) {
		case shuangyu:
			SpannableStringBuilder spBuilder = SpannableStringUtil
					.string2SpanStr(text.replace("◎", "[h2]◎[/h2]"));
			text = spBuilder.toString();
			String reglex = ".+\n";
			int i = 1;
			Pattern p = Pattern.compile(reglex);
			Matcher m = p.matcher(text);
			while (m.find()) {
				if (i % 2 == 0) {
					RelativeSizeSpan retSizeSpan = new RelativeSizeSpan(0.9F);
					ForegroundColorSpan fontColorSpan = new ForegroundColorSpan(
							getResources().getColor(R.color.green_classic));
					spBuilder.setSpan(retSizeSpan, m.start(), m.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					spBuilder.setSpan(fontColorSpan, m.start(), m.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				i += 1;
			}
			txtView.setText(spBuilder);
			break;
		default:
			if (article.getType().isUBBText()) {
				txtView.setText(SpannableStringUtil.string2SpanStr(article
						.getText()));
			}
			break;
		}
	}
	/** 切换课文 */
	private void alterLesson() {
		this.tvCurrentTime.setText("00:00");
		this.sbAudio.setProgress(0);
		if (this.btnReplay.getVisibility() == View.VISIBLE) {
			disappear(btnReplay);
			disappear(btnPrevious);
			disappear(btnNext);
			this.llSeekbar.setVisibility(View.VISIBLE);
		}
		llWorkSpace.startAnimation(close);
	}
	/** 音乐播放器按钮的出现动画 */
	private void appear(View view) {
		view.setVisibility(View.VISIBLE);
		view.startAnimation(AnimationLoader.inAnim);
	}
	/** 音乐播放器按钮的消失动画 */
	private void disappear(View paramView) {
		paramView.startAnimation(AnimationLoader.outAnim);
		paramView.setVisibility(View.GONE);
	}
	/** 切屏 */
	private void flipperMove(int whichScreen) {
		this.workspace.snapToScreen(whichScreen);
	}

	private void goBack() {
		if (this.player != null)
			this.player.stopmusic();
		// TODO exit Activity
		finish();
	}
	
	/*@Override
	protected void onStart() {
		super.onStart();
		if(null!=player&&isPlaying){
			player.play();
		}
	}*/

	@Override
	protected void onStop() {
		pause();
//		isPlaying=true;
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		if(null!=player)
			player.stopmusic();
		if(null!=downLoader)
			downLoader.pause();
		super.onDestroy();
	}

	
	/**
	 * 初始化workspace视图的其中一屏
	 * @param workspace
	 * @param textView
	 */
	private void initWorkspaceScreenView(WorkSpace workspace,TextView textView) {
		ScrollView scrollView = new ScrollView(context);
		LinearLayout layWithTxt=new LinearLayout(context);
//		scrollView.setFillViewport(true);
		LinearLayout.LayoutParams txtLayParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		txtLayParam.setMargins(global.width/ 20, 20,
				global.width / 20, 0);
		setTextProperty(textView,setting);
		Typeface fontType = Setting.getJpFontType(context);
		textView.setTypeface(fontType);
		textView.setTextColor(context.getResources()
				.getColor(R.color.textcolor));
		layWithTxt.addView(textView, txtLayParam);
		scrollView.addView(layWithTxt);
		workspace.addView(scrollView);
	}

	/**
	 * 初始化视频页签
	 * @param workSpace
	 */
	private void initVideoForWorkspaceScreen(WorkSpace workSpace){
		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		videoOpView=inflater.inflate(R.layout.video_operation, null);
		tvVideoInfo=(TextView) videoOpView.findViewById(R.id.tvVideoInfo);
		btnVideoDelete=(Button) videoOpView.findViewById(R.id.btnVideoDelete);
		btnVideoDownload=(Button) videoOpView.findViewById(R.id.btnVideoDownload);
		btnVideoPlay=(Button) videoOpView.findViewById(R.id.btnVideoPlay);
		pbVideoDownload=(ProgressBar) videoOpView.findViewById(R.id.pbVideoDownload);
		setTextProperty(tvVideoInfo, setting);
		downLoader=new DownLoader(1, lesson.getVideoDownUri(), lesson.getVideoPath(), handler, context){
			@Override
			protected void onDownloadBegin(long filesize) {
				super.onDownloadBegin(filesize);
				if(filesize>0){
					SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPreference.lessonConfig.name, Context.MODE_PRIVATE);
					String key=lesson.getId()+Constants.sharedPreference.lessonConfig.huihua_video_size_suffix;
					sharedPreferences.edit().putLong(key, filesize).commit();
				}
			}
		};
		//初始化下载状态
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPreference.lessonConfig.name, Context.MODE_PRIVATE);
		//用于判断本地文件和文件长度是否相等传递初始化状态
		String key=lessonId+Constants.sharedPreference.lessonConfig.huihua_video_size_suffix;
		downLoader.setFileSize(sharedPreferences.getLong(key, 0));
		long fileSize = downLoader.getFileSize();
		long localFileLength = downLoader.getLocalFileLength();
		videoDownStatus=DownLoader.DOWN_NONE;
		if(localFileLength<fileSize&&localFileLength!=0){
			videoDownStatus=DownLoader.DOWN_PAUSE;
		}else if(localFileLength == fileSize&&localFileLength!=0){
			videoDownStatus=DownLoader.DOWN_COMPLETE;
		}
		//初始化视频按钮组
		btnVideoPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context,VideoPlayerActivity.class);
				//下载完成播放本地
				if(videoDownStatus==DownLoader.DOWN_COMPLETE){
					intent.putExtra(Constants.bundleKey.videoIsOnline, false);
					intent.putExtra(Constants.bundleKey.videoUri, lesson.getVideoPath());
				}else{
					//播放网络
					intent.putExtra(Constants.bundleKey.videoIsOnline, true);
					intent.putExtra(Constants.bundleKey.videoUri, lesson.getVideoUri());
				}
				startActivity(intent);
			}
		});
		btnVideoDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(downLoader.isStop()){
					new Thread(){
						public void run() {
							downLoader.down();
						};
					}.start();
				}else{
					downLoader.pause();
				}
			}
		});
		btnVideoDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downLoader.deleteFile();
			}
		});
		workSpace.addView(videoOpView);
		updateVideoDownState(videoDownStatus, downLoader);
	}
	
	private void updateVideoDownState(int status,DownLoader downloader){
		videoDownStatus=status;
		int percent;
		switch (status) {
		case DownLoader.DOWN_NONE:
			tvVideoInfo.setText("视频未下载，您可以选择在线播放或下载到本地后播放");
			btnVideoPlay.setText("在线播放");
			btnVideoDownload.setText("下载");
			btnVideoDownload.setEnabled(true);
			btnVideoDelete.setEnabled(false);
			pbVideoDownload.setVisibility(View.INVISIBLE);
			break;
		case DownLoader.DOWN_PAUSE:
			tvVideoInfo.setText("任务暂停，您可以选择在线播放或下载到本地后播放");
			btnVideoPlay.setText("在线播放");
			btnVideoDownload.setText("续传");
			btnVideoDownload.setEnabled(true);
			btnVideoDelete.setEnabled(true);
			pbVideoDownload.setVisibility(View.VISIBLE);
			percent=downloader.getPercent();
			if(percent>=0&&percent<=100){
				int progress=(int) ((float)pbVideoDownload.getMax()/(float)100*percent);
				pbVideoDownload.setProgress(progress);
			}else{
				Toast.makeText(context, Constants.DOWNSIZEWRONG,Toast.LENGTH_LONG ).show();
				downloader.deleteFile();
				updateVideoDownState(DownLoader.DOWN_NONE,downloader);
			}
			break;
		case DownLoader.DOWN_BEGIN:
			tvVideoInfo.setText("任务开始，您可以选择在线播放或下载到本地后播放");
			btnVideoPlay.setText("在线播放");
			btnVideoDownload.setText("暂停");
			btnVideoDownload.setEnabled(true);
			btnVideoDelete.setEnabled(true);
			pbVideoDownload.setVisibility(View.VISIBLE);
			break;
		case DownLoader.DOWN_LOADING:
//			tvVideoInfo.setText("任务暂停，您可以选择在线播放或下载到本地后播放");
//			btnVideoPlay.setText("在线播放");
//			btnVideoDownload.setText("暂停");
//			btnVideoDownload.setEnabled(true);
//			btnVideoDelete.setEnabled(true);
//			sbVideoDownload.setVisibility(View.VISIBLE);
			percent=downloader.getPercent();
			if(percent>=0&&percent<=100){
				int progress=(int) ((float)pbVideoDownload.getMax()/(float)100*percent);
				pbVideoDownload.setProgress(progress);
			}else{
				Toast.makeText(context, Constants.DOWNSIZEWRONG,Toast.LENGTH_LONG ).show();
				downloader.deleteFile();
				updateVideoDownState(DownLoader.DOWN_NONE,downloader);
			}
			break;
		case DownLoader.DOWN_COMPLETE:
			tvVideoInfo.setText("任务完成，点击播放观看视频");
			btnVideoPlay.setText("   播  放   ");
			btnVideoDownload.setText("完成");
			btnVideoDownload.setEnabled(false);
			btnVideoDelete.setEnabled(true);
			pbVideoDownload.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.msgWhat.error:
				String tip = msg.getData().getString(Constants.bundleKey.errorMsg);
				Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
				break;
			case Constants.msgWhat.downstate_changed:
				int index=msg.arg1;
				int downState=msg.arg2;
				updateVideoDownState(downState, downLoader);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		};
	};

	private void pause() {
		if (!this.isPlaying || null==player)
			return;
		this.btnPlay.setBackgroundResource(R.drawable.sel_play);
		this.player.pause();
		this.isPlaying = false;
	}

	// public void setImage(ImageView imgView, Bitmap img){} //TODO setImage()

	// public void setLrc(int paramInt){} //TODO setLrc()

	private void setTextProperty(TextView textView,Setting setting) {
		textView.setTextSize(setting.getTextSize());
		textView.setLineSpacing(setting.getLineSpacing(), 1.0F);
		textView.setTypeface(setting.getFont());
	}

	/** 不同播放模式下如何显示音乐栏按钮*/
	private void showMediaButtons() {
		switch (playMode) {
		case Setting.PLAYMODE_SELECT:
			this.llSeekbar.setVisibility(View.GONE);
			appear(btnNext);
			appear(btnPrevious);
			appear(btnReplay);
			break;
		case Setting.PLAYMODE_SEQUENCE:
			lessonId=lesson.getGroupNextId();
			alterLesson();
			break;
		case Setting.PLAYMODE_ONE:
			if (this.player == null)
				return;
			this.player.mediaplayer.seekTo(0);
			this.player.play();
			this.isPlaying = true;
			btnPlay.setBackgroundResource(R.drawable.sel_pause);
			break;

		default:
			break;
		}
	}

	// private class LyricAdapter extends BaseAdapter{} //TODO LyricAdapter

	
	/** 初始化动画效果 */
	private void initAnimation() {
		MyAnimListener myAnimListenner = new MyAnimListener();
		this.open = new AlphaAnimation(0.0F, 1.0F);
		this.open.setDuration(500L);
		this.open.setAnimationListener(myAnimListenner);
		this.close = new AlphaAnimation(1.0F, 0.0F);
		this.close.setDuration(500L);
		this.close.setAnimationListener(myAnimListenner);
		this.close.setFillAfter(true);
	}

	/**
	 * 动画监听
	 * @author Alkaid
	 *
	 */
	private class MyAnimListener implements Animation.AnimationListener {
		@Override
		public void onAnimationEnd(Animation anim) {
			if (anim == close) {
				new GetLessonTask().execute();
				return;
			}
		}
		public void onAnimationRepeat(Animation anim) {}
		public void onAnimationStart(Animation anim) {}
	}

	private class Mp3Player extends Player {
		public Mp3Player() {
			super(sbAudio);
		}
		@Override
		protected void onSetedDataSource(MediaPlayer mp) {
			super.onSetedDataSource(mp);
			if (TextUtils.isEmpty(finalTimeJ[currentView]))
				tvTotalTime
						.setText(ViewUtil.formatTimeInmmss(mp.getDuration()));
			else
				tvTotalTime.setText(finalTimeJ[currentView]);
			tvCurrentTime.setText(currentTimeJ[currentView]);
		}
		@Override
		protected void onUpdateProgress(MediaPlayer mp) {
			super.onUpdateProgress(mp);
			tvCurrentTime.setText(ViewUtil.formatTimeInmmss(mp
					.getCurrentPosition()));
			tvCurrentTime.invalidate();
		}
		@Override
		protected void onCompletion(MediaPlayer mp) {
			super.onCompletion(mp);
			showMediaButtons();
		}
	}
	
	private void initSettingDialog(){
		SettingDialog settingDialog=new SettingDialog(this, setting){
			@Override
			protected void onSettingChanged(Setting setting) {
				for(int i=0;i<articlesNum;i++){
					setTextProperty(tvArticles[i],setting);
				}
			}
		};
		settingDialog.show();
	}
}
