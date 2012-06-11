package com.alkaid.ojpl.view.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;

import com.alkaid.ojpl.common.LogUtil;

public class Player {
	private boolean firsttime;
	private Handler handler;
	public MediaPlayer mediaplayer;
	private SeekBar mySeekBar;
	private Timer timer;
	private TimerTask timetask;

	public Player(SeekBar seekBar) {
		this.mySeekBar = seekBar;
		//操作
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int progress;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mediaplayer.seekTo(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (mediaplayer == null)
					return;
				int i = mediaplayer.getDuration() * progress;
				this.progress = i / seekBar.getMax();
			}
		});
		this.timer = new Timer();
		this.firsttime = true;
		//更新进度条
		this.timetask = new TimerTask() {
			@Override
			public void run() {
				if (Player.this.mediaplayer == null)
					return;
				if (!Player.this.mediaplayer.isPlaying())
					return;
//				if (Player.this.list.isPressed())
//					return;
				Player.this.handler.sendEmptyMessage(1);
			}
		};
		this.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mediaplayer == null)
					return;
				int currentPosition = mediaplayer.getCurrentPosition();
				onUpdateProgress(mediaplayer);
				int duration = mediaplayer.getDuration();
				if (duration <= 0)
					return;
				mySeekBar.setProgress(currentPosition * mySeekBar.getMax() / duration);
			}
		};
		initMediaPlayer();
	}

	/**
	 * 初始化
	 */
	private void initMediaPlayer() {
		try {
			this.mediaplayer = new MediaPlayer();
			this.mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Player.this.onCompletion(mp);
				}
			});
			this.mediaplayer.setScreenOnWhilePlaying(true);
			return;
		} catch (Exception e) {
			LogUtil.e("Error", "init media error");
		}
		onInitedMediaPlayer(mediaplayer);
	}

	/**
	 * 暂停
	 */
	public void pause() {
		if (this.mediaplayer == null)
			return;
		this.mediaplayer.pause();
	}

	/**
	 * 播放
	 */
	public void play() {
		onPreparePlay(mediaplayer);
		if (this.firsttime) {
			timer.schedule(timetask, 0L, 1000L);
			this.firsttime = false;
		}
		this.mediaplayer.start();
	}

	/**
	 * 设置音频地址 此方法将会重置mediaPlayer
	 * @param file
	 */
	public void setDataSource(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			this.mediaplayer.reset();
			mediaplayer.setDataSource(fis.getFD());
			this.mediaplayer.prepare();
			this.firsttime = true;
		} catch (IllegalArgumentException e) {
			LogUtil.e(e);
		} catch (IllegalStateException e) {
			LogUtil.e(e);
		} catch (IOException e) {
			LogUtil.e(e);
		}
		onSetedDataSource(mediaplayer);
	}

	/**
	 * 设置音频地址 此方法将会重置mediaPlayer
	 * @param fileName
	 */
	public void setDataSource(String fileName) {
		setDataSource(new File(fileName));
	}

	/**停止 调用该方法后应该销毁该对象*/
	public void stopmusic() {
		if (this.mediaplayer == null)
			return;
		this.mediaplayer.stop();
		this.timer.cancel();
		this.mediaplayer.release();
		this.mediaplayer = null;
	}
	
	/**
	 * 初始化mediaPlayer后回调
	 * @param mp mediaPlayer
	 */
	protected void onInitedMediaPlayer(MediaPlayer mp){}
	/** 
	 * 在更新进度时回调此方法
	 * @param mp mediaPlayer
	 */
	protected void onUpdateProgress(MediaPlayer mp) {}
	/**
	 * mediaPlayer播放完成时回调此方法
	 * @param mp mediaPlayer
	 */
	protected void onCompletion(MediaPlayer mp){}
	/**
	 * Player.play()时回调此方法
	 * @param mp mediaPlayer
	 */
	protected void onPreparePlay(MediaPlayer mp){}
	/**
	 * 绑定音频地址后回调  即{@link #setDataSource(String)}方法完成后回调
	 * @param mp
	 */
	protected void onSetedDataSource(MediaPlayer mp){}
}
