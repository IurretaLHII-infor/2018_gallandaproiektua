package com.example.sasiroot.agpr;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeablePager extends ViewPager {

    private static boolean enabled;

    public NonSwipeablePager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return enabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return enabled && super.onInterceptTouchEvent(event);
    }

    public static void setPagingEnabled(boolean enabledOn) {
        enabled = enabledOn;
    }

    public static boolean isPagingEnabled() {
        return enabled;
    }
}