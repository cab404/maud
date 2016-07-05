package ru.everypony.maud.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 06:35 on 05/07/16
 *
 * @author cab404
 */
public class IgnorantRelativeLayout extends RelativeLayout {

    public IgnorantRelativeLayout(Context context) {
        super(context);
    }

    public IgnorantRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IgnorantRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Rect rect = new Rect();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            getHitRect(rect);
            if (!rect.contains((int) event.getX(), (int) event.getY()))
                return false;
        }
        return super.onTouchEvent(event);
    }
}
