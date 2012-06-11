/**
 * 
 */
package com.alkaid.ojpl.model;

import java.util.ArrayList;
import java.util.List;

import com.alkaid.ojpl.common.Constants;

/**
 * @author Alkaid
 * 课文实体类<br/>
 * 要使用lesson之前必须调用 {@link #init(BookItem)}
 */
public class Lesson extends Model{

	private static final long serialVersionUID = 5322381412069403249L;
	/** 最后一课id */
//	private static final int LAST_LESSON_ID=25;
	/** 所在册数*/
	private int volumn;
	/** 每册的最后一课id*/
	private static final int[] lastLessonIds=new int[]{25,50};
	/** 几课一组 */
//	private static final int GROUP_INTERVAL=4;
	/** 每册分别分成几组  最后一组不够{@link GROUP_INTERVAL}课的与前面一组合并 */
//	private static final int GROUP_COUNT=Math.max(1, LAST_LESSON_ID/GROUP_INTERVAL);
	/*private static final int[] groupCounts=new int[]{
		Math.max(1, lastLessonIds[0]/GROUP_INTERVAL),
		Math.max(lastLessonIds[1], (lastLessonIds[1]-lastLessonIds[0])/GROUP_INTERVAL)
	};*/
	
	/** id 第几课*/
	private int id;
	/** 课文标题（日文） */
	private String titleJp;
	/** 具体文章 */
	private List<Article> articles=new ArrayList<Article>();
	/** 资源路径 */
	private String path;
	/** 数据文件路径 */
	private String datFilePath;
	
	/** 当前分组中第一课的课文Id*/
	private int groupFirstId;
	/** 当前分组中最后一课的课文Id*/
	private int groupLastId;
	/** 课文在当前分组中的位置*/
	private int positionInGroup;
	/** 当前分组长度 */
	private int groupSize;
	/** 当前分组课文的下一课的id 若是最后一课则下一课为分组第一课 */
	private int groupNextId;
	/** 当前分组课文的上一课的id 若是第一课则下一课为分组最后一课 */
	private int groupPreId;
	
	/*public Lesson(int id) {
		this.id=id;
		if(id>0){
			this.groupFirstId=calcCurrentGroupFirstId(id);
			this.groupLastId=calcCurrentGroupLastId(id);
			this.groupSize=groupLastId-groupFirstId+1;
			int volumn=calcCurrentVolumn(id);
			if(volumn==1){
				this.path=Constants.PATH_RES + "/djdry_1_" + groupFirstId + "_" + groupLastId;
			}else{
				this.path=Constants.PATH_RES + "/djdry"+volumn+"_1_" + groupFirstId + "_" + groupLastId;
			}
			this.positionInGroup=id-groupFirstId;
			this.datFilePath=path+"/"+(positionInGroup+1)+".dat";
			this.groupNextId=id==groupLastId?groupFirstId:id+1;
			this.groupPreId=id==groupFirstId?groupLastId:id-1;
		}
	}*/
	
	private Lesson(){}
	public static Lesson getEmptyLesson(){
		return new Lesson();
	}
	
	public Lesson(int id,BookItem bookItem){
		this.id=id;
		this.groupFirstId=Integer.parseInt(bookItem.getLessonItems().get(0).getId());
		this.groupLastId=Integer.parseInt(bookItem.getLessonItems().get(bookItem.getLessonItems().size()-1).getId());
		this.groupSize=groupLastId-groupFirstId+1;
		volumn=calcCurrentVolumn(id);
		this.path=Constants.PATH_RES + "/djdry_"+volumn+"_" + groupFirstId + "_" + groupLastId;
		this.positionInGroup=id-groupFirstId;
		this.datFilePath=path+"/"+(positionInGroup+1)+".dat";
		this.groupNextId=id==groupLastId?groupFirstId:id+1;
		this.groupPreId=id==groupFirstId?groupLastId:id-1;
		this.titleJp=bookItem.getLessonItems().get(positionInGroup).getTitle();
	}
	
	/** 根据lessonId获得lesson所在册数*/
	public static int calcCurrentVolumn(int lessonId){
		int volumn=1;
		for(int i=0;i<lastLessonIds.length;i++){
			if(lessonId>lastLessonIds[i]){
				volumn+=1;
			}
		}
		return volumn;
	}
	
