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
/**  
 * 仿Launcher中的WorkSapce，可以左右滑动切换屏幕的类  
 * @author Alkaid 
 */    
public class WorkSpace extends ViewGroup {
	/** 核心成员scroller 通过它计算滚动距离以及获得当前滚动坐标*/
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    /** 当前屏索引*/
    private int mCurScreen;
    private static int mDefaultScreen = 0;
    
    /** 触屏状态：不动*/
    private static final int TOUCH_STATE_REST = 0;
    /** 触屏状态：滚动*/
    private static final int TOUCH_STATE_SCROLLING = 1;
    /** 手指滑动速度临界值，超过该常量则切屏*/
    private static final int SNAP_VELOCITY = 600;
    /** 触屏状态*/
    private int mTouchState = TOUCH_STATE_REST;
//    private boolean move=false;
    /** 触屏移动距离临界值，
		超过该值则设置触屏状态mTouchState=TOUCH_STATE_SCROLLING,否则设为Rest，
		根据系统服务取得*/
    private int mTouchSlop;
    /** 上次触屏X坐标*/
    private float mLastMotionX;
    /** 上次触屏y坐标  该控件暂时用不到*/
//    private float mLastMotionY;
    /** 屏幕改变监听类 */
    private OnScreenChangedListenner mListenner;
    public WorkSpace(Context context, AttributeSet attrs) {    
        this(context, attrs, 0);    
    }    
    public WorkSpace(Context context, AttributeSet attrs, int defStyle) {    
        super(context, attrs, defStyle);    
        mScroller = new Scroller(context);    
        mCurScreen = mDefaultScreen;    
        //TODO 测试的时候发现太灵敏了 故*4 还没发现其他更好的方法 暂时这样改 待改进
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop()*4;    
    }
    /**
     * 设置显示位置，实现子视图横向排列布局
     */
    @Override    
    protected void onLayout(boolean changed, int l, int t, int r, int b) { 
        if (changed) {    
            int childLeft = 0;    
            final int childCount = getChildCount();    
            for (int i=0; i<childCount; i++) {    
                final View childView = getChildAt(i);    
                if (childView.getVisibility() != View.GONE) {    
                    final int childWidth = childView.getMeasuredWidth();    
                    childView.layout(childLeft, 0,     
                            childLeft+childWidth, childView.getMeasuredHeight());    
                    childLeft += childWidth;    
                }    
            }   
            setToScreen(mCurScreen);
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
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);       
        }       
        // Log.e(TAG, "moving to screen "+mCurScreen);       
//        scrollTo(mCurScreen * width, 0);             
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
     * 通过滑动距离和屏幕宽度计算应该定位在哪一屏，然后调用{@link #snapToScreen(int)}切屏<br/>
     * 是为了实现当屏幕拖动到一个位置松手后的处理<br/>
     * According to the position of current layout  
     * scroll to the destination page.  
     */    
    private void snapToDestination() {    
        final int screenWidth = getWidth();    
        final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;    
        snapToScreen(destScreen);    
    }    
        
    /**
     * 根据指指定屏幕索引切换屏幕
     * @param whichScreen 指定的屏幕索引
     */
    public void snapToScreen(int whichScreen) {    
        // get the valid layout page    
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));    
        if (getScrollX() != (whichScreen*getWidth())) {    
            final int delta = whichScreen*getWidth()-getScrollX();  
            //设定x轴和y轴的起点及移动的距离，调用该方法将执行滚动动画，默认动画时间是250毫秒
            mScroller.startScroll(getScrollX(), 0,     
                    delta, 0, Math.abs(delta)*2);   
            int preScreen=mCurScreen;
            mCurScreen = whichScreen;
            if(mListenner!=null && whichScreen!=preScreen)
            	mListenner.onChanged(preScreen, mCurScreen);
            invalidate();       // Redraw the layout    
        }    
    }
    
    /**
     * 根据指指定屏幕索引切换屏幕,无动画效果 光计算scrollOffSet
     * @param whichScreen 指定的屏幕索引
     */
    public void setToScreen(int whichScreen) {  
    	mCurScreen = whichScreen;    
    	if(getWidth()<=0){
    		return;
    	}
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));  
        mCurScreen = whichScreen; 
        scrollTo(whichScreen*getWidth(), 0);    
    }    
    /**
     * 获得当前屏幕索引
     * @return
     */
    public int getCurScreen() {    
        return mCurScreen;    
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
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {       
                // Fling enough to move left       
            	LogUtil.d("snap left");    
                snapToScreen(mCurScreen - 1);       
            } else if (velocityX < -SNAP_VELOCITY       
                    && mCurScreen < getChildCount() - 1) {       
                // Fling enough to move right       
            	LogUtil.d("snap right");    
                snapToScreen(mCurScreen + 1);       
            } else {       
                snapToDestination();       
            }       
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
    
    public void setOnScreenChangedListenner(OnScreenChangedListenner listenner){
    	this.mListenner=listenner;
    }
        
    /** 监听WorkSpace页面改变的Listenner */
    public static abstract interface OnScreenChangedListenner
    {
    	/**
    	 * 页面改变时回调
    	 * @param preScreen 上一个屏幕索引
    	 * @param currentScreen 当前屏幕索引
    	 */
    	public abstract void onChanged(int preScreen,int currentScreen);
    }
}    