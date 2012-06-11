/**
 * 
 */
package com.alkaid.ojpl.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.alkaid.ojpl.common.AlkaidException;
import com.alkaid.ojpl.common.IOUtil;
import com.alkaid.ojpl.common.LogUtil;

/**
 * @author Alkaid
 * 五十音图数据读取
 */
public class FiftyMapDao {

	private final String assets_data="fiftymap/fiftymap.dat";
	private final String assets_head="fiftymap/fiftyhead.dat";
	
	Context context;
	public FiftyMapDao(Context context) {
		this.context=context;
	}
	/**
	 * 解析Assets数据文件.dat 返回texts数组
	 * @param assetFile
	 * @return
	 * @throws AlkaidException
	 */
	private List<StringBuilder> parseDatFile(String assetFile) throws AlkaidException{
		//解析dat文件
		List<StringBuilder> texts=new ArrayList<StringBuilder>();
		String contents=null;
		InputStream is=null;
		try {
			is = context.getAssets().open(assetFile);
			contents = IOUtil.readInputStrem2Str(is, "utf-8");
		} catch (FileNotFoundException e) {
			throw new AlkaidException("文件不存在！");
		} catch (IOException e) {
			LogUtil.e(e);
			return null;
		}
		String regex = "\\*[^\\*]*\\n\\*";  
	    Pattern p = Pattern.compile(regex);  
	    Matcher m = p.matcher(contents);  
	    while (m.find()){  
	    	texts.add( new StringBuilder(m.group()) ); 
	    } 
	    return texts;
	}
	
	public void getHeads(List<String[]> spanHead,List<String[]> colHead){
		List<StringBuilder> data=parseDatFile(assets_head);
		for(StringBuilder text:data){
			int firstIndexOfLine=text.indexOf("\n");
			text.replace(0, firstIndexOfLine+1, "");
			text.replace(text.length()-2, text.length(), "");
			String[] strheads=text.toString().split("\n");
			for(int i=0;i<strheads.length;i++){
				String[] heads=strheads[i].split("\t");
				if(i==0){
					colHead.add(heads);
				}else if(i==1){
					spanHead.add(heads);
				}
			}
		}
	}
	
	public void getData(List<String[][]> hiraganas,List<String[][]> katakanas,List<String[][]> romes){
		List<StringBuilder> data=parseDatFile(assets_data);
		for(int i=0;i<data.size();i++){
			StringBuilder text=data.get(i);
			int firstIndexOfLine=text.indexOf("\n");
			text.replace(0, firstIndexOfLine+1, "");
			text.replace(text.length()-2, text.length(), "");
			String[] lines=text.toString().split("\n");
			String[][] mHiraganas = null;
			String[][] mKatakanas = null;
			String[][] mRomas = null;
			for(int j=0;j<lines.length;j++){
				String[] cells=lines[j].split("\t");
				if(j==0){
					//初始化数组
					mHiraganas=new String[lines.length][cells.length/3];
					mKatakanas=new String[lines.length][cells.length/3];
					mRomas=new String[lines.length][cells.length/3];
				}
				for(int k=0;k<cells.length;k++){
					switch (k%3) {
					//平假名
					case 0:
						mHiraganas[j][k/3]=cells[k];
						break;
					//片假名
					case 1:
						mKatakanas[j][k/3]=cells[k];
						break;
					//罗马拼音
					case 2:
						mRomas[j][k/3]=cells[k];
						break;
					default:
						break;
					}
				}
			}
			hiraganas.add(mHiraganas);
			katakanas.add(mKatakanas);
			romes.add(mRomas);
		}
	}
}