	/** 根据lessonId获得当前分组的第一课Id *//*
	public static int calcCurrentGroupFirstId(int lessonId) {
		// 按分组计算出当前lesson是在哪一组
		int from;
		from = lessonId - (lessonId - 1) % GROUP_INTERVAL;
		// 不等分的情况下 对最后两组做特俗处理
		if (LAST_LESSON_ID % GROUP_INTERVAL > 0) {
			// 当前所在组，这里不够数的一组依然算一组，并是按合并计算当前组
			int currentGroup = (lessonId - 1 + GROUP_INTERVAL) / GROUP_INTERVAL;
			if (currentGroup == GROUP_COUNT + 1) {
				from -= GROUP_INTERVAL;
			} else if (currentGroup == GROUP_COUNT) {
			}
		}
		return from;
		
		//得到大分组
		int GROUP_COUNT=0;
		int LAST_LESSON_ID=0;
		int offSetLessonId=lessonId;	//根据每册的最后一课作为偏移值得到的lessonId
		int offSet=0;	//偏移值  一般是最后一课
		for(int i=0;i<lastLessonIds.length;i++){
			if(lessonId>lastLessonIds[i]){
				GROUP_COUNT=groupCounts[i+1];
				LAST_LESSON_ID=lastLessonIds[i+1];
				offSet=lastLessonIds[0];
				offSetLessonId=lessonId - offSet;
			}
		}
		// 按分组计算出当前lesson是在哪一组
		int from;
		from = offSetLessonId - (offSetLessonId - 1) % GROUP_INTERVAL;
		// 不等分的情况下 对最后两组做特俗处理
		if (LAST_LESSON_ID % GROUP_INTERVAL > 0) {
			// 当前所在组，这里不够数的一组依然算一组，并是按合并计算当前组
			int currentGroup = (offSetLessonId - 1 + GROUP_INTERVAL) / GROUP_INTERVAL;
			if (currentGroup == GROUP_COUNT + 1) {
				from -= GROUP_INTERVAL;
			} else if (currentGroup == GROUP_COUNT) {
			}
		}
		return from+offSet;
	}*/
	/** 根据lessonId获得当前分组的最后一课Id *//*
	public static int calcCurrentGroupLastId(int lessonId) {
		// 按分组计算出当前lesson是在哪一组
		int to;
		int delta = GROUP_INTERVAL - lessonId % GROUP_INTERVAL;
		delta = delta == GROUP_INTERVAL ? 0 : delta;
		to = lessonId + delta;
		// 不等分的情况下 对最后两组做特俗处理
		if (LAST_LESSON_ID % GROUP_INTERVAL > 0) {
			// 当前所在组，这里不够数的一组依然算一组，并是按合并计算当前组
			int currentGroup = (lessonId - 1 + GROUP_INTERVAL) / GROUP_INTERVAL;
			if (currentGroup == GROUP_COUNT + 1) {
				to = LAST_LESSON_ID;
			} else if (currentGroup == GROUP_COUNT) {
				to = LAST_LESSON_ID;
			}
		}
		return to;
		
		//得到所在册数
		int GROUP_COUNT=0;
		int LAST_LESSON_ID=0;
		int offSetLessonId=lessonId;	//根据每册的最后一课作为偏移值得到的lessonId
		int offSet=0;	//偏移值  一般是最后一课
		for(int i=0;i<lastLessonIds.length;i++){
			if(lessonId>lastLessonIds[i]){
				GROUP_COUNT=groupCounts[i+1];
				LAST_LESSON_ID=lastLessonIds[i+1];
				offSet=lastLessonIds[0];
				offSetLessonId=lessonId - offSet;
			}
		}
		// 按分组计算出当前lesson是在哪一组
		int to;
		int delta = GROUP_INTERVAL - offSetLessonId % GROUP_INTERVAL;
		delta = delta == GROUP_INTERVAL ? 0 : delta;
		to = offSetLessonId + delta;
		// 不等分的情况下 对最后两组做特俗处理
		if (LAST_LESSON_ID % GROUP_INTERVAL > 0) {
			// 当前所在组，这里不够数的一组依然算一组，并是按合并计算当前组
			int currentGroup = (offSetLessonId - 1 + GROUP_INTERVAL) / GROUP_INTERVAL;
			if (currentGroup == GROUP_COUNT + 1) {
				to = LAST_LESSON_ID;
			} else if (currentGroup == GROUP_COUNT) {
				to = LAST_LESSON_ID;
			}
		}
		return to+offSet;
	}*/
	
	/**
	 * 获得具体课文路径
	 * @param lessonId lessonId
	 * @return
	 *//*
	public static String getLessonPath(int lessonId){
//		//按分组计算出当前lesson是在哪一组
//		int from,to;
//		from = lessonId - (lessonId - 1) % GROUP_INTERVAL;
//		int delta = GROUP_INTERVAL - lessonId % GROUP_INTERVAL;
//		delta = delta == GROUP_INTERVAL ? 0 : delta;
//		to = lessonId + delta;
//		//不等分的情况下 对最后两组做特俗处理
//		if (LAST_LESSON_ID % GROUP_INTERVAL > 0) {
//			//当前所在组，这里不够数的一组依然算一组，并是按合并计算当前组
//			int currentGroup=(lessonId-1+GROUP_INTERVAL) / GROUP_INTERVAL;
//			if (currentGroup == GROUP_COUNT+1) {
//				from -= GROUP_INTERVAL;
//				to = LAST_LESSON_ID;
//			}else if(currentGroup == GROUP_COUNT){
//				to = LAST_LESSON_ID;
//			}
//		}
		return Constants.PATH_RES + "/djdry_1_" + calcCurrentGroupFirstId(lessonId) + "_" + calcCurrentGroupLastId(lessonId);
	}*/
	/** 计算课文在当前在分组中的位置*//*
	public static int calcLessonPositionInGroup(int lessonId){
		int from=calcCurrentGroupFirstId(lessonId);
		return lessonId-from;
	}*/
	/** 获得完整title */
	public String getTitleJpFull(){
		return "第"+id+"课"+":"+titleJp;
	}
	
	public int getId() {
		return id;
	}
	public String getTitleJp() {
		return titleJp;
	}
	public void setTitleJp(String titleJp) {
		this.titleJp = titleJp;
	}
	public List<Article> getArticles() {
		return articles;
	}
	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}
	public String getPath() {
		return path;
	}
	public String getDatFilePath() {
		return datFilePath;
	}

	public int getGroupFirstId() {
		return groupFirstId;
	}

	public int getGroupLastId() {
		return groupLastId;
	}

	public int getPositionInGroup() {
		return positionInGroup;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public int getGroupNextId() {
		return groupNextId;
	}

	public int getGroupPreId() {
		return groupPreId;
	}
	public int getVolumn() {
		return volumn;
	}
	
}
