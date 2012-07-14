package com.alkaid.ojpl.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.DownLoader;
import com.alkaid.ojpl.common.IOUtil;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.common.NetWorkUtil;
import com.alkaid.ojpl.common.SNSShare;
import com.alkaid.ojpl.common.UnZipUtil;
import com.alkaid.ojpl.data.BookItemOperator;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.view.ad.BannerAdManager;
import com.alkaid.ojpl.view.ad.PointsManager;
import com.alkaid.ojpl.view.ui.CustAlertDialog;
import com.alkaid.ojpl.view.ui.OperateDialog;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

public class BookShelf extends BaseActivity{
	
	private List<BookItem> bookItems;
	private Map<Integer, ViewGroup> bookViews;
	private Map<Integer, DownLoader> downloaders;
	public static String SUBSTRING_WORD = "=";
	boolean isExit = false;
	private PointsManager pointsManager;
	private Map<Integer, Integer> downloadStates;
	
	private final int msg_what_exit=1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookshelf);
		//友盟自动更新
		UmengUpdateAgent.update(this);
		Button btnShare=(Button) findViewById(R.id.btnShare);
		Button btnFeedback=(Button) findViewById(R.id.btnFeedback);
		btnShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//umeng分享组件分享
				InputStream is = context.getResources().openRawResource(R.drawable.share_pic_s);
	        	SNSShare.share(context, is);
	        	//客户端分享
