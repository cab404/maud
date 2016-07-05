package ru.everypony.maud.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:20 on 16/09/15
 *
 * @author cab404
 */
public class IgnorantCoordinatorLayout extends CoordinatorLayout {
    public IgnorantCoordinatorLayout(Context context) {
        super(context);
    }

    public IgnorantCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    boolean shouldLayout = true;

    ArrayList<Integer> heights = new ArrayList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!shouldLayout) {
            heights.clear();
            for (int i = 0; i < getChildCount(); i++)
                heights.add(getChildAt(i).getTop());
            super.onLayout(changed, l, t, r, b);
            for (int i = 0; i < getChildCount(); i++)
                getChildAt(i).offsetTopAndBottom(heights.get(i) - getChildAt(i).getTop());
        } else
            super.onLayout(changed, l, t, r, b);

        shouldLayout = false;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh > h)
            shouldLayout = true;
        super.onSizeChanged(w, h, oldw, oldh);
        if (resizeCallback != null)
            resizeCallback.onSizeChanged(w, h, oldw, oldh);

    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        for (int i = 0; i < getChildCount(); i++)
            if (getChildAt(i).onTouchEvent(ev))
                return super.onTouchEvent(ev);
        return false;
    }

    ResizeCallback resizeCallback;

    public interface ResizeCallback {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }

    public ResizeCallback getResizeCallback() {
        return resizeCallback;
    }

    public void setResizeCallback(ResizeCallback resizeCallback) {
        this.resizeCallback = resizeCallback;
    }
}
