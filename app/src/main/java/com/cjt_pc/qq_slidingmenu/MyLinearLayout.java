package com.cjt_pc.qq_slidingmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

class MyLinearLayout extends LinearLayout implements View.OnClickListener {

    /**
     * 侧滑菜单ViewGroup
     */
    private MySlidingMenu mySlidingMenu;

    /**
     * 菜单视图的宽度
     */
    private int menuWidth;

    /**
     * 当前内容布局是否拦截事件
     */
    private boolean intercept = false;

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 在构造方法后面初始化mySlidingMenu和menuWidth，避免父布局为null或者多次加载
        if (changed){
            mySlidingMenu = (MySlidingMenu) (getParent().getParent());
            ViewGroup menu = (ViewGroup) ((ViewGroup) (mySlidingMenu.getChildAt(0))).getChildAt(0);
            menuWidth = menu.getWidth();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 若内容视图非完全显示，设置需要拦截
        intercept = mySlidingMenu.getScrollX() != menuWidth;
        return intercept;
    }

    @Override
    public void onClick(View view) {
        // 若需要拦截，则设置点击事件
        if (intercept) {
            mySlidingMenu.smoothScrollTo(menuWidth, 0);
        }
    }
}