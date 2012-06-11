package com.alkaid.ojpl.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.alkaid.ojpl.view.BookShelf;


/**
 * @author Lelouch
 *用于下载的下载类，包括下载，删除等操作
 */
public class DownLoader {
	/** 下载状态：未下载*/
	public static final int DOWN_NONE=3;
	/** 下载状态：正在下载*/
	public static final int DOWN_LOADING=1;
	/** 下载状态：暂停*/
	public static final int DOWN_PAUSE=0;
	/** 下载状态：完成*/
	public static final int DOWN_COMPLETE=2;
	/** 下载状态：开始下载*/
	public static final int DOWN_BEGIN=4;
	/** 解压缩状态，开始解压*/
	public static final int ZIP_BEGIN=5;
	public int tag;
	private boolean isStop=true;
	private long curSize;
	private long fileSize;
	private String path;
	private String url;
	private Handler handle;
	private Context context; 
	
	
	public DownLoader(int tag,Handler handle) {
		this.tag = tag;
		this.handle = handle;
	}
	
	public DownLoader(int tag,String url,String path,Handler handle,Context context){
		this.tag = tag;
		this.url = url;
		this.path = path;
		this.handle = handle;
		this.context =context;
	}
	
	/**文件下载到本地 */
	public void down(){
//LogUtil.e("下载"+curSize+"-"+fileSize);
		sendMsg(DOWN_BEGIN, handle);
		isStop = false;
		File localFile = getLocalFile();
		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile accessFile = null;
		//判断本地文件是否存在，不存在新建文件
		if(localFile == null){
			localFile = new File(path);
			if(!localFile.getParentFile().exists()){
				localFile.getParentFile().mkdirs();
			}
			try {
				localFile.createNewFile();
				curSize = 0;
			} catch (IOException e) {
				LogUtil.e("本地文件不存在");
			}
		}else{
			curSize = localFile.length();
		}
		//用于获取输入流
		try {
			URL u = new URL(url);
			conn= (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5*1000);
			conn.setRequestProperty("Range","bytes="+curSize+"-");
			is = conn.getInputStream();
			if(curSize<=0){
				fileSize =conn.getContentLength();
				onDownloadBegin(fileSize);
			}
			LogUtil.i("connected...........");
		} catch (IOException e) {
			Message msg=handle.obtainMessage(Constants.msgWhat.error);
			msg.getData().putString(Constants.bundleKey.errorMsg, "网络连接有异常");
			LogUtil.e(e);
			conn.disconnect();
		}
		//写输出流到本地文件中
		if(is!=null){
			try {
				accessFile = new RandomAccessFile(localFile, "rwd");
				BufferedInputStream bis = new BufferedInputStream(is);
				accessFile.seek(curSize);
				byte[] buffer = new byte[1024];
				int len = 0;
				LogUtil.i("reading...........");
				int destPercent=1;
				while(isStop==false&&(len = bis.read(buffer))!=-1){
					accessFile.write(buffer,0,len);
					curSize += len;
					if(isStop==false&&!handle.equals(null)){
						int percent=getPercent();
						if(percent>=destPercent){
							sendMsg(DOWN_LOADING, handle);
							destPercent=percent+1;
						}
					}
				}
			} catch (IOException e) {
				LogUtil.e(e);
			}finally{
				conn.disconnect();
				try {
					is.close();
					accessFile.close();
				} catch (IOException e) {
					LogUtil.e(e);
				}
			}
		}
//LogUtil.e("下载ing"+curSize+"-"+fileSize);
		//文件下载，如果为断点续传将不执行，直至下载完毕
		if(getPercent()==100){					
			if(!handle.equals(null)){
				isStop = true;
				sendMsg(ZIP_BEGIN, handle);
//LogUtil.e("完成"+curSize+"-"+fileSize);
			}
		}		
	}
	/** 暂停下载 */
	public void pause(){
//LogUtil.e("暂停"+curSize+"-"+fileSize);
		if(!handle.equals(null)){
			sendMsg(DOWN_PAUSE, handle);
		}
		isStop = true;
	}
	/** 删除文件*/
	public boolean deleteFile(){
		File file = getLocalFile();
		if(file.exists()){
			file.delete();
			if(!handle.equals(null)){
				sendMsg(DOWN_NONE, handle);
			}
			isStop = true;
			curSize = 0;
			return true;
		}else{
			return false;
		}	
	}	
	/** 获取本地文件*/
	public File getLocalFile(){
		File localFile = new File(path);
		if(localFile.exists()){
			return localFile;
		}else{
			return null;
		}
	}
	/** 取得本地文件长度*/
	public long getLocalFileLength(){
		if(getLocalFile()!=null){
			return getLocalFile().length();
		}else{
			return 0;
		}
		
	}
	/** 实时获取下载的文件文件长度，会联网，非缓存值*/
	/*public long getFileLength(){		
		HttpURLConnection conn;
		try {
			LogUtil.i("get length connectting...........");
			URL u = new URL(url);
			conn= (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5*1000);
			fileSize =conn.getContentLength();
			LogUtil.i("get length connected ...........");
//			Map<String, List<String>> headerMap = conn.getHeaderFields();
//			Iterator<String> iterator = headerMap.keySet().iterator();
//			while (iterator.hasNext()) {
//			String key = iterator.next();
//			List<String> values = headerMap.get(key);
//			LogUtil.e(key + ":" +values.toString());
//			}
		} catch (IOException e) {
			LogUtil.e("网络连接有异常");
		}
		return fileSize;
	}*/
	
	/** 下载时所响应的操作消息*/
	public void sendMsg(int downState,Handler handle){
		Message msg = handle.obtainMessage(BookShelf.msg_what_updateBookView);		
		msg.arg1=this.tag;
		msg.arg2=downState;
		handle.sendMessage(msg);
	}
	
	/**下载完成时,用于检测完整性和正确性*/
	public boolean downCheck(){
		if(getPercent()<0&&getPercent()>100){
			return false;
		}else{
			return true;
		}
	}
	
	/**用于获得下载进度的百分比*/
	public int getPercent(){
		int percent = (int) (100*(curSize)/fileSize);
		return percent;
	}
	
	
	public long getCurSize() {
		return curSize;
	}

	public void setCurSize(long curSize) {
		this.curSize = curSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}


	/**
	 * 下载前操作 子类覆盖此方法
	 * @param filesize 文件长度 {@link #fileSize}
	 */
	protected void onDownloadBegin(long filesize) {}
	
}
