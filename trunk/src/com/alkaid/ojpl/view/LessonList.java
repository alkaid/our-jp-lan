/**
 * 
 */
package com.alkaid.ojpl.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alkaid.ojpl.R;
import com.alkaid.ojpl.common.Constants;
import com.alkaid.ojpl.data.BookItemOperator;
import com.alkaid.ojpl.model.BookItem;
import com.alkaid.ojpl.model.LessonItem;
import com.alkaid.ojpl.model.Setting;
import com.alkaid.ojpl.view.ad.BannerAdManager;

/**
 * @author Alkaid
 * 课本列表
 *
 */
public class LessonList extends BaseActivity {
	
//	private String bookId;
	private BookItem bookItem;
//	private ArrayList<String> lessonTitles=new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lesson_list);
		bookItem = (BookItem) global.getData(Constants.bundleKey.bookItem);
		if(null==bookItem){
			String bookItemId=savedInstanceState.getString(Constants.bundleKey.bookItemId);
			bookItem=new BookItemOperator().getBookItemById(bookItemId, this);
			initView();
		}else{
			initView();
		}
	}

	/**
	 * 
	 */
	private void initView() {
		ListView lessonList = (ListView) this.findViewById(R.id.lesson_section_id);
		LessonsAdapter lAdapter = new LessonsAdapter(); 
		lessonList.setAdapter(lAdapter);
		lessonList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lessonList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//当课本为说明的书本时做单独处理
				Intent intent = null;
				if(bookItem.getId().equals(Constants.INTRODUCTION)){
					intent=new Intent(context, Introduction.class);
					global.putData(Constants.bundleKey.bookItem,bookItem);
					intent.putExtra(Constants.bundleKey.lessonId, bookItem.getLessonItems().get(position).getId());
				}else{
					intent=new Intent(context, LessonContents.class);
//				intent.putExtra(Constants.bundleKey.bookItem, bookItem);
					global.putData(Constants.bundleKey.bookItem,bookItem);
					intent.putExtra(Constants.bundleKey.lessonId, bookItem.getLessonItems().get(position).getId());
				}
	//				bundle.putStringArrayList(Constants.bundleKey.lessonTitle, lessonTitles);
	//				dataTranslate(bundle,LessonContents.class);
				startActivity(intent);
			}
		});
		Button btnBack=(Button)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(Constants.bundleKey.bookItemId, bookItem.getId());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		new BannerAdManager(this).creatAd();
	}
	
	public class LessonsAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return bookItem.getLessonItems().size();
		}

		@Override
		public Object getItem(int position) {
			return bookItem.getLessonItems().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) LessonList.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			LinearLayout lessonLayout = (LinearLayout) inflater.inflate(R.layout.lesson_section, null);	
			TextView section = (TextView) lessonLayout.findViewById(R.id.lesson_title_id);
			LessonItem lesson = bookItem.getLessonItems().get(position);
			section.setText(lesson.getTitle());
			section.setTypeface(Setting.getJpFontType(LessonList.this));
			return lessonLayout;
		}
		
	}
}
