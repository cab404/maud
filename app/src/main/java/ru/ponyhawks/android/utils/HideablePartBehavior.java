package ru.ponyhawks.android.utils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Stivks view to bottom and handles collapse/expand operations.
 * <p/>
 * Created at 16:06 on 15/09/15
 *
 * @author cab404
 */
public class HideablePartBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    public void init(ViewGroup parent, View child) {
        dragHelper = ViewDragHelper.create(parent, callback);
        dragHelper.captureChildView(child, 0);
        lockOn(child);
    }

    public void lockOn(View child) {
        collapsedOffset = child.getBottom() - ((View) child.getParent()).getHeight();
    }

    enum State {
        EXPANDED, COLLAPSED, HIDDEN
    }

    ChangeCallback changeCallback;
    State state = State.COLLAPSED;
    boolean useForOffsetCalc = false;
    int collapsedOffset = -1;
    int lastTop;

    public void setChangeCallback(ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    ViewDragHelper dragHelper;
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            useForOffsetCalc = true;
            if (collapsedOffset == -1)
                lockOn(child);
            return true;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return ((View) child.getParent()).getHeight();
        }


        @Override
        public void onViewReleased(View view, float xvel, float yvel) {
            super.onViewReleased(view, xvel, yvel);
            final int height = ((View) view.getParent()).getHeight();
            int dst;
            switch (state) {
                case EXPANDED:
                    dst = (height - view.getHeight()) / 2;
                    if (lastTop - dst > view.getHeight() - yvel) {
                        useForOffsetCalc = true;
                        collapse(view);
                        break;
                    }
                    expand(view);
                    break;
                case COLLAPSED:
                    dst = height - view.getHeight() + collapsedOffset;
                    int delta = lastTop - dst;
                    if (delta > 0)
                        if (delta > view.getHeight() - yvel) {
                            hide(view);
                            break;
                        }

                    if (delta < 0)
                        if (-delta > view.getHeight() + yvel) {
                            useForOffsetCalc = true;
                            expand(view);
                            break;
                        }
                    collapse(view);

                    break;
                case HIDDEN:
                    hide(view);
                    break;
            }

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            lastTop = top;

            if (useForOffsetCalc && (state == State.EXPANDED || state == State.COLLAPSED)) {
                int expandLimit = calculateDst(State.EXPANDED, changedView);
                int collapseLimit = calculateDst(State.COLLAPSED, changedView);

                if (top <= collapseLimit && top >= expandLimit) {
                    float interpolation = (float) (top - expandLimit) / (collapseLimit - expandLimit);
                    changeCallback.onExpandCollapse(interpolation);
                }
            }

            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

    };

    public void collapse(View view) {
        useForOffsetCalc = true;
        if (dragHelper == null) return;
        final int dst = calculateDst(State.COLLAPSED, view);
        if (dragHelper.smoothSlideViewTo(view, view.getLeft(), dst)) {
            if (!started)
                ViewCompat.postOnAnimation(view, new SettleRunnable(view));
            if (state != State.COLLAPSED && changeCallback != null)
                changeCallback.onCollapse(view);
            state = State.COLLAPSED;
        }
    }

    public void expand(View view) {
        useForOffsetCalc = true;
        if (dragHelper == null) return;
        final int dst = calculateDst(State.EXPANDED, view);
        if (dragHelper.smoothSlideViewTo(view, view.getLeft(), dst)) {
            if (!started)
                ViewCompat.postOnAnimation(view, new SettleRunnable(view));
            if (state != State.EXPANDED && changeCallback != null)
                changeCallback.onExpand(view);
            state = State.EXPANDED;
        }
    }

    public void hide(View view) {
        useForOffsetCalc = true;
        if (dragHelper == null) return;
        final int dst = calculateDst(State.HIDDEN, view);
        if (dragHelper.smoothSlideViewTo(view, 0, dst)) {
            if (!started)
                ViewCompat.postOnAnimation(view, new SettleRunnable(view));
            if (state != State.HIDDEN && changeCallback != null)
                changeCallback.onHide(view);
            state = State.HIDDEN;
        }
    }

    public HideablePartBehavior() {
        super();
    }

    public HideablePartBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
//        collapsedOffset = attrs.getAttributeIntValue(null, "collapseOffset", -1);
    }

    public int calculateDst(State state, View view) {
        final int height = ((View) view.getParent()).getHeight();
        switch (state) {
            case HIDDEN:
                return height;
            case COLLAPSED:
                return height - view.getHeight() + collapsedOffset;
            case EXPANDED:
                return (height - view.getHeight()) / 2;
            default:
                throw new RuntimeException();
        }
    }

    public void syncImmediate(View child) {
        child.offsetTopAndBottom(calculateDst(state, child) - child.getTop());
        finish = true;
    }


    public void sync(View child) {
        useForOffsetCalc = true;
        switch (state) {
            case HIDDEN:
                hide(child);
                break;
            case COLLAPSED:
                collapse(child);
                break;
            case EXPANDED:
                expand(child);
                break;
        }
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, final V child, int layoutDirection) {
        child.offsetTopAndBottom(child.getTop() - lastTop);
        sync(child);
        return false;
    }

    boolean ignore = false;
    boolean scrollTransfered = false;

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (ignore)
            return false;

        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
            scrollTransfered = false;
        if (scrollTransfered)
            return false;
        if (checkOldNestedThings(child, ev)) {
            scrollTransfered = true;
            return false;
        }


        if (parent.isPointInChildBounds(child, (int) ev.getX(), (int) ev.getY())) {
            if (dragHelper == null)
                return false;
            return dragHelper.shouldInterceptTouchEvent(ev);
        } else
            return false;
    }

    Rect tmp = new Rect();

    boolean checkOldNestedThings(View view, MotionEvent ev) {
        view.getGlobalVisibleRect(tmp);
        if (!tmp.contains((int) ev.getRawX(), (int) ev.getRawY()))
            return false;

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                if (checkOldNestedThings(group.getChildAt(i), ev))
                    return true;
        } else {
            if (view instanceof EditText)
                if (((EditText) view).getLineCount() > 1)
                    return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (dragHelper == null) return false;
        dragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean blocksInteractionBelow(CoordinatorLayout parent, View child) {
        return true;
    }

    public interface ChangeCallback {
        void onHide(View view);

        void onExpand(View view);

        void onCollapse(View view);

        void onExpandCollapse(float state);
    }

    boolean started = false;
    boolean finish = false;

    class SettleRunnable implements Runnable {
        private final View view;
        private int startHeight;

        public SettleRunnable(View view) {
            this.view = view;
            startHeight = view.getHeight();
        }

        @Override
        public void run() {
            if (finish){
                finish = false;
                started = false;
                return;
            }
            started = true;
            if (startHeight != view.getHeight()) {
                dragHelper.smoothSlideViewTo(view, 0, calculateDst(state, view));
                startHeight = view.getHeight();
            }
            if (dragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(view, this);
            } else {
                started = false;
                useForOffsetCalc = false;
            }

        }
    }

}
