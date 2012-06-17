package com.alkaid.ojpl.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.view.ui.SoundView;
import com.alkaid.ojpl.view.ui.SoundView.OnVolumeChangedListener;
import com.alkaid.ojpl.view.ui.VideoView;
import com.alkaid.ojpl.view.ui.VideoView.OnSizeChangeListener;

public class VideoPlayerActivity extends Activity {
	
	private final static String TAG = "VideoPlayerActivity";
	private boolean isOnline = false; 
	private boolean isChangedVideo = false;
	private final static int PROGRESS_CHANGED = 0;
    private final static int HIDE_CONTROLER = 1;
    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
	
//	public static LinkedList<MovieInfo> playList = new LinkedList<MovieInfo>();
	/*public class MovieInfo{
		String displayName;  
		String path;
	}*/
	private Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private static int position ;
	private int playedTime;
	
	
	private VideoView vv = null;
	private SeekBar seekBar = null;  
	private TextView durationTextView = null;
	private TextView playedTextView = null;
	private GestureDetector mGestureDetector = null;
	private AudioManager mAudioManager = null;  
	
	private int maxVolume = 0;
	private int currentVolume = 0;  
	
//	private ImageButton bn1 = null;
	private ImageButton btnPre = null;
	private ImageButton btnPlay = null;
	private ImageButton btnNext = null;
	private ImageButton btnSound = null;
	
	private View controlView = null;
	private PopupWindow controler = null;
	
	private SoundView mSoundView = null;
	private PopupWindow mSoundWindow = null;
	
	
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static int controlHeight = 0;  
	
	private final static int TIME = 6868;  
	
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private boolean isFullScreen = false;
	private boolean isSilent = false;
	private boolean isSoundShow = false;
	private String uriString=null;
	
	private ProgressDialog prepareProgressDialog;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(Constants.bundleKey.videoIsOnline, isOnline);
		outState.putString(Constants.bundleKey.videoUri, uriString);
		super.onSaveInstanceState(outState);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        LinearLayout llMain=new LinearLayout(this);
        setContentView(llMain,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        //初始化播放源
        uriString=getIntent().getStringExtra(Constants.bundleKey.videoUri);
        isOnline=getIntent().getBooleanExtra(Constants.bundleKey.videoIsOnline,false);
        if(TextUtils.isEmpty(uriString)){
        	uriString=savedInstanceState.getString(Constants.bundleKey.videoUri);
        	isOnline=savedInstanceState.getBoolean(Constants.bundleKey.videoIsOnline);
        }
        //显示控制面板
        Looper.myQueue().addIdleHandler(new IdleHandler(){
			@Override
			public boolean queueIdle() {
				if(controler != null && vv.isShown()){
					controler.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
					//controler.update(screenWidth, controlHeight);
					controler.update(0, 0, screenWidth, controlHeight);
				}
				return false;  
			}
        });
        //初始化控制面板
        controlView = getLayoutInflater().inflate(R.layout.controler, null);
        controler = new PopupWindow(controlView);
        durationTextView = (TextView) controlView.findViewById(R.id.duration);
        playedTextView = (TextView) controlView.findViewById(R.id.has_played);
        mSoundView = new SoundView(this);
        mSoundView.setOnVolumeChangeListener(new OnVolumeChangedListener(){
			@Override
			public void setYourVolume(int index) {
				cancelDelayHide();
				updateVolume(index);
				hideControllerDelay();
			}
        });
        mSoundWindow = new PopupWindow(mSoundView);
        position = -1;	//当前播放视频的position
//        bn1 = (ImageButton) controlView.findViewById(R.id.button1);
        btnPre = (ImageButton) controlView.findViewById(R.id.btnPre);
        btnPlay = (ImageButton) controlView.findViewById(R.id.btnPlay);
        btnNext = (ImageButton) controlView.findViewById(R.id.btnNext);
        btnSound = (ImageButton) controlView.findViewById(R.id.btnSound);
        //初始化VideoView
        vv = new VideoView(this);
        llMain.setGravity(Gravity.CENTER);
        llMain.addView(vv,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        vv.setOnErrorListener(new OnErrorListener(){
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				vv.stopPlayback();
				isOnline = false;
				new AlertDialog.Builder(VideoPlayerActivity.this)
                .setTitle("对不起")
                .setMessage("您所播的视频格式不正确，播放已停止。")
                .setPositiveButton("知道了",
                        new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								vv.stopPlayback();
							}
                        })
                .setCancelable(false)
                .show();
				VideoPlayerActivity.this.finish();
				return false;
			}
        });
        
        btnPlay.setImageResource(R.drawable.sel_vpause);
        vv.stopPlayback();
        if(isOnline){
        	vv.setVideoURI(Uri.parse(uriString));
        }else{
        	vv.setVideoPath(uriString);
        }
        //初始化播放列表
        /*getVideoFile(playList, new File("/sdcard/"));
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
        	Cursor cursor = getContentResolver().query(videoListUri, new String[]{"_display_name","_data"}, null, null, null);
            int n = cursor.getCount();
            cursor.moveToFirst();
            LinkedList<MovieInfo> playList2 = new LinkedList<MovieInfo>();
            for(int i = 0 ; i != n ; ++i){
            	MovieInfo mInfo = new MovieInfo();
            	mInfo.displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
            	mInfo.path = cursor.getString(cursor.getColumnIndex("_data"));
            	playList2.add(mInfo);
            	cursor.moveToNext();
            }
            if(playList2.size() > playList.size()){
            	playList = playList2;
            }
        }*/
        
        vv.setMySizeChangeLinstener(new OnSizeChangeListener(){
			@Override
			public void onSizeChanged() {
				setVideoScale(SCREEN_DEFAULT);
			}
        	
        });
              
