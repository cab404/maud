package ru.ponyhawks.android.utils;

import android.view.View;

/**
 * Sorry for no comments!
 * Created at 6:14 on 05.02.15
 *
 * @author cab404
 */
public abstract class DoubleClickListener implements View.OnClickListener {
    long last_clicked = 0;

    byte num_clicked = 0;

    static final byte num_to_click = 2;
    static final int max_delay = 250;


    @Override
    public void onClick(View v) {
        long c_time = System.currentTimeMillis();
        if (num_clicked == 0)
            v.postDelayed(cSingleClick = new SingleClickCame(v), max_delay);

        if (last_clicked + max_delay > c_time) {
            num_clicked++;
            stopIt();
        } else
            num_clicked = 1;

        last_clicked = c_time;

        if (num_clicked == num_to_click) {
            stopIt();
            num_clicked = 0;
            act(v);
        }

    }

    private void stopIt() {
        if (cSingleClick != null) {
            cSingleClick.stopIt = true;
            cSingleClick = null;
        }
    }

    private SingleClickCame cSingleClick;

    private class SingleClickCame implements Runnable {
        private final View v;
        boolean stopIt = false;

        public SingleClickCame(View v) {
            this.v = v;
        }

        @Override
        public void run() {
            if (stopIt) return;
            num_clicked = 0;
            stopIt();
            actOnSingleClick(v);
        }
    }

    protected void actOnSingleClick(View v) {
    }

    public abstract void act(View v);

}