//				Intent intent = new Intent(Intent.ACTION_SEND);
//				intent.setType("text/plain");
//				intent.putExtra(Intent.EXTRA_SUBJECT, "分享到");
//				intent.putExtra(Intent.EXTRA_TEXT,
//						"I would like to share this with you...");
//				startActivityForResult(Intent.createChooser(intent, getTitle()),1);
			}
		});
		btnFeedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UMFeedbackService.openUmengFeedbackSDK(context);
			}
		});
		//加载数据
		new InitTask().execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		pointsManager=new PointsManager(context);
		//加载广告条
		new BannerAdManager(context).creatAd();
	}
	
	@Override
	protected void onDestroy() {
		pointsManager.finalize();
		super.onDestroy();
	}
	
	/** 初始化任务 初始化应用全局信息以及书本信息*/
	private class InitTask extends AsyncTask<Void, Integer, Integer>{
		@Override
		protected Integer doInBackground(Void... params) {
			//初始化bookItem
			BookItemOperator bio = new BookItemOperator();
			bookItems = bio.getAllBookItems(BookShelf.this);
			bookViews = new HashMap<Integer, ViewGroup>();
			initDownloaders();
			initDownloadStates();
			return null;
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			showShelf();
		}
	}
	
	/**
	 * 初始化适配器 显示书架
	 */
	private void showShelf(){
		ListView shelfList = (ListView) findViewById(R.id.shelf_list_id);
		ShelfAdapter shelfAdpater = new ShelfAdapter();
		shelfList.setAdapter(shelfAdpater);
	}
	
	/** 初始化下载器*/
	private void initDownloaders(){
		downloaders =new HashMap<Integer, DownLoader>();
		DownLoader d=null;
		for(int i=0;i<bookItems.size();i++){
			final BookItem bookItem=bookItems.get(i);
			//判断书的类型，说明的书要单独处理,五十音图的书要单独处理
			if(bookItem.getId().equals(Constants.FIFTYMAPID)||bookItem.getId().equals(Constants.INTRODUCTION)){
				//五十音加载时的操作
				d = new DownLoader(i,null,null,handle,context);
			}else{
				//初始化下载地址
				String downAdd = bookItem.getDownloadAdd();
				//初始化下载到的地址
				String path = Constants.PATH_RES +"/"+
						downAdd.substring(downAdd.lastIndexOf(SUBSTRING_WORD)+1);
				//初始化下载器
				d = new DownLoader(i,downAdd,path,handle,context){
					@Override
					protected void onDownloadBegin(long filesize) {
						super.onDownloadBegin(filesize);
						if(filesize>0){
							SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPreference.bookConfig.name, Context.MODE_PRIVATE);
							String key=bookItem.getId()+Constants.sharedPreference.bookConfig.size_suffix;
							sharedPreferences.edit().putLong(key, filesize).commit();
						}
					}
				};
			}
			downloaders.put(i, d);
		}
	}
	
	/** 初始化下载状态*/
	private void initDownloadStates(){
		downloadStates=new HashMap<Integer, Integer>();
		for(int i=0;i<bookItems.size();i++){
			final BookItem bookItem=bookItems.get(i);
			//判断书的类型，五十音图的书要单独处理，说明的书要单独处理
			if(bookItem.getId().equals(Constants.FIFTYMAPID)||bookItem.getId().equals(Constants.INTRODUCTION)){
				downloadStates.put(i, DownLoader.TASK_COMPLETE);
			}else{
				//初始化下载图标的状态
				SharedPreferences sharedPreferences = getSharedPreferences(Constants.sharedPreference.bookConfig.name, Context.MODE_PRIVATE);
				//用于判断本地文件和文件长度是否相等传递初始化状态
				String key=bookItem.getId()+Constants.sharedPreference.bookConfig.size_suffix;
				DownLoader d=downloaders.get(i);
				d.setFileSize(sharedPreferences.getLong(key, 0));
				long fileSize = d.getFileSize();
				long localFileLength = d.getLocalFileLength();
				int status=DownLoader.DOWN_NONE;
				if(localFileLength<fileSize&&localFileLength!=0){
					status=DownLoader.DOWN_PAUSE;
				}else if(localFileLength == fileSize&&localFileLength!=0){
	//				status=DownLoader.ZIP_BEGIN;	//TODO 考虑未解压情况处理
					status=DownLoader.DOWN_PAUSE;
				}else if(localFileLength==0){
					String zipDir = Constants.PATH_RES+"/"+bookItem.getDownloadAdd().substring(bookItem.getDownloadAdd().lastIndexOf(SUBSTRING_WORD)+1,bookItem.getDownloadAdd().lastIndexOf("."));
					File f = new File(zipDir);
					if(f.exists()){
						status = DownLoader.TASK_COMPLETE;
					}else{
						status=DownLoader.DOWN_NONE;
					}
				}
				downloadStates.put(i, status);
			}
		}
	}
		
	//书架所用到的适配器
	class ShelfAdapter extends BaseAdapter {
		LayoutInflater inflater;
		/** 每行几个视图 */
		private static final int NUM_PER_LINE=3;

		public ShelfAdapter(){
			inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}
		
		@Override
		public int getCount() {
			if(bookItems.size()<=4*NUM_PER_LINE){
				return 4;
			}
			return (NUM_PER_LINE-bookItems.size()%NUM_PER_LINE+bookItems.size())/NUM_PER_LINE;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		private class ViewHolder{
			BookViewHolder[] bookViewHolders=new BookViewHolder[NUM_PER_LINE];
			LinearLayout floor;
			public ViewHolder(){
				for(int i=0;i<bookViewHolders.length;i++){
					bookViewHolders[i]=new BookViewHolder();
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder=null;
			if(null==convertView){
				viewHolder=new ViewHolder();
				RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.bookshelf_item, parent,false);
				//2.2以下不能用该构造方法  否则alighParentBottom无效
//				RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.bookshelf_item, null);
				convertView=rl;
				for(int i = 0;i<NUM_PER_LINE;i++){
					ViewGroup bookView=(ViewGroup)inflater.inflate(R.layout.book_item, null);
					ImageView bookImage=(ImageView) bookView.findViewById(R.id.book_show_id);
					LinearLayout downState = (LinearLayout) bookView.findViewById(R.id.download_tag_id);
					TextView percent = (TextView) downState.findViewById(R.id.download_percent_id);
					ImageView downImage1 = (ImageView) downState.findViewById(R.id.down_image1_id);
					ImageView downImage2 = (ImageView) downState.findViewById(R.id.down_image2_id);
					ProgressBar process = (ProgressBar) bookView.findViewById(R.id.zipLoad_id);
					
					viewHolder.floor = (LinearLayout)convertView.findViewById(R.id.shelf_center_id);
					viewHolder.bookViewHolders[i].bookView=bookView;
					viewHolder.bookViewHolders[i].bookImage=bookImage;
					viewHolder.bookViewHolders[i].downState=downState;
					viewHolder.bookViewHolders[i].percent=percent;
					viewHolder.bookViewHolders[i].downImage1=downImage1;
					viewHolder.bookViewHolders[i].downImage2=downImage2;
					viewHolder.bookViewHolders[i].progresse=process;
					
					LinearLayout.LayoutParams bookLayParams=new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
					bookLayParams.weight=1;
					bookLayParams.bottomMargin=(int) (26*global.dm.density); //26dp
					viewHolder.floor.addView(bookView,bookLayParams);
				}
				convertView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder) convertView.getTag();
			}
			for(int i = 0;i<NUM_PER_LINE;i++){
				int bookIndex=position*NUM_PER_LINE+i;
				viewHolder.bookViewHolders[i].bookView.setTag(bookIndex);
				bookViews.put(bookIndex, viewHolder.bookViewHolders[i].bookView);
			}
			refreshBookView(position,viewHolder);
			return convertView;
		}
		
		private void refreshBookView(int position,ViewHolder viewHolder){
			//每行三本书
			for( int i = 0;i<NUM_PER_LINE;i++){
				int bookIndex=position*NUM_PER_LINE+i;
				//超过数据长度则返回不可操作不可见的空view
				if(bookIndex>bookItems.size()-1){
					viewHolder.bookViewHolders[i].bookView.setEnabled(false);
					viewHolder.bookViewHolders[i].bookView.setVisibility(View.INVISIBLE);
					continue;
				}
				viewHolder.bookViewHolders[i].bookView.setEnabled(true);
				viewHolder.bookViewHolders[i].bookView.setVisibility(View.VISIBLE);
				BookItem bookItem=bookItems.get(bookIndex);
				//初始化书封面图片地址					
				String imageAdd = bookItem.getImageAdd();
				viewHolder.bookViewHolders[i].bookImage.setImageBitmap(loadImage(imageAdd));
				updateBookState(downloadStates.get(bookIndex), bookIndex,viewHolder.bookViewHolders[i],false);
				viewHolder.bookViewHolders[i].bookView.setOnClickListener(new BookOnClickListenner(bookIndex));
				viewHolder.bookViewHolders[i].bookView.setOnLongClickListener(new BookOnLongClickListenner(bookIndex));
			}
		}
		
		// 图书点击下载状态的变化
		private class BookOnClickListenner implements View.OnClickListener{
			private int index;
			public BookOnClickListenner(int index){
				this.index=index;
			}
			@Override
			public void onClick(View v) {
				// 测试解压
				/*
				 * if(i==1){ Message
				 * msg=handle.obtainMessage(DownLoader.ZIP_BEGIN);
				 * msg.obj=i; handle.sendMessage(msg); return; }
				 */
				// 判断SD卡操作，错误返回提示
				if (!IOUtil.checkSDCard()) {
					Toast.makeText(context, Constants.SDERROR,
							Toast.LENGTH_SHORT).show();
					return;
				}
				// 获取当前选取书的下载器
				final DownLoader downloader = downloaders.get(index);
				// 获取本书的对象
				final BookItem bookItem = bookItems.get(index);
				// 五十音图不用下载 直接进入
				if (bookItem.getId().equals(Constants.FIFTYMAPID)) {
					Intent intent = new Intent(context,
							FiftyMap.class);
					startActivity(intent);
					return;
				}
				//说明不用下载 直接进入
				if (bookItem.getId().equals(Constants.INTRODUCTION)) {
					Intent intent=new Intent(context, LessonList.class);
//					intent.putExtra(Constants.bundleKey.bookItem, bookItem);
					global.putData(Constants.bundleKey.bookItem,bookItem);
					startActivity(intent);
					return;
				}
				// 判断是否是下载完的文件
				String downAdd = bookItem.getDownloadAdd();
				String zipDir = Constants.PATH_RES
						+ "/"
						+ downAdd.substring(
								downAdd.lastIndexOf(SUBSTRING_WORD) + 1,
								downAdd.lastIndexOf("."));
				File f = new File(zipDir);
				// 书本下载完成点击所做的操作
				if (f.exists()&&downloader.getLocalFileLength()==0) {
					Intent intent=new Intent(context, LessonList.class);
//					intent.putExtra(Constants.bundleKey.bookItem, bookItem);
					global.putData(Constants.bundleKey.bookItem,bookItem);
					startActivity(intent);
					return;
				}
				//开始下载或暂停
				if(!downloader.isStop()){
					// 做暂停操作
					downloader.pause();
					return;
				}
				// 做下载操作
				if (downloader.isStop()) {
					downloadBook(downloader, bookItem);
					return;
				}
			}
		}
		/** 长按菜单*/
		private class BookOnLongClickListenner implements View.OnLongClickListener{
			private int index;
			public BookOnLongClickListenner(int index){
				this.index=index;
			}
			@Override
			public boolean onLongClick(View v) {
				final DownLoader downloader = downloaders.get(index);
				final BookItem bookItem = bookItems.get(index);
				if(!bookItem.getId().equals(Constants.FIFTYMAPID)){
					final OperateDialog dialog = new OperateDialog(context, index,handle);
					dialog.setOnDownloadClickListenner(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							downloadBook(downloader, bookItem);
							dialog.dismiss();
						}
					});
					dialog.show();
				}
				return true;
			}
		}
						
		//读取assets目录下的图片
		private Bitmap loadImage(String path){
			AssetManager am = context.getAssets();
			InputStream is = null;
			Bitmap image = null;
			try {
				is = am.open(path);
				image = BitmapFactory.decodeStream(is);
			} catch (IOException e) {
				LogUtil.e("file:"+path+" open failed!");
				LogUtil.e(e);
			}finally{
				try {
					is.close();
				} catch (IOException e) {
					LogUtil.e(e);
				}
			}				
			return image;			
		}		
	}
	
	//定义一个Handle用于处理线程和UI的通讯
	//定义消息的what值0表示暂停状态,1表示进度条进行更新,2表示完成下载所做的操作,3表示刚开始下载,-1错误处理
	private Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case msg_what_exit:
				isExit = false;
				return;
			case Constants.msgWhat.error:
				String tip = msg.getData().getString(Constants.bundleKey.errorMsg);
				Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
				break;
			case Constants.msgWhat.downstate_changed:
				int index=msg.arg1;
				int downState=msg.arg2;
				downloadStates.put(index, downState);
				updateBookState(downState, index);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	/**
	 * 根据下载状态更新ui
	 * @param status  下载状态 包括  {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param bookIndex  书本索引
	 */
	private void updateBookState(int status, final int bookIndex) {
		BookViewHolder bookViewHolder=new BookViewHolder();
		ViewGroup bookView = bookViews.get(bookIndex);
		//若bookView的tag与书本索引不等 说明该bookView不是要更新的view 这里是为了防止回收再利用的bookView接收到下载线程的更新请求
		if(Integer.parseInt(bookView.getTag().toString())!=bookIndex){
			return;
		}
		bookViewHolder.bookView=bookView;
		bookViewHolder.downState=(LinearLayout)bookView.findViewById(R.id.download_tag_id);
		bookViewHolder.progresse=(ProgressBar) bookView.findViewById(R.id.zipLoad_id);
		LinearLayout downState=bookViewHolder.downState;
		bookViewHolder.percent=(TextView) downState.findViewById(R.id.download_percent_id);
		bookViewHolder.downImage1=(ImageView) downState.findViewById(R.id.down_image1_id);
		bookViewHolder.downImage2=(ImageView) downState.findViewById(R.id.down_image2_id);
		updateBookState(status, bookIndex, bookViewHolder,true);
	}
	/**
	 * 根据下载状态更新ui  这个方法用于有bookViewHolder缓存的更新
	 * @param status  下载状态 包括 {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param bookIndex  书本索引
	 * @param bookViewHolder bookView视图缓存
	 * @param operation 是否进行非UI的操作,具体指下载解压等行为(实际上该参数是专为ListView更新UI用的)
	 */
	private void updateBookState(int status, final int bookIndex,BookViewHolder bookViewHolder,boolean operation) {
		final DownLoader downloader = downloaders.get(bookIndex);
		LinearLayout downState = bookViewHolder.downState;
		TextView percent = bookViewHolder.percent;
		ImageView image1 = bookViewHolder.downImage1;
		ImageView image2 = bookViewHolder.downImage2;
		ProgressBar progress = bookViewHolder.progresse;
		
		switch(status){
		case DownLoader.DOWN_PAUSE:
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(Color.WHITE);
			downState.getBackground().setAlpha(100);
			image1.setVisibility(View.VISIBLE);
			image2.setVisibility(View.GONE);
			image1.setBackgroundResource(R.drawable.continue1);
			image1.clearAnimation();
			percent.setVisibility(View.GONE);
			progress.setVisibility(View.GONE);
			break;
		case DownLoader.DOWN_BEGIN:
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(Color.WHITE);
			downState.getBackground().setAlpha(100);
			image1.setVisibility(View.VISIBLE);
			image2.setVisibility(View.VISIBLE);
			image1.setBackgroundResource(R.drawable.downing);
			image2.setBackgroundResource(R.drawable.downing1);
			percent.setVisibility(View.VISIBLE);
			percent.setText("0%");
			progress.setVisibility(View.GONE);
			downAnimation(image1, image2,bookIndex);
			break;
		case DownLoader.DOWN_LOADING:
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(Color.WHITE);
			downState.getBackground().setAlpha(100);
			image1.setVisibility(View.VISIBLE);
			image2.setVisibility(View.VISIBLE);
			image1.setBackgroundResource(R.drawable.downing);
			image2.setBackgroundResource(R.drawable.downing1);
			percent.setVisibility(View.VISIBLE);
			percent.setText("0%");
			progress.setVisibility(View.GONE);
			if(image1.getAnimation()==null){
				downAnimation(image1, image2,bookIndex);
			}
			if(downloader.getPercent()>=0&&downloader.getPercent()<=100){
				LogUtil.i("Downloading=======filesize="+downloader.getFileSize());
				percent.setText(downloader.getPercent()+"%");
			}else{
				Toast.makeText(context, Constants.DOWNERROR,Toast.LENGTH_LONG ).show();
				downloader.deleteFile();
				downloader.setCurSize(0);
				if(operation){
					if(IOUtil.checkSDCard()==false){
						Toast.makeText(context,Constants.SDERROR, Toast.LENGTH_LONG).show();
					}else if(NetWorkUtil.isOnline(context)==false){
						Toast.makeText(context, Constants.NETERROR, Toast.LENGTH_LONG).show();
					}else{
						new Thread(){								
							@Override
							public void run() {
								downloader.down();												
							};								
						}.start();
					}
				}
			}
			break;
		case DownLoader.TASK_COMPLETE:
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(0x0);
			image1.setVisibility(View.VISIBLE);
			image1.clearAnimation();
			image1.setBackgroundResource(R.drawable.complete);
			image2.setVisibility(View.GONE);
			percent.setVisibility(View.GONE);
			progress.setVisibility(View.GONE);
			break;
		case DownLoader.DOWN_NONE:	
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(0x0);
			image1.setVisibility(View.VISIBLE);
			image1.clearAnimation();
			image1.setBackgroundResource(R.drawable.add);
			image2.setVisibility(View.GONE);
			percent.setVisibility(View.GONE);
			progress.setVisibility(View.GONE);
			break;
		case DownLoader.DOWN_COMPLETE://开始解压
			downState.setVisibility(View.VISIBLE);
			downState.setBackgroundColor(Color.WHITE);
			downState.getBackground().setAlpha(100);
			image1.clearAnimation();
			image1.setVisibility(View.GONE);
			image2.setVisibility(View.GONE);
			percent.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			if(operation){
				new Thread(){
					@Override
					public void run() {
						UnZipUtil zipUtil = new UnZipUtil();
						String downAdd = downloader.getUrl();
						String zipPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf(SUBSTRING_WORD)+1);
						String destDir = Constants.PATH_RES+"/";
						try {
							zipUtil.readByApacheZipFile(zipPath, destDir);
						} catch (FileNotFoundException e) {
							LogUtil.e(e);
						} catch (ZipException e) {
							LogUtil.e(e);
						} catch (IOException e) {
							LogUtil.e(e);
						}
						Message msg=handle.obtainMessage(Constants.msgWhat.downstate_changed);
						msg.arg1=bookIndex;
						msg.arg2=DownLoader.TASK_COMPLETE;
						handle.sendMessage(msg);
					};
				}.start();
			}
			break;
		default:
			break;
		}
	}
	
	/**重写按键方法，用于退成程序*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if(keyCode == KeyEvent.KEYCODE_BACK){  
            if(!isExit){  
                isExit = true;  
                Toast.makeText(context,Constants.EXITREMIND, Toast.LENGTH_SHORT).show(); 
                handle.sendMessageDelayed(handle.obtainMessage(msg_what_exit),4000);
//                mHandle.sendEmptyMessageDelayed(0, 2000);
                }else{  
                finish();  
                System.exit(0);  
            }
        }
        return false;  
    }

	/**下载图标的动画*/
	public Animation downAnimation(ImageView image1,ImageView image2,int position){
		Animation animation = new TranslateAnimation(0, 0, 0, 12*global.dm.density);
		animation.setDuration(200);
		animation.setRepeatCount(-1);
		image1.setAnimation(animation);
		animation.startNow();
		return animation;
			
	}
		
	public List<BookItem> getBookItems() {
		return bookItems;
	}

	public Map<Integer, DownLoader> getDownloaders() {
		return downloaders;
	}


	/**
	 * 下载操作 包含一系列逻辑判断
	 * @param downloader
	 * @param bookItem
	 */
	private void downloadBook(final DownLoader downloader,
			final BookItem bookItem) {
		if (!NetWorkUtil.isOnline(context)) {
			Toast.makeText(context,
					Constants.NETERROR, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		//当网络不是wifi时提示是否下载
		if (NetWorkUtil.getAPNType(context) != 1) {
			String cancel="算了";
			String title="您所在的网络下载会产生流量费用，建议您通过WIFI下载后观看";
			String downText="继续";
			new CustAlertDialog.Builder(context)
				.setMessage(title)
				.setPositiveButton(cancel, new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton(downText, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						//该值表示是否需要积分 默认需要 true
						boolean isPointsEnough=pointsOperation(bookItem);
						if(!isPointsEnough) return;
						new Thread(){								
							@Override
							public void run() {
								downloader.down();												
							};								
						}.start();
					}
				})
				.create().show();
		} else {
			boolean isPointsEnough=pointsOperation(bookItem);
			if(!isPointsEnough) return;
			new Thread() {
				@Override
				public void run() {
					downloader.down();
				};
			}.start();
		}
	}
	
	/**
	 * 积分墙操作 
	 * @param bookItem 
	 * @return  积分是否充足
	 */
	private boolean pointsOperation(final BookItem bookItem) {
		//该值表示是否需要积分 默认需要 true
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				Constants.sharedPreference.bookConfig.name, Context.MODE_PRIVATE);
		String key=bookItem.getId()+Constants.sharedPreference.bookConfig.needPoints_suffix;
		if (sharedPreferences.getBoolean(key, true)) {
			// 积分
			boolean isPointsEnough = pointsManager.isPointsEnough();
			// 积分不够则不进行下载操作
			if (!isPointsEnough) {
				return false;
			} else {
				pointsManager.spendPoints(Constants.points.spendPerAction);
			}
			sharedPreferences.edit().putBoolean(key, false)
					.commit();
		}
		return true;
	}
	
	private static class BookViewHolder{
		/** 整本书的外容器*/
		ViewGroup bookView;
		/** 书本封面*/
		ImageView bookImage;
		/** 下载状态容器*/
		LinearLayout downState;
		/** 百分比*/
		TextView percent;
		/** 下载图标1*/
		ImageView downImage1;
		/** 下载图标2*/
		ImageView downImage2;
		/** 解压进度条*/
		ProgressBar progresse;
	}
}