//        bn1.setAlpha(0xBB);
        btnPre.setAlpha(0xBB);  
        btnPlay.setAlpha(0xBB);
        btnNext.setAlpha(0xBB);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        btnSound.setAlpha(findAlphaFromSound());
        //播放列表键
        /*bn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(VideoPlayerActivity.this, VideoChooseActivity.class);
				VideoPlayerActivity.this.startActivityForResult(intent, 0);
				cancelDelayHide();
			}
        });*/
        
        btnNext.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				/*int n = playList.size();
				isOnline = false;
				if(++position < n){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					VideoPlayerActivity.this.finish();
				}*/
			}
        });
        
        btnPlay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				cancelDelayHide();
				if(isPaused){
					vv.start();
					btnPlay.setImageResource(R.drawable.sel_vpause);
					hideControllerDelay();
				}else{
					vv.pause();
					btnPlay.setImageResource(R.drawable.sel_vplay);
				}
				isPaused = !isPaused;
			}
        });
        
        btnPre.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				/*isOnline = false;
				if(--position>=0){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					VideoPlayerActivity.this.finish();
				}*/
			}
        });
        
        btnSound.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				cancelDelayHide();
				if(isSoundShow){
					mSoundWindow.dismiss();
				}else{
					if(mSoundWindow.isShowing()){
						mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
					}else{
						mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
						mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
					}
				}
				isSoundShow = !isSoundShow;
				hideControllerDelay();
			}   
       });
        
        btnSound.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				if(isSilent){
					btnSound.setImageResource(R.drawable.sel_vsound);
				}else{
					btnSound.setImageResource(R.drawable.sounddisable);
				}
				isSilent = !isSilent;
				updateVolume(currentVolume);
				cancelDelayHide();
				hideControllerDelay();
				return true;
			}
        });
        
        seekBar = (SeekBar) controlView.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
					if(fromUser){
//						if(!isOnline){
							vv.seekTo(progress);
//						}
					}
				}
				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
					myHandler.removeMessages(HIDE_CONTROLER);
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
				}
        	});
        
        getScreenSize();
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if(isFullScreen){
					setVideoScale(SCREEN_DEFAULT);
				}else{
					setVideoScale(SCREEN_FULL);
				}
				isFullScreen = !isFullScreen;
				Log.d(TAG, "onDoubleTap");
				
				if(isControllerShow){
					showController();
				}
				return true;
			}
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}
				//return super.onSingleTapConfirmed(e);
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if(isPaused){
					vv.start();
					btnPlay.setImageResource(R.drawable.sel_vpause);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					vv.pause();
					btnPlay.setImageResource(R.drawable.sel_vplay);
					cancelDelayHide();
					showController();
				}
				isPaused = !isPaused;
				//super.onLongPress(e);
			}	
        });
        
        vv.setOnPrepareListener(new VideoView.OnPrepareListener() {
			@Override
			public void onPrepare() {
				prepareProgressDialog=ProgressDialog.show(VideoPlayerActivity.this, null, "正在载入...",true,true,new DialogInterface.OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
			}
		});
        // vv.setVideoPath("http://202.108.16.171/cctv/video/A7/E8/69/27/A7E86927D2BF4D2FA63471D1C5F97D36/gphone/480_320/200/0.mp4");
        vv.setOnPreparedListener(new OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer arg0) {
					if(null!=prepareProgressDialog)
						prepareProgressDialog.dismiss();
					setVideoScale(SCREEN_DEFAULT);
					isFullScreen = false; 
					if(isControllerShow){
						showController();  
					}
					int i = vv.getDuration();
					Log.d("onCompletion", ""+i);
					seekBar.setMax(i);
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					durationTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));
					/*controler.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
					controler.update(screenWidth, controlHeight);
					myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);*/
					vv.start();  
					btnPlay.setImageResource(R.drawable.sel_vpause);
					hideControllerDelay();
					myHandler.sendEmptyMessage(PROGRESS_CHANGED);
				}	
	        });
        
        vv.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer arg0) {
					/*int n = playList.size();
					isOnline = false;
					if(++position < n){
						vv.setVideoPath(playList.get(position).path);
					}else{
						vv.stopPlayback();
						VideoPlayerActivity.this.finish();
					}*/
				}
        	});
    }

   /* @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode==0&&resultCode==Activity.RESULT_OK){
    		vv.stopPlayback();
    		int result = data.getIntExtra("CHOOSE", -1);
    		Log.d("RESULT", ""+result);
    		if(result!=-1){
    			isOnline = false;
    			isChangedVideo = true;
    			vv.setVideoPath(playList.get(result).path);
    			position = result;
    		}else{
    			String url = data.getStringExtra("CHOOSE_URL");
    			if(url != null){
    				vv.setVideoPath(url);
    				isOnline = true;
    				isChangedVideo = true;
    			}
    		}
    		return ;
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}*/

    Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
				case PROGRESS_CHANGED:
					int i = vv.getCurrentPosition();
					seekBar.setProgress(i);
					if(isOnline){
						int j = vv.getBufferPercentage();
						seekBar.setSecondaryProgress(j * seekBar.getMax() / 100);
					}else{
						seekBar.setSecondaryProgress(0);
					}
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					playedTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));
					sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
					break;
				case HIDE_CONTROLER:
					hideController();
					break;
			}
			super.handleMessage(msg);
		}	
    };

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = mGestureDetector.onTouchEvent(event);
		if(!result){
			if(event.getAction()==MotionEvent.ACTION_UP){
				/*if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}*/
			}
			result = super.onTouchEvent(event);
		}
		return result;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		getScreenSize();
		if(isControllerShow){
			cancelDelayHide();
			hideController();
			showController();
			hideControllerDelay();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		playedTime = vv.getCurrentPosition();
		vv.pause();
		btnPlay.setImageResource(R.drawable.sel_vplay);
		super.onPause();   
	}

	@Override
	protected void onResume() {
		if(!isChangedVideo){
			vv.seekTo(playedTime);
			vv.start();  
		}else{
			isChangedVideo = false;
		}
		//if(vv.getVideoHeight()!=0){
		if(vv.isPlaying()){
			btnPlay.setImageResource(R.drawable.sel_vpause);
			hideControllerDelay();
		}
		Log.d("REQUEST", "NEW AD !");
		if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if(controler.isShowing()){
			controler.dismiss();
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
		}
		myHandler.removeMessages(PROGRESS_CHANGED);
		myHandler.removeMessages(HIDE_CONTROLER);
		if(vv.isPlaying()){
			vv.stopPlayback();
		}
//		playList.clear();
		super.onDestroy();
	}     

	private void getScreenSize()
	{
		Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
//        controlHeight = screenHeight/4;
        DisplayMetrics dm=new DisplayMetrics();
		display.getMetrics(dm);
        controlHeight=(int) (120*dm.density);
	}
	
	private void showController(){
		controler.update(0,0,screenWidth, controlHeight);
		isControllerShow = true;
	}
	private void hideController(){
		if(controler.isShowing()){
			controler.update(0,0,0, 0);
			isControllerShow = false;
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
			isSoundShow = false;
		}
	}
	/** 延迟  {@link #TIME时间隐藏控制面板}*/
	private void hideControllerDelay(){
		myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	/** 取消延迟隐藏面板*/
	private void cancelDelayHide(){
		myHandler.removeMessages(HIDE_CONTROLER);
	}

    private void setVideoScale(int flag){
    	switch(flag){
    		case SCREEN_FULL:
    			Log.d(TAG, "screenWidth: "+screenWidth+" screenHeight: "+screenHeight);
    			vv.setVideoScale(screenWidth, screenHeight);
    			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    			break;
    		case SCREEN_DEFAULT:
    			int videoWidth = vv.getVideoWidth();
    			int videoHeight = vv.getVideoHeight();
    			int mWidth = screenWidth;
    			int mHeight = screenHeight - 25;
    			if (videoWidth > 0 && videoHeight > 0) {
    	            if ( videoWidth * mHeight  > mWidth * videoHeight ) {
    	                //Log.i("@@@", "image too tall, correcting");
    	            	mHeight = mWidth * videoHeight / videoWidth;
    	            } else if ( videoWidth * mHeight  < mWidth * videoHeight ) {
    	                //Log.i("@@@", "image too wide, correcting");
    	            	mWidth = mHeight * videoWidth / videoHeight;
    	            } else {
    	            }
    	        }
    			vv.setVideoScale(mWidth, mHeight);
    			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    			break;
    	}
    }

    private int findAlphaFromSound(){
    	if(mAudioManager!=null){
    		//int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    		int alpha = currentVolume * (0xCC-0x55) / maxVolume + 0x55;
    		return alpha;
    	}else{
    		return 0xCC;
    	}
    }

    private void updateVolume(int index){
    	if(mAudioManager!=null){
    		if(isSilent){
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    		}else{
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    		}
    		currentVolume = index;
    		btnSound.setAlpha(findAlphaFromSound());
    	}
    }

    /*private void getVideoFile(final LinkedList<MovieInfo> list,File file){
    	file.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				int i = name.indexOf('.');
				if(i != -1){
					name = name.substring(i);
					if(name.equalsIgnoreCase(".mp4")||name.equalsIgnoreCase(".3gp")){
						MovieInfo mi = new MovieInfo();
						mi.displayName = file.getName();
						mi.path = file.getAbsolutePath();
						list.add(mi);
						return true;
					}
				}else if(file.isDirectory()){
					getVideoFile(list, file);
				}
				return false;
			}
    	});
    }*/
}