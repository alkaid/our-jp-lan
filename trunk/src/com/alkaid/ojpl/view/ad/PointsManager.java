package com.alkaid.ojpl.view.ad;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.view.ui.CustAlertDialog;
import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;
/**
 * 积分管理类
 * @author Alkaid
 *
 */
public class PointsManager {
	/** 由于获取积分是异步的，该常量标识获取积分的状态:成功*/
	public static final int GETPOINTS_SUCCESS=1;
	/** 由于获取积分是异步的，该常量标识获取积分的状态:服务器还未返还数据*/
	public static final int GETPOINTS_LOADING=2;
	/** 由于获取积分是异步的，该常量标识获取积分的状态:发生错误*/
	public static final int GETPOINTS_ERROR=3;
	
	private Context context;
	private UpdatePoints updatePoints;
	/** 是否开启积分墙 若手机没联网 则默认不开启 联网状态下，它的值等同于{@link #pointsEnable} */
	private boolean offersEnable=false;
	/** 是否开启积分系统 由服务器获得 由于是下载需要积分，则可以不考虑没联网的状态*/
	private boolean pointsEnable=false;
	private static final String KEY_POINTS_ENABLE="pointsEnable";
	public PointsManager(Context context){
		this.context=context;
		init(context);
		updatePoints=new UpdatePoints(context);
		updatePoints.getAsyncPoints();
	}
	
	//初始化万普
	public static void init(Context context){
		AppConnect.getInstance(context);
	}
	
	public static boolean offersEnable(Context context){
		return "true".equals(AppConnect.getInstance(context).getConfig(KEY_POINTS_ENABLE));
	}
	/**
	 * 判断积分是否充足并弹窗
	 * @param context
	 * @return 积分是否充足
	 */
	public boolean isPointsEnough(){
		int status=getGetpointsStatus();
		//这个拿到的是初始化时拿到的缓存值 不是实时的
//		pointsEnable="true".equals(AppConnect.getInstance(context).getConfig(KEY_POINTS_ENABLE));
		//实时获取  测试发现离线情况下拿到的是缓存
		pointsEnable="true".equals(AppConnect.getInstance(context).getConfig_Sync(KEY_POINTS_ENABLE));
		if(!pointsEnable){}
		//若积分系统未开启，则免积分
		if(!pointsEnable){
			return true;
		}
		String tip=null;
		switch (status) {
		case GETPOINTS_LOADING:
			tip="正在从服务器获取数据,请等待5-10秒钟再进行操作";
			Toast.makeText(context, tip, Toast.LENGTH_SHORT).show(); 
			break;
		case GETPOINTS_ERROR:
			tip="正在从服务器获取数据,请等待5-10秒钟再进行操作";
			Toast.makeText(context, tip, Toast.LENGTH_SHORT).show(); 
			break;
		case GETPOINTS_SUCCESS:
			int points=updatePoints.getTotalPoints();
			tip="每部分学习资料阅读需要"+Constants.points.perAction+"积分,您当前积分为"+points+",积分不足,点击获取积分按钮免费获得积分";
			
			if(points>Constants.points.perAction){
				return true;
			}
			new CustAlertDialog.Builder(context)
			.setMessage(tip)
			.setPositiveButton("获取积分", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					showOffers();
					dialog.dismiss();
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.dismiss();
				}
			})
			.create().show();
			break;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * 万普异步获取积分类
	 * @author Alkaid
	 *
	 */
	private static class UpdatePoints implements UpdatePointsNotifier{
		private Context context;
		private String currencyName=null;
		private int totalPoints=-1;
		private String error=null;
		
		public UpdatePoints(Context context){
			this.context=context;
		}
		/** 异步获取积分 */
		public void getAsyncPoints(){
			AppConnect.getInstance(context).getPoints(this);
		}
		@Override
		public void getUpdatePoints(String currencyName, int pointTotal) {
			this.currencyName=currencyName;
			this.totalPoints=pointTotal;
			LogUtil.i("当前"+currencyName+"："+pointTotal);
		}
		@Override
		public void getUpdatePointsFailed(String error) {
			this.error=error;
			LogUtil.w("获取积分时发生错误："+error);
			this.getAsyncPoints();
		}
		/** 获得货币名称*/
		public String getCurrencyName() {
			return currencyName;
		}
		/** 获得积分 */
		public int getTotalPoints() {
			return totalPoints;
		}
		/** 获得错误信息 */
		public String getError() {
			return error;
		}
	}
	
	/** 获得货币名称*/
	public String getCurrencyName() {
		return updatePoints.currencyName;
	}
	/** 获得积分 */
	public int getTotalPoints() {
		return updatePoints.totalPoints;
	}
	/** 获得错误信息 */
	public String getError() {
		return updatePoints.error;
	}
	/** 判断获取积分的状态 结果为{@link #GETPOINTS_SUCCESS},{@link #GETPOINTS_LOADING},{@link #GETPOINTS_ERROR}*/
	public int getGetpointsStatus(){
		if(updatePoints.totalPoints>=0)
			return GETPOINTS_SUCCESS;
		if(updatePoints.error==null)
			return GETPOINTS_LOADING;
		return GETPOINTS_ERROR;
	}
	/** 消费积分 */
	public void spendPoints(int amount){
		AppConnect.getInstance(context).spendPoints(amount, updatePoints);
	}
	/** 奖励积分 */
	public void awardPoints(int amount){
		AppConnect.getInstance(context).awardPoints(amount, updatePoints);
	}
	/** 展示积分墙 */
	public void showOffers(){
		AppConnect.getInstance(context).showOffers(context);
	}
	/** 回收资源*/
	public void finalize(){
		AppConnect.getInstance(context).finalize();
	}
	/** 展示积分墙 */
	public static void showOffers(Context context){
		AppConnect.getInstance(context).showOffers(context);
	}
}
