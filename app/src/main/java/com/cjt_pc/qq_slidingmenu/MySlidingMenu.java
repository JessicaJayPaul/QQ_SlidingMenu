package com.cjt_pc.qq_slidingmenu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by cjt-pc on 2015/12/5.
 * Email:879309896@qq.com
 */
public class MySlidingMenu extends HorizontalScrollView {

    // 三种侧滑菜单模式，依次是 普通、抽屉式、仿QQ
    public final static int NORMOAL = 0;
    public final static int DRAWER = 1;
    public final static int QQ = 2;

    private ViewGroup leftMenu, rightContent;
    private boolean once = false;
    private int mScreenWidth;
    private int mMenuRightPadding;
    private int slidingMode;

    public MySlidingMenu(Context context) {
        this(context, null);
    }

    public MySlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MySlidingMenu);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.MySlidingMenu_menu_right_padding:
                    // 获取菜单right_padding，默认为50dp
                    mMenuRightPadding = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
                    break;
                case R.styleable.MySlidingMenu_sliding_mode:
                    slidingMode = a.getInteger(attr, 0);
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!once) {
            LinearLayout mSlidingLayout = (LinearLayout) getChildAt(0);
            leftMenu = (ViewGroup) mSlidingLayout.getChildAt(0);
            rightContent = (ViewGroup) mSlidingLayout.getChildAt(1);
            leftMenu.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
            rightContent.getLayoutParams().width = mScreenWidth;
            once = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            scrollTo(leftMenu.getLayoutParams().width, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (getScrollX() >= leftMenu.getLayoutParams().width / 2) {
                    smoothScrollTo(leftMenu.getLayoutParams().width, 0);
                } else {
                    smoothScrollTo(0, 0);
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (slidingMode == NORMOAL) {
            return;
        } else if (slidingMode == DRAWER) {
            ObjectAnimator menuTranlationAnimator = ObjectAnimator.ofFloat(leftMenu, "translationX", l);
            menuTranlationAnimator.setDuration(0);
            menuTranlationAnimator.start();
            return;
        }
        float scale = l * 1.0f / leftMenu.getWidth();
        // 菜单动画
        ObjectAnimator leftMenuScaleAnimatorX = ObjectAnimator.ofFloat(leftMenu, "scaleX", 1f, 0.7f + 0.3f * (1 - scale));
        ObjectAnimator leftMenuScaleAnimatorY = ObjectAnimator.ofFloat(leftMenu, "scaleY", 1f, 0.7f + 0.3f * (1 - scale));
        ObjectAnimator menuAlphaAnimator = ObjectAnimator.ofFloat(leftMenu, "alpha", leftMenu.getAlpha(), 0.7f + 0.3f * (1 - scale));
        leftMenu.setPivotX(0);
        leftMenu.setPivotY(leftMenu.getHeight() / 2);
        ObjectAnimator menuTranlationAnimator = ObjectAnimator.ofFloat(leftMenu, "translationX", (int) ((0.3 * scale + 0.7) * l));
        AnimatorSet menuSet = new AnimatorSet();
        menuSet.setDuration(0);
        menuSet.play(leftMenuScaleAnimatorY).with(menuAlphaAnimator).with(menuTranlationAnimator).with(leftMenuScaleAnimatorX);
        menuSet.start();
        // 内容动画
        ObjectAnimator contentScaleAnimatorY = ObjectAnimator.ofFloat(rightContent, "scaleY", 1f, 0.7f + 0.3f * scale);
        ObjectAnimator contentScaleAnimatorX = ObjectAnimator.ofFloat(rightContent, "scaleX", 1f, 0.7f + 0.3f * scale);
        rightContent.setPivotX(0);
        rightContent.setPivotY(rightContent.getHeight() / 2);
        AnimatorSet contentSet = new AnimatorSet();
        contentSet.play(contentScaleAnimatorY).with(contentScaleAnimatorX);
        contentSet.setDuration(0);
        contentSet.start();
    }
}
