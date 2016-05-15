package ru.everypony.maud.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 18:21 on 05/12/15
 *
 * @author cab404
 */
public class AnimatedImage extends View {

    private Paint paint = new Paint();

    {
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        paint.setDither(true);
    }

    public AnimatedImage(Context context) {
        super(context);
    }

    public AnimatedImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.best_pony, outValue, true);
        drawable = (AnimationDrawable) context.getResources().getDrawable(outValue.resourceId);
    }

    AnimationDrawable drawable;

    public void setDrawable(AnimationDrawable drawable) {
        this.drawable = drawable;
    }

    Rect dst = new Rect();
    int frame_delay = 40;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        frame_delay = drawable.getDuration(0);
        int frame_index = (int)
                ((System.currentTimeMillis() % (frame_delay * drawable.getNumberOfFrames()))
                        /
                        frame_delay);
        final Bitmap frame = ((BitmapDrawable) drawable.getFrame(frame_index)).getBitmap();

        canvas.save();
        dst.set(0, 0, (int) (getHeight() * ((float) frame.getWidth() / frame.getHeight())), getHeight());
        dst.offset((getWidth() - dst.width()) / 2, 0);
        canvas.drawBitmap(frame, null, dst, paint);
        canvas.restore();
        invalidate();
    }
}
