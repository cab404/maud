package ru.everypony.maud.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 21:47 on 27/09/15
 *
 * @author cab404
 */
public class UpdateDrawable extends Drawable {
    public static final int TINT = 0x66ffffff;
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
    final Bitmap upd;
    final Rect size;
    final Rect dst = new Rect();
    final Rect dst2 = new Rect();

    int num = 0;

    boolean spinning = false;
    float dps = 360 * 2;
    float spin;
    long last = 0;
    int padding;


    @SuppressWarnings("ConstantConditions")
    public UpdateDrawable(Context context) {
        Context context1 = context;
        final float dp = context.getResources().getDisplayMetrics().density;
        padding = (int) (8 * dp);
        paint.setTextSize(8 * dp);
        paint.setTypeface(Typeface.MONOSPACE);

        upd = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_refresh);
        size = new Rect(0, 0, upd.getWidth(), upd.getHeight());

    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                invalidateSelf();
            }
        });
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
        invalidateSelf();
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

        canvas.save();

        int side = upd.getHeight() - padding * 2;
        Gravity.apply(Gravity.CENTER, side, side, getBounds(), dst);

        paint.setColor(num == 0 || spinning ? -1 : TINT);

        canvas.rotate(spin, dst.centerX(), dst.centerY());

        canvas.drawBitmap(upd, null, dst, paint);
        canvas.restore();

        if (num == 0) return;
        paint.setColor(spinning ? TINT : -1);
        String nm = num + "";
        paint.getTextBounds(nm, 0, nm.length(), dst2);
        canvas.drawText(nm, dst.centerX() - dst2.width() / 2, dst.centerY() + dst2.height() / 2, paint);

    }

    public void setPadding(int padding) {
        this.padding = padding;
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
