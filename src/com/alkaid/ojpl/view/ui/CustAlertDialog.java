/**
 * 
 */
package com.alkaid.ojpl.view.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.alkaid.ojpl.R;

/**
 * @author Alkaid
 *
 */
public class CustAlertDialog extends Dialog{
	public CustAlertDialog(Context context, int theme) {  
        super(context, theme);  
    }  
    public CustAlertDialog(Context context) {  
        super(context);  
    }  
    /** 
     * Helper class for creating a custom dialog 
     */  
    public static class Builder{

    	private Context context;  
        private String title;  
        private String message;
        //单选框默认被选中的Item
        private int defaultCheckedItem;
    	private int checkedItem = -1;
        private String positiveButtonText;  
        private String negativeButtonText;
        private String[] singleChoiceItems;
        private View contentView;  
        private boolean canceledOnTouchOutside=false;
//        private final static String DEFAULT_POSITIVE_TEXT="确定";
//        private final static String DEFAULT_NEGATIVE_TEXT="取消";
   
        private DialogInterface.OnClickListener   
                        positiveButtonClickListener,  
                        negativeButtonClickListener; 
        public Builder(Context context) { 
            this.context = context;  
        }  
   
        /**
         * 是否可以点击对话框外面以取消对话框
         * @param canceledOnTouchOutside
         * @return
         */
        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
			this.canceledOnTouchOutside = canceledOnTouchOutside;
			return this;
		}

		/** 
         * Set the Dialog message from String 
         * @param title 
         * @return 
         */  
        public Builder setMessage(String message) {  
            this.message = message;  
            return this;  
        }  
   
        /** 
         * Set the Dialog message from resource 
         * @param title 
         * @return 
         */  
        public Builder setMessage(int message) {  
            this.message = (String) context.getText(message);  
            return this;  
        }  
   
        /** 
         * Set the Dialog title from resource 
         * @param title 
         * @return 
         */  
        public Builder setTitle(int title) {  
            this.title = (String) context.getText(title);  
            return this;  
        }  
   
        /** 
         * Set the Dialog title from String 
         * @param title 
         * @return 
         */  
        public Builder setTitle(String title) {  
            this.title = title;  
            return this;  
        }  
   
        /** 
         * Set a custom content view for the Dialog. 
         * If a message is set, the contentView is not 
         * added to the Dialog... 
         * @param v 
         * @return 
         */  
        public Builder setContentView(View v) {  
            this.contentView = v;  
            return this;  
        }  
   
        /** 
         * Set the positive button resource and it's listener 
         * @param positiveButtonText 
         * @param listener 
         * @return 
         */  
        public Builder setPositiveButton(int positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = (String) context  
                    .getText(positiveButtonText);  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
   
        /** 
         * Set the positive button text and it's listener 
         * @param positiveButtonText 
         * @param listener 
         * @return 
         */  
        public Builder setPositiveButton(String positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = positiveButtonText;  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
   
        /** 
         * Set the negative button resource and it's listener 
         * @param negativeButtonText 
         * @param listener 
         * @return 
         */  
        public Builder setNegativeButton(int negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = (String) context  
                    .getText(negativeButtonText);  
            this.negativeButtonClickListener = listener;  
            return this;  
        }  
   
        /** 
         * Set the negative button text and it's listener 
         * @param negativeButtonText 
         * @param listener 
         * @return 
         */  
        public Builder setNegativeButton(String negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = negativeButtonText;  
            this.negativeButtonClickListener = listener;  
            return this;  
        }
        /**
         * Set the singleChoiceItems'texts and their listener
         * @param singleChoiceItems
         * @param listener
         * @param defaultCheckedItem 默认被选中的单选项
         * @return
         */
        public Builder setSingleChoiceItems(String[] singleChoiceItems,int defaultCheckedItem){
        	this.singleChoiceItems = singleChoiceItems;
        	this.defaultCheckedItem = defaultCheckedItem;
        	checkedItem = defaultCheckedItem;
        	return this;
        }
   
        /** 
         * Create the custom dialog 
         */  
        public CustAlertDialog create() {  
            LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
            final CustAlertDialog dialog = new CustAlertDialog(context,   
                    R.style.AlertDialog);  
            View layout = inflater.inflate(R.layout.alert_dialog, null);  
            dialog.addContentView(layout, new LayoutParams(  
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
            // set the dialog title  
//            ((TextView) layout.findViewById(R.id.tvTitle)).setText(title);
            //set the askRadioGroup
            if(singleChoiceItems!=null&&singleChoiceItems.length>0){
            	RadioGroup rg = (RadioGroup) layout.findViewById(R.id.rgPointAsk);
            	int i = 0;
            	while(i<singleChoiceItems.length){
            		if(singleChoiceItems[i] != null){
	            		RadioButton rb = new RadioButton(context);
	            		rb.setId(i);
	            		rb.setText(singleChoiceItems[i]);
	            		//默认被选中
	            		if(i == defaultCheckedItem){
	            			rb.setChecked(true);
	            		}
	            		rg.addView(rb);
            		}
            		i++;           		
            	}
            	rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						checkedItem = checkedId;
					}
				});
            }else{
            	layout.findViewById(R.id.rgPointAsk).setVisibility(  
                        View.GONE);  
            }
            // set the confirm button  
            if (positiveButtonText != null) {  
                ((Button) layout.findViewById(R.id.positiveButton))  
                        .setText(positiveButtonText);  
                ((Button) layout.findViewById(R.id.positiveButton))  
                        .setOnClickListener(new View.OnClickListener() {  
                            public void onClick(View v) {
                            	if(null==positiveButtonClickListener){
                            		dialog.dismiss();
                            	}else{
                                    positiveButtonClickListener.onClick(  
                                            dialog,   
                                            checkedItem);  
                            	}
                            }  
                        });  
            } else {  
                // if no confirm button just set the visibility to GONE  
                layout.findViewById(R.id.positiveButton).setVisibility(  
                        View.GONE);  
            }  
            // set the cancel button  
            if (negativeButtonText != null) {  
                ((Button) layout.findViewById(R.id.negativeButton))  
                        .setText(negativeButtonText);  
                ((Button) layout.findViewById(R.id.negativeButton))  
                        .setOnClickListener(new View.OnClickListener() {  
                            public void onClick(View v) {  
                            	if (negativeButtonClickListener == null) {
                            		dialog.dismiss();
                            	}else{
                                	negativeButtonClickListener.onClick(  
                                    		dialog,   
                                            DialogInterface.BUTTON_NEGATIVE);  
                            	}
                            }  
                        });  
            } else {  
                // if no confirm button just set the visibility to GONE  
                layout.findViewById(R.id.negativeButton).setVisibility(  
                        View.GONE);  
            }  
            // set the content message  
            if (message != null) {  
                ((TextView) layout.findViewById(  
                        R.id.tvMessage)).setText(message);  
            } else if (contentView != null) {  
                // if no message set  
                // add the contentView to the dialog body  
//                ((LinearLayout) layout.findViewById(R.id.content))  
//                        .removeAllViews();  
//                ((LinearLayout) layout.findViewById(R.id.content))  
//                        .addView(contentView,   
//                                new LayoutParams(  
//                                        LayoutParams.WRAP_CONTENT,   
//                                        LayoutParams.WRAP_CONTENT));  
            }  
            dialog.setContentView(layout);  
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            return dialog;  
        }
    	
    }
}
