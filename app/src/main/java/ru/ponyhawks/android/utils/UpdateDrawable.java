package ru.ponyhawks.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.view.MenuItem;
import android.view.View;

import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 21:47 on 27/09/15
 *
 * @author cab404
 */
public class UpdateDrawable extends Drawable {
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
    final Bitmap upd;
    final Rect size;
    final Rect dst = new Rect();
    int num = 0;

    boolean spinning = false;
    float dps = 360 * 2;
    float spin;
    long last = 0;
    int padding;


    @SuppressWarnings("ConstantConditions")
    public UpdateDrawable(Context context) {
        upd = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_refresh);
        size = new Rect(0, 0, upd.getWidth(), upd.getHeight());

        final float dp = context.getResources().getDisplayMetrics().density;
        padding = (int) (6 * dp);
        paint.setTextSize(8 * dp);
        paint.setTypeface(Typeface.MONOSPACE);
    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    public void setMenuIcon(final MenuItem item, final View contentFrame) {
        ViewCompat.postOnAnimation(contentFrame, new Runnable() {
            @Override
            public void run() {
                item.setIcon(UpdateDrawable.this);
                if (item.isVisible() && spinning)
                    ViewCompat.postOnAnimation(contentFrame, this);
            }
        });
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public void draw(Canvas canvas) {
        long ctime = System.currentTimeMillis();

        if (spinning) {
            invalidateSelf();
            if (last == 0) last = ctime;
            spin += (dps * (ctime - last) / 1000);
            spin %= 360;
            last = ctime;
        } else last = 0;

        canvas.rotate(spin);

        paint.setColor(num == 0 || spinning ? -1 : 0x66ffffff);
        dst.set(size);
        dst.right -= padding * 2;
        dst.bottom -= padding * 2;
        dst.offset(dst.width() / -2, dst.height() / -2);
        canvas.drawBitmap(upd, size, dst, paint);


        if (num == 0) return;
        canvas.rotate(-spin);
        paint.setColor(spinning ? 0x66ffffff : -1);
        String nm = num + "";
        paint.getTextBounds(nm, 0, nm.length(), dst);
        canvas.drawText(nm, dst.width() / -2, dst.height() / 2, paint);

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
