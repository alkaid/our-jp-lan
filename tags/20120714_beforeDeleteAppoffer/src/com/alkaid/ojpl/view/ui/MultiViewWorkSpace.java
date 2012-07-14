package com.alkaid.ojpl.view.ui;    
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.alkaid.ojpl.common.LogUtil;
import com.alkaid.ojpl.view.ui.WorkSpace.OnScreenChangedListenner;
/**  
 * 仿Launcher中的WorkSapce，可以左右滑动切换屏幕的类,与WorkSpace不同的是，MultiViewWorkSpace支持一屏中容纳多个子视图
 * @author Alkaid 
 */    
public class MultiViewWorkSpace extends ViewGroup {
	/** 核心成员scroller 通过它计算滚动距离以及获得当前滚动坐标*/
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    /** 一个屏幕中第一个子视图索引  滑屏时最后屏幕的定位以该参数为计算标准*/
//    private int firstChildIndex;
    /** 屏幕中最后一个子视图索引  滑屏时最后屏幕的定位以该参数为计算标准*/
//    private int lastChildIndex;
    /** 子视图长度集合*/
    private int[] childWidth=null;
    /** 当前须要移动到X坐标的距离 */
    private int currentLeft=0;
    /** 子视图总长度*/
    private int totalWidth;
    /** 屏幕宽度*/
    private int screenWidth;
    /** 第一个超出屏幕右边界的子视图索引*/
//    private int outOfScreenChild=0;
    /** 初始子视图索引 */
    private int defaultChild;
    /** 所有子视图的左边界 */
    public int[] childLefts;
    
