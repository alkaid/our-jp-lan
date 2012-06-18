package com.alkaid.ojpl.alipay;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.common.Constants.sharedPreference.aliPay;

public class AliPay {
	static String TAG = "AppDemo4";
	private Activity mActivity;
	private ProgressDialog mProgress = null;
	
	/**
	 * AliPay的构造方法
	 * @param mActivity是传入的Activity
	 */
	public AliPay(Activity mActivity) {
		this.mActivity = mActivity;
	}

	/**
	 * retrieve the product list. 设置商品列表
	 */
	void initProductList() {

	}

	/**
	 * get the selected order info for pay. 获取商品订单信息
	 * 
	 * @param position
	 *            商品在列表中的位置
	 * @return
	 */
	String getOrderInfo() {
		String strOrderInfo = "partner=" + "\"" + PartnerConfig.PARTNER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "seller=" + "\"" + PartnerConfig.SELLER + "\"";
		strOrderInfo += "&";
		strOrderInfo += "out_trade_no=" + "\"" + getOutTradeNo() + "\"";
		strOrderInfo += "&";
		strOrderInfo += "subject=" + "\"" + mActivity.getString(R.string.subjectName)
				+ "\"";
		strOrderInfo += "&";
		strOrderInfo += "body=" + "\"" + mActivity.getString(R.string.subjectBody) + "\"";
		strOrderInfo += "&";
		strOrderInfo += "total_fee=" + "\""
				+ mActivity.getString(R.string.subjectFee) + "\"";
		strOrderInfo += "&";
		strOrderInfo += "notify_url=" + "\""
				+ "http://notify.java.jpxx.org/index.jsp" + "\"";

		return strOrderInfo;
	}

	/**
	 * get the out_trade_no for an order. 获取外部订单号
	 * 
	 * @return
	 */
	String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
		Date date = new Date();
		String strKey = format.format(date);

		java.util.Random r = new java.util.Random();
		strKey = strKey + r.nextInt();
		strKey = strKey.substring(0, 15);
		return strKey;
	}


	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param signType
	 *            签名方式
	 * @param content
	 *            待签名订单信息
	 * @return
	 */
	String sign(String signType, String content) {
		return Rsa.sign(content, PartnerConfig.RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 * @return
	 */
	String getSignType() {
		String getSignType = "sign_type=" + "\"" + "RSA" + "\"";
		return getSignType;
	}

	/**
	 * get the char set we use. 获取字符集
	 * 
	 * @return
	 */
	String getCharset() {
		String charset = "charset=" + "\"" + "utf-8" + "\"";
		return charset;
	}

	/**
	 * the onItemClick for the list view of the products. 商品列表商品被点击事件
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}

	/**
	 * check some info.the partner,seller etc. 检测配置信息
	 * partnerid商户id，seller收款帐号不能为空
	 * 
	 * @return
	 */
	private boolean checkInfo() {
		String partner = PartnerConfig.PARTNER;
		String seller = PartnerConfig.SELLER;
		if (partner == null || partner.length() <= 0 || seller == null
				|| seller.length() <= 0)
			return false;

		return true;
	}

	//
	// the handler use to receive the pay result.
	// 这里接收支付结果，支付宝手机端同步通知
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String strRet = (String) msg.obj;

				// 测试中打印同步通知log，上线建议注释掉，或者自行设置开关
				Log.d(TAG, strRet);
				
				switch (msg.what) {
				case AlixId.RQF_PAY: {
					//
					closeProgress();

					BaseHelper.log(TAG, strRet);

					// 此处将提示给开发人员具体的交易状态码，
					// 由于安全支付服务付款成功以后会有提示展示给用户，所以建议在上线版本中不进行额外提示
					// 以免造成用户提示的混乱。
					// 从通知中获取参数
					try {
						// 获取交易状态，具体状态代码请参看文档
						String tradeStatus = "resultStatus=";
						int imemoStart = strRet.indexOf("resultStatus=");
						imemoStart += tradeStatus.length();
						int imemoEnd = strRet.indexOf(";memo=");
						tradeStatus = strRet.substring(imemoStart, imemoEnd);

						// 对通知进行验签
						ResultChecker resultChecker = new ResultChecker(strRet);

						int retVal = resultChecker.checkSign();
						// 返回验签结果以及交易状态
						// 验签失败
						if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) {
							BaseHelper.showDialog(
									mActivity,
									"提示",
									mActivity.getResources().getString(
											R.string.check_sign_failed),
									android.R.drawable.ic_dialog_alert);
						} else {
							if(tradeStatus.equals("{9000}")){
								BaseHelper.showDialog(mActivity, "提示",aliPay.successCostAlert, R.drawable.infoicon);
							}else{
								BaseHelper.showDialog(mActivity,"提示", aliPay.failCostAlert, R.drawable.infoicon);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();

						BaseHelper.showDialog(mActivity, "提示", strRet,
								R.drawable.infoicon);
					}
				}
					break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	//
	//
	/**
	 * the OnCancelListener for lephone platform. lephone系统使用到的取消dialog监听
	 */
	static class AlixOnCancelListener implements
			DialogInterface.OnCancelListener {
		Activity mcontext;

		AlixOnCancelListener(Activity context) {
			mcontext = context;
		}

		public void onCancel(DialogInterface dialog) {
			mcontext.onKeyDown(KeyEvent.KEYCODE_BACK, null);
		}
	}

	//
	// close the progress bar
	// 关闭进度框
	void closeProgress() {
		try {
			if (mProgress != null) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回键监听事件
	 */
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			BaseHelper.log(TAG, "onKeyDown back");
//
//			this.finish();
//			return true;
//		}
//
//		return false;
//	}

	//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Log.v(TAG, "onDestroy");
//
//		try {
//			mProgress.dismiss();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
	/**
	 * 支付宝支付的方法
	 */
	public void pay(){
		// check to see if the MobileSecurePay is already installed.
		// 检测安全支付服务是否安装
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(mActivity);
		boolean isMobile_spExist = mspHelper.detectMobile_sp();
		if (!isMobile_spExist)
			return;

		// check some info.
		// 检测配置信息
		if (!checkInfo()) {
			BaseHelper
					.showDialog(
							mActivity,
							"提示",
							"缺少partner或者seller，请在src/com/alipay/android/appDemo4/PartnerConfig.java中增加。",
							R.drawable.infoicon);
			return;
		}

		// start pay for this order.
		// 根据订单信息开始进行支付
		try {
			// prepare the order info.
			// 准备订单信息
			String orderInfo = getOrderInfo();
			// 这里根据签名方式对订单信息进行签名
			String signType = getSignType();
			String strsign = sign(signType, orderInfo);
			// 对签名进行编码
			strsign = URLEncoder.encode(strsign);
			// 组装好参数
			String info = orderInfo + "&sign=" + "\"" + strsign + "\"" + "&"
					+ getSignType();
			// start the pay.
			// 调用pay方法进行支付
			MobileSecurePayer msp = new MobileSecurePayer();
			boolean bRet = msp.pay(info, mHandler, AlixId.RQF_PAY, mActivity);

			if (bRet) {
				// show the progress bar to indicate that we have started
				// paying.
				// 显示“正在支付”进度条
				closeProgress();
				mProgress = BaseHelper.showProgress(mActivity, null, "正在支付", false,
						true);
			} else
				;
		} catch (Exception ex) {
			Toast.makeText(mActivity, R.string.remote_call_failed,
					Toast.LENGTH_SHORT).show();
		}

	}
}
