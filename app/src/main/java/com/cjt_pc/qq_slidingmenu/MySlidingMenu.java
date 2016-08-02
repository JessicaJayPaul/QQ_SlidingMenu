package com.cjt_pc.qq_slidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by cjt-pc on 2015/12/5.
 * Email:879309896@qq.com
 */
public class MySlidingMenu extends HorizontalScrollView {

    /**
     * 最低触发菜单动画效果水平速率
     */
    public final static int MIN_VELOCITY = 500;

    // 三种侧滑菜单模式，依次是 普通、抽屉式、仿QQ
    public final static int NORMAL = 0;
    public final static int DRAWER = 1;
    public final static int QQ = 2;

    /**
     * 菜单透明度的最低程度，默认值是0.7
     */
    private float minAlpha = 0.7f;

    /**
     * 菜单布局占父布局的百分比，默认值是0.8
     */
    private float menuWidthRate = 0.8f;

    /**
     * 仿QQ菜单缩放视图比例，默认值是0.7
     */
    private float scaleRate = 0.7f;

    private ViewGroup leftMenu, rightContent;
    private boolean once = false;
    private int slidingMode;

    // 速度监控器
    private VelocityTracker mVelocityTracker;

    public MySlidingMenu(Context context) {
        this(context, null);
    }

    public MySlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取xml定义的属性（如果存在的话）
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MySlidingMenu);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.MySlidingMenu_menu_width_rate:
                    menuWidthRate = a.getFloat(attr, menuWidthRate);
                    break;
                case R.styleable.MySlidingMenu_sliding_mode:
                    // 获取侧滑模式，默认是普通侧滑模式
                    slidingMode = a.getInteger(attr, NORMAL);
                    break;
                case R.styleable.MySlidingMenu_menu_alpha:
                    minAlpha = a.getFloat(attr, minAlpha);
                    break;
                case R.styleable.MySlidingMenu_scale_rate:
                    scaleRate = a.getFloat(attr, scaleRate);
                    break;
            }
        }
        a.recycle();
    }

    /**
     * 重写onMeasure方法，自定义测量控件大小（不必调用super.onMeasure()，方法末尾须调用setMeasuredDimension()方法）
     *
     * @param widthMeasureSpec  建议宽度信息封装类
     * @param heightMeasureSpec 建议高度信息封装类
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (!once) {
            LinearLayout mSlidingLayout = (LinearLayout) getChildAt(0);
            leftMenu = (ViewGroup) mSlidingLayout.getChildAt(0);
            rightContent = (MyLinearLayout) mSlidingLayout.getChildAt(1);
            // 计算左菜单的宽度
            int leftMemuWidth = (int) (widthSize * menuWidthRate);
            // 首先测量mSlidingLayout的宽度，然后再测量其子View的宽度，避免对子View自身的测量造成影响
            mSlidingLayout.measure(MeasureSpec.makeMeasureSpec(widthSize + leftMemuWidth, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
            rightContent.measure(widthMeasureSpec, heightMeasureSpec);
            leftMenu.measure(MeasureSpec.makeMeasureSpec(leftMemuWidth, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
            once = true;
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            setAnmateViewPivot();
            scrollTo(leftMenu.getWidth(), 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        createVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                calScrollTo(getScrollVelocity());
                recycleVelocityTracker();
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 计算当前视图滑动方向
     *
     * @param velocityX 当前手指横向移动速度
     */
    public void calScrollTo(int velocityX) {
        if (getScrollX() >= leftMenu.getWidth() / 2) {
            if (velocityX > MIN_VELOCITY) {
                smoothScrollTo(0, 0);
            } else {
                smoothScrollTo(leftMenu.getWidth(), 0);
            }
        } else {
            if (velocityX < -MIN_VELOCITY) {
                smoothScrollTo(leftMenu.getWidth(), 0);
            } else {
                smoothScrollTo(0, 0);
            }
        }
    }

    /**
     * 视图滑动时触发此方法
     *
     * @param l    视图水平偏移量
     * @param t    视图垂直偏移量
     * @param oldl 原始视图左上角水平坐标
     * @param oldt 原始视图坐上小垂直坐标
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        switch (slidingMode) {
            case NORMAL:
                break;
            case DRAWER:
                drawerAnimator(l);
                break;
            case QQ:
                QQAnimator(l);
                break;
            default:
                break;
        }
    }

    /**
     * 抽屉动画
     *
     * @param offsetX 视图水平偏移量
     */
    public void drawerAnimator(int offsetX) {
        leftMenu.animate().translationX(offsetX).setDuration(0).start();
    }

    /**
     * 仿QQ侧滑动画
     *
     * @param offsetX 视图水平偏移量
     */
    public void QQAnimator(int offsetX) {
        float offRate = offsetX * 1.0f / leftMenu.getWidth();
        // 菜单动画
        float menuDegree = scaleRate + (1 - scaleRate) * (1 - offRate);
        float alphaDegree = minAlpha + (1 - minAlpha) * (1 - offRate);
        leftMenu.animate().scaleX(menuDegree).scaleY(menuDegree).alpha(alphaDegree).setDuration(0).start();
        // 内容动画
        float contentDegree = scaleRate + (1 - scaleRate) * offRate;
        rightContent.animate().scaleX(contentDegree).scaleY(contentDegree).setDuration(0).start();
    }

    /**
     * 设置需要缩放动画的缩放中心点
     */
    public void setAnmateViewPivot() {
        leftMenu.setPivotX(leftMenu.getWidth());
        leftMenu.setPivotY(leftMenu.getHeight() / 2);
        rightContent.setPivotX(0);
        rightContent.setPivotY(rightContent.getHeight() / 2);
    }

    /**
     * 初始化VelocityTracker对象，并将触摸滑动事件加入到VelocityTracker当中
     *
     * @param event 触摸滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        return (int) mVelocityTracker.getXVelocity();
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }
}