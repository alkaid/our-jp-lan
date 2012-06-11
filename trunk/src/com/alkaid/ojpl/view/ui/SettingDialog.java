package com.alkaid.ojpl.view.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.model.Setting;

public abstract class SettingDialog extends Dialog {
	
	private Context context;
	private Setting setting;
	private final String lblFontSize="大小：";
	private final String lblSpacing="行间距：";
	private int minFontSize;
	private int maxFontSize;
	private int fontSizeProgScale;
	private final int minSpacing=0;
	private final int maxSpacing=18;
	private int spacingProgScale;
	
	public SettingDialog(Context context,Setting setting){
		this(setting,context,R.style.Theme_Dialog);
	}


	public SettingDialog(Setting setting,Context context, int theme) {
		super(context, theme);
		this.context=context;
		this.setting=setting;
		setContentView(R.layout.setting);
		setCanceledOnTouchOutside(true);
		initView();
	}

	private void initView() {
//		this.setTitle("设置");
		final TextView tvFontSize=(TextView)findViewById(R.id.tvFontSize);
		final TextView tvSpacing=(TextView)findViewById(R.id.tvSpacing);
		final SeekBar sbFontSize=(SeekBar)findViewById(R.id.sbFontSize);
		final SeekBar sbSpacing=(SeekBar)findViewById(R.id.sbSpacing);
		final RadioGroup rgPlayMode=(RadioGroup)findViewById(R.id.rgPlayMode);
		final RadioButton rdoSelect=(RadioButton)findViewById(R.id.rdoSelect);
		final RadioButton rdoSequence=(RadioButton)findViewById(R.id.rdoSequence);
		final RadioButton rdoOne=(RadioButton)findViewById(R.id.rdoOne);
		Button btnSave=(Button)findViewById(R.id.btnSave);
		Button btnReset=(Button)findViewById(R.id.btnReset);
		Button btnCancel=(Button)findViewById(R.id.btnCancel);
		
		
		minFontSize=setting.DEFAULT_SIZE-8;
		maxFontSize=setting.DEFAULT_SIZE+4;
		fontSizeProgScale=sbFontSize.getMax()/(maxFontSize-minFontSize);
		spacingProgScale=sbSpacing.getMax()/(maxSpacing-minSpacing);
		
		tvFontSize.setTextSize(setting.getTextSize());
		tvFontSize.setText(lblFontSize+setting.getTextSize());
		tvSpacing.setText(lblSpacing+setting.getLineSpacing());
		sbFontSize.setProgress((setting.getTextSize()-minFontSize)*fontSizeProgScale);
		sbSpacing.setProgress((setting.getLineSpacing()-minSpacing)*spacingProgScale);
		switch (setting.getPlayMode()) {
		case Setting.PLAYMODE_ONE:
			rdoOne.setChecked(true);
			break;
		case Setting.PLAYMODE_SELECT:
			rdoSelect.setChecked(true);
			break;
		case Setting.PLAYMODE_SEQUENCE:
			rdoSequence.setChecked(true);
			break;
		default:
			rdoSelect.setChecked(true);
			break;
		}
		
		sbFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				int fontSize= progress/fontSizeProgScale+minFontSize;
				tvFontSize.setTextSize(fontSize);
				tvFontSize.setText(lblFontSize+fontSize);
			}
		});
		sbSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				int spacing= progress/spacingProgScale+minSpacing;
				tvSpacing.setText(lblSpacing+spacing);
			}
		});
		
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tvFontSize.setTextSize(setting.DEFAULT_SIZE);
				tvFontSize.setText(lblFontSize+setting.DEFAULT_SIZE);
				tvSpacing.setText(lblSpacing+setting.DEFAULT_LINESPACING);
				sbFontSize.setProgress((setting.DEFAULT_SIZE-minFontSize)*fontSizeProgScale);
				sbSpacing.setProgress((setting.DEFAULT_LINESPACING-minSpacing)*spacingProgScale);
				switch (setting.getPlayMode()) {
				case Setting.PLAYMODE_ONE:
					rdoOne.setChecked(true);
					break;
				case Setting.PLAYMODE_SELECT:
					rdoSelect.setChecked(true);
					break;
				case Setting.PLAYMODE_SEQUENCE:
					rdoSequence.setChecked(true);
					break;
				default:
					rdoSelect.setChecked(true);
					break;
				}
			}
		});
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int fontSize=sbFontSize.getProgress()/fontSizeProgScale+minFontSize;
				int spacing=sbSpacing.getProgress()/spacingProgScale+minSpacing;
				int playMode=setting.getPlayMode();
				switch (rgPlayMode.getCheckedRadioButtonId()) {
				case R.id.rdoOne:
					playMode=Setting.PLAYMODE_ONE;
					break;
				case R.id.rdoSelect:
					playMode=Setting.PLAYMODE_SELECT;
					break;
				case R.id.rdoSequence:
					playMode=Setting.PLAYMODE_SEQUENCE;
				default:
					break;
				} 
				setting.setTextSize(fontSize);
				setting.setLineSpacing(spacing);
				setting.setPlayMode(playMode);
				setting.saveSetting();
				onSettingChanged(setting);
				SettingDialog.this.dismiss();
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingDialog.this.dismiss();
			}
		});
	}
	
	protected abstract void onSettingChanged(Setting setting);
}