    /** 触屏状态：不动*/
    private static final int TOUCH_STATE_REST = 0;
    /** 触屏状态：滚动*/
    private static final int TOUCH_STATE_SCROLLING = 1;
    /** 手指滑动速度临界值，超过该常量则切屏*/
//    private static final int SNAP_VELOCITY = 600;
    /** 触屏状态*/
    private int mTouchState = TOUCH_STATE_REST;
//    private boolean move=false;
    /** 触屏移动距离临界值，
		超过该值则设置触屏状态mTouchState=TOUCH_STATE_SCROLLING,否则设为Rest，
		根据系统服务取得*/
    private int mTouchSlop;
    /** 上次触屏X坐标*/
    private float mLastMotionX;
	private OnScreenChangedListenner mListenner;
    /** 上次触屏y坐标  该控件暂时用不到*/
//    private float mLastMotionY;
    /** 屏幕改变监听类 */
//    private OnScreenChangedListenner mListenner;
    public MultiViewWorkSpace(Context context, AttributeSet attrs) {    
        this(context, attrs, 0);    
    }    
    public MultiViewWorkSpace(Context context, AttributeSet attrs, int defStyle) {    
        super(context, attrs, defStyle);    
        mScroller = new Scroller(context);  
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); 
    }
    /**
     * 设置显示位置，实现子视图横向排列布局
     */
    @Override    
    protected void onLayout(boolean changed, int l, int t, int r, int b) {    
        if (changed) {  
        	LogUtil.i("MultiViewWorkSpace onLayout changed!");
//        	outOfScreenChild=0;
            int childLeft = 0;    
            final int childCount = getChildCount();    
            childWidth=new int[childCount];
            childLefts=new int[childCount];
            for (int i=0; i<childCount; i++) {    
                final View childView = getChildAt(i);    
                if (childView.getVisibility() != View.GONE) { 
                	childLefts[i]=childLeft;
                    final int childWidth = childView.getMeasuredWidth();
                    this.childWidth[i]=childWidth;
                    childView.layout(childLeft, 0,     
                            childLeft+childWidth, childView.getMeasuredHeight());    
                    childLeft += childWidth;    
//                    if(outOfScreenChild<=0 && childLeft/screenWidth>0){
//                    	outOfScreenChild=i;
//                    }
                }    
            }    
            totalWidth=childLeft;
            setToScreen(this.defaultChild);
        }    
    }    
    /**
     * 设置workspace的尺寸，注意传进来的LayoutParam必须是精确值，否则抛出IllegalStateException异常
     */
    @Override      
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {       
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
        final int width = MeasureSpec.getSize(widthMeasureSpec);  
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);       
        if (widthMode != MeasureSpec.EXACTLY) {       
            throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");     
        }       
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);       
        if (heightMode != MeasureSpec.EXACTLY) {       
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");    
        }       
        // The children are given the same width and height as the scrollLayout       
        final int count = getChildCount();       
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(getChildAt(i).getMeasuredWidth(), heightMeasureSpec);       
        }       
        // Log.e(TAG, "moving to screen "+mCurScreen);   
        screenWidth=width;
    }
    
    /**
     * 计算当前应该滑动到的X坐标位置
     */
    private void calcCurrentLeft(){
    	//3种情况
    	//1.子视图总长度小于等于当前屏幕宽度，松手后将以所有视图中的第一个子视图左边界为屏幕左边界
    	//2.松手后所有子视图中的最后一个子视图的右边界小于屏幕宽度，则屏幕右边界确定为最后视图的右边界，以此来计算左边界
    	//3.松手后当前屏幕中的第一个子视图设为a,若a宽度的一半以上在屏幕左边界以外，则firstChildIndex=a+1,否则firstChildIndex=a;
    	if(totalWidth<=screenWidth || getScrollX()<=0){
    		currentLeft=0;
    	}else if(totalWidth - getScrollX() <= screenWidth){
    		currentLeft=totalWidth- screenWidth;
    	}else{
    		for(int i=0;i<childLefts.length;i++){
    			if(getScrollX()>childLefts[i]){
    				if(getScrollX()-childLefts[i]>=childWidth[i+1]/2){
    					currentLeft=childLefts[i]+childWidth[i+1];
    				}else{
    					currentLeft=childLefts[i];
    				}
    				break;
    			}
    		}
    	}
    }
    
    public int getChildX(int whichChild){
    	return childLefts[whichChild]-getScrollX();
    }
    /**
     * 重写了父类的dispatchDraw();
     * 主要功能是判断抽屉是否打开、绘制指定的屏幕，可以绘制当前一屏，也可以绘制当前屏幕和下一屏幕，也可以绘制所有的屏幕
     * 这儿的绘制指显示屏幕上的child(例如：app、folder、Wiget)。和 computeScroll()中的setCurrentScreen(mCurrentScreen);方法配合使用可以实现屏幕的拖动多少显示多少的功能。
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
//    	if (this.move)
//        {
//          for(int j=0;j<getChildCount();j++){
//        	  boolean bool = drawChild(canvas, getChildAt(j), getDrawingTime() * 3L);
//          }
//        }
    	super.dispatchDraw(canvas);
    }
        
    /**  
     * 通过滑动距离和屏幕宽度计算定位坐标<br/>
     * 是为了实现当屏幕拖动到一个位置松手后的处理<br/>
     * According to the position of current layout  
     * scroll to the destination page.  
     */    
    private void snapToDestination() {
    	calcCurrentLeft();
        if(getScrollX()!=currentLeft){
        	int delta = currentLeft-getScrollX();  
        	mScroller.startScroll(getScrollX(), 0,     
                    delta, 0, Math.abs(delta)*2); 
        	invalidate();
        }
    }
        
    /**
     * 根据指指定子视图索引切换屏幕
     * @param whichChild 指定的子视图索引
     */
    public void snapToScreen(int whichChild) {
    	int viewLeft=childLefts[whichChild];
    	int viewRight=childLefts[whichChild]+childWidth[whichChild];
    	int delta=0;
    	if(viewLeft<getScrollX()){
    		delta=viewLeft-getScrollX();
    		mScroller.startScroll(getScrollX(), 0,     
                    delta, 0, Math.abs(delta)*2); 
    		invalidate();
    	}else if(viewRight>getScrollX()+screenWidth){
    		delta=viewRight-getScrollX()-screenWidth;
    		mScroller.startScroll(getScrollX(), 0,     
                    delta, 0, Math.abs(delta)*2); 
    		invalidate();
    	}
    }
    
    /**
     * 根据指指定子视图索引切换屏幕,无动画效果 光计算scrollOffSet
     * @param whichChild 指定的屏幕索引
     */
    public void setToScreen(int whichChild) { 
    	this.defaultChild=whichChild;
    	if(null==childLefts)
    		return;
    	int viewLeft=childLefts[whichChild];
    	int viewRight=childLefts[whichChild]+childWidth[whichChild];
    	if(viewLeft<getScrollX()){
    		scrollTo(viewLeft, 0);
    	}else if(viewRight>getScrollX()+screenWidth){
    		scrollTo(viewRight-screenWidth,0);
    	}
    }    
    /**
     * 要实现滑屏必须覆盖父类的该方法
     * 由{@link android.widget.Scroller#computeScrollOffset()}判断是否已经移动到指定位置<br/>
     * 用于移动过程中调用，如果未到位将调用{@link #scrollTo(int, int)}继续移动，产生动画移动的效果
     */
    @Override    
    public void computeScroll() {    
        if (mScroller.computeScrollOffset()) {    
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());    
            postInvalidate();    
        }    
    }
    /**
     * 实现UI滑屏效果
     */
    @Override    
    public boolean onTouchEvent(MotionEvent event) {    
        if (mVelocityTracker == null) {    
            mVelocityTracker = VelocityTracker.obtain();    
        }    
        mVelocityTracker.addMovement(event);    
            
        final int action = event.getAction();    
        final float x = event.getX();    
//        final float y = event.getY();    
            
        switch (action) {    
        case MotionEvent.ACTION_DOWN:    
            LogUtil.d("event down!");    
            if (!mScroller.isFinished()){
            	//停止动画，那么移动屏幕也会中止，用的场合是轻扫手势后，又做了按下的手势
                mScroller.abortAnimation();    
            }    
            mLastMotionX = x;    
            break;    
                
        case MotionEvent.ACTION_MOVE:    
            int deltaX = (int)(mLastMotionX - x);    
            mLastMotionX = x; 
            //滑动视图
            scrollBy(deltaX, 0);    
            break;    
                
        case MotionEvent.ACTION_UP:    
        	LogUtil.d("event : up");    
        	//计算滑动速度
            // if (mTouchState == TOUCH_STATE_SCROLLING) {       
            final VelocityTracker velocityTracker = mVelocityTracker;       
            velocityTracker.computeCurrentVelocity(1000);       
            int velocityX = (int) velocityTracker.getXVelocity();       
            LogUtil.d("velocityX:"+velocityX);     
            /**3种情况
             * 1.滑动速度大于临界值，且方向左(<0),则切到左屏幕
             * 2.滑动速度大于临界值，切方向右(>0),则切到右屏幕
             * 3.滑动速度小于临界值，则根据滑动距离由{@link #snapToDestination()}函数自动切屏
             */
//            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {       
//                // Fling enough to move left       
//            	LogUtil.d("snap left");    
//                snapToScreen(mCurScreen - 1);       
//            } else if (velocityX < -SNAP_VELOCITY       
//                    && mCurScreen < getChildCount() - 1) {       
//                // Fling enough to move right       
//            	LogUtil.d("snap right");    
//                snapToScreen(mCurScreen + 1);       
//            } else {       
                snapToDestination();       
//            }       
            if (mVelocityTracker != null) {       
                mVelocityTracker.recycle();       
                mVelocityTracker = null;       
            }       
            // }       
            mTouchState = TOUCH_STATE_REST;       
            break;    
        case MotionEvent.ACTION_CANCEL:    
            mTouchState = TOUCH_STATE_REST;    
            break;    
        }    
            
        return true;    
    }
    /** 
     * {@link #onTouchEvent(MotionEvent)}的前置事件<br/>
     * 这里主要是做手势判断，记录是否应该移动的状态{@link #mTouchState}，记录坐标值({@link #mLastMotionX},{@link #mLastMotionY})<br/>
     * 也可以不这么做，用{@link android.view.GestureDetector} 来实现 
     */
    @Override    
    public boolean onInterceptTouchEvent(MotionEvent ev) {    
    	LogUtil.d("onInterceptTouchEvent-slop:"+mTouchSlop);    
            
        final int action = ev.getAction();    
        if ((action == MotionEvent.ACTION_MOVE) &&     
                (mTouchState != TOUCH_STATE_REST)) {    
            return true;    
        }    
            
        final float x = ev.getX();    
//        final float y = ev.getY();    
            
        switch (action) {    
        case MotionEvent.ACTION_MOVE:    
            final int xDiff = (int)Math.abs(mLastMotionX-x);    
            if (xDiff>mTouchSlop) {    
                mTouchState = TOUCH_STATE_SCROLLING;    
//                move=true;    
            }    
            break;    
                
        case MotionEvent.ACTION_DOWN:    
            mLastMotionX = x;    
//            mLastMotionY = y;    
            mTouchState = mScroller.isFinished()? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;    
            break;    
                
        case MotionEvent.ACTION_CANCEL:    
        case MotionEvent.ACTION_UP:    
            mTouchState = TOUCH_STATE_REST;  
//            move=false;
            break;    
        }    
            
        return mTouchState != TOUCH_STATE_REST;    
    }  
    
//    public void setOnSnapListenner(OnScreenChangedListenner listenner){
//    	this.mListenner=listenner;
//    }
        
    /** 监听WorkSpace页面改变的Listenner */
    public static abstract interface OnSnapListenner
    {
    	/**
    	 * 页面滑动时回调
    	 * @param preScreen 上一个屏幕索引
    	 * @param currentScreen 当前屏幕索引
    	 */
    	public abstract void onSnap(int snapX);
    }
    
}    