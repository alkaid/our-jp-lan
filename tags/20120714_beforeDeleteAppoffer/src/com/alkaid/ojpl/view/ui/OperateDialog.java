package com.alkaid.ojpl.view.ui;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.DownLoader;
import com.alkaid.ojpl.common.Global;
import com.alkaid.ojpl.common.IOUtil;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.view.BookShelf;
import com.alkaid.ojpl.view.LessonList;

/**
 * @author Lelouch
 * 这个类是获得处理长按事件生成的列表Dialog,其中删除的键提供了删除的提问Dialog
 *
 */
public class OperateDialog extends Dialog {
	public final static String TITLE="操作栏";
	public final static String DELETETITLE="你确定要删除课本么?";
	public final static String CONFIRMBTN="确定";
	public final static String CANCEL="取消";
	public static final String DOWNLOADTITLE="您所在的网络下载会产生流量费用，建议您通过WIFI下载后阅读";
	public static final String CONTINUE="继续";
	private TextView downTxt;
	private TextView pauseTxt;
	private TextView deleteTxt;
	private TextView readTxt;
	private TextView cancelTxt;
	private DownLoader down;	
	private Context context;
	private Handler handle;
	int tag;
	public OperateDialog(Context context,int tag,Handler handle) {
		this(context,tag,handle,R.style.Theme_Dialog);
	}
	public OperateDialog(Context context,int tag,Handler handle,int theme) {
		super(context,theme);
		this.context = context;
		this.tag = tag;
		this.handle = handle;
		initView();
	}

	public void initView(){
		this.setContentView(R.layout.operate_dialog);
		this.setTitle(TITLE);
		downTxt = (TextView) this.findViewById(R.id.down_dialog_id);
		pauseTxt = (TextView) this.findViewById(R.id.pause_dialog_id);
		deleteTxt = (TextView) this.findViewById(R.id.delete_dialog_id);
		readTxt = (TextView) this.findViewById(R.id.read_dialog_id);
		cancelTxt = (TextView) this.findViewById(R.id.cancel_dialog_id);
		final BookShelf bookShelf = (BookShelf) context;
		down = bookShelf.getDownloaders().get(tag);
		final BookItem bookItem =bookShelf.getBookItems().get(tag);
		final long fileSize = down.getFileSize();
		final long localFileLength = down.getLocalFileLength();
		final boolean isStop = down.isStop();
		String downAdd = down.getUrl();
		String decompressPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf("=")+1,downAdd.lastIndexOf("."));
		File decompressFile = new File(decompressPath);
		
		//解压后的文件存在且zip不存在则是阅读状态
		if(decompressFile.exists()&&localFileLength==0){
			readTxt.setEnabled(true);
			pauseTxt.setEnabled(false);
			downTxt.setEnabled(false);
		}else{
		//完全未下载过数据的情况
			//1.未下载 2.点击下载后刚联网马上暂停此时filesize==0 3.正常下载过程中
			if((fileSize==0||localFileLength<fileSize)&&isStop){
				readTxt.setEnabled(false);
				pauseTxt.setEnabled(false);
				downTxt.setEnabled(true);
			//1.点击下载后刚联网此时filesize==0  2.正常下载过程中暂停了
			}else if((fileSize==0||localFileLength<fileSize)&&!isStop){
				readTxt.setEnabled(false);
				pauseTxt.setEnabled(true);
				downTxt.setEnabled(false);
			//1.下载完成解压未完成
			}else if(localFileLength==fileSize&&localFileLength!=0&&isStop){
				readTxt.setEnabled(false);
				pauseTxt.setEnabled(false);
				downTxt.setEnabled(false);
				deleteTxt.setEnabled(true);
			}
		}	
		deleteTxt.setEnabled(localFileLength!=0||decompressFile.exists());
		cancelTxt.setEnabled(true);
		
		this.setCanceledOnTouchOutside(true);
		
		pauseTxt.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(localFileLength<fileSize&&isStop==false){
					down.pause();
				}
				OperateDialog.this.dismiss();
			}
		});
		
		deleteTxt.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				new CustAlertDialog.Builder(context).setMessage(DELETETITLE)
					.setPositiveButton(CONFIRMBTN, new OnClickListener() {				
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String downAdd = down.getUrl();
							String zipPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf("=")+1,downAdd.lastIndexOf("."));
							File f = new File(zipPath);
							if(down.getLocalFileLength()>0){
								down.deleteFile();
							}
							if(down.getLocalFileLength()==0&&f.exists()){
								down.sendMsg(DownLoader.DOWN_NONE, handle);
								down.setStop(true);
								down.setCurSize(0);
								IOUtil.delFileDir(f);
							}
							dialog.dismiss();
						}
					})
					.setNegativeButton(CANCEL, new OnClickListener() {		
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
				}).create().show();
				OperateDialog.this.dismiss();
			}
		});
		
		readTxt.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String downAdd = down.getUrl();
				String zipPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf("=")+1,downAdd.lastIndexOf("."));
				File f = new File(zipPath);
				if(localFileLength==0&&isStop==true&&f.exists()){
					Intent intent=new Intent(context, LessonList.class);
//					intent.putExtra(Constants.bundleKey.bookItem, bookItem);
					Global.getGlobal(context).putData(Constants.bundleKey.bookItem,bookItem);
					OperateDialog.this.dismiss();
					context.startActivity(intent);
				}
				OperateDialog.this.dismiss();
			}
		});
		
		cancelTxt.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				OperateDialog.this.dismiss();
			}
			
		});				
	}
	
	public void setOnDownloadClickListenner(View.OnClickListener onClickListener){
		downTxt.setOnClickListener(onClickListener);
	}
}
