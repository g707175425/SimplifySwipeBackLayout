package com.reactnativenavigation.layouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.reactnativenavigation.R;

public class SimplifySwipeBackLayout extends RelativeLayout {
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private static final int FULL_ALPHA = 255;

    private static final int DURATION = 500;

    private final Scroller scroller;

    private boolean mEnable = false;

    private Drawable mShadowLeft;

    private float mScrimOpacity;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private Rect mTmpRect = new Rect();

    private int edgeSize;
    private int lastX;
    private boolean finished = false;
    private SwipeListener swipeListener;

    public SimplifySwipeBackLayout(Context context) {
        this(context, null);
    }

    public SimplifySwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplifySwipeBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setShadow(getResources().getDrawable(R.drawable.screen_shadow_left));
        //默认边界大小
        edgeSize = (int) (20 * getContext().getResources().getDisplayMetrics().density + 0.5f);
        //初始化Scroller
        scroller = new Scroller(getContext());
        setWillNotDraw(false);
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }


    /**
     * 侧拉边缘距离
     */
    public void setEdgeSize(int size) {
        this.edgeSize = size;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mEnable && !finished){
            System.out.println("x:"+event.getX()+";rawX:"+event.getRawX());
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(event.getX() <= edgeSize){
                        //在边界
                        lastX = scroller.getCurrX();
                        return true;
                    }else {
                        return false;
                    }
                case MotionEvent.ACTION_MOVE:
                    scroller.startScroll(lastX,0, (int) (event.getX()-lastX),0,0);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    if(event.getX() > getWidth()/2){
                        scroller.startScroll(scroller.getCurrX(),0,getWidth()-scroller.getCurrX(),
                                0,DURATION);
                        //退出页面,故不再接受手势
                        finished = true;
                    }else {
                        scroller.startScroll(scroller.getCurrX(),0,-scroller.getCurrX(),
                                0,DURATION);
                    }
                    invalidate();
                    return true;
                default:
                    return false;
            }
        }else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mEnable)getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mEnable && ev.getX() < edgeSize || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if(scroller.computeScrollOffset()){
            mScrimOpacity = ((float)getWidth()-(float)scroller.getCurrX())/(float)getWidth();
            System.out.println("mScrimOpacity:"+mScrimOpacity);
            invalidate();
            if(finished && scroller.isFinished() && swipeListener != null){
                //结束
                swipeListener.onFinished();
            }
        }

        super.computeScroll();
    }

    /**
     * 设置自定义阴影图
     */
    public void setShadow(Drawable shadow) {
        mShadowLeft = shadow;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(scroller.getCurrX(),0);
        super.draw(canvas);
        drawShadow(canvas);
        drawScrim(canvas);
        canvas.restore();
    }

    private void drawScrim(Canvas canvas) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);

        canvas.clipRect(-getWidth(), 0, 0, getHeight());
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas) {
        final Rect childRect = mTmpRect;
        getHitRect(childRect);

        mShadowLeft.setBounds(- mShadowLeft.getIntrinsicWidth(), childRect.top,
                0, childRect.bottom);
        mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
        mShadowLeft.draw(canvas);

    }

    public void setSwipeListener(SwipeListener swipeListener){
        this.swipeListener = swipeListener;
    }

    public interface SwipeListener{
        /**
         * 页面滑动退出后监听,可以在此监听中进行activity,fragment,view的退出操作
         */
        void onFinished();
    }
}
