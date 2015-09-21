package ru.ponyhawks.android.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 01:00 on 22/09/15
 *
 * @author cab404
 */
public class UpdateView extends View {
    Drawable refreshCircle;
    boolean spinning = true;
    float dps = 360;
    float spin;
    private Paint paint = new Paint();

    public UpdateView(Context context) {
        super(context);
        refreshCircle = context.getResources().getDrawable(R.drawable.ic_refresh);
    }

    long last = 0;

    Matrix matrix = new Matrix();

    @Override
    public void onDraw(Canvas canvas) {
        long ctime = System.currentTimeMillis();

        if (last == 0) last = ctime;
        spin += dps * (ctime - last) / 1000;
        spin %= 360;
        last = ctime;

        matrix.setRotate(spin);
//        canvas.setMatrix(matrix);

        refreshCircle.draw(canvas);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(Math.abs(spin - 180) / 10 + 10, 20, 10, paint);
        if (spinning)
            invalidate();
    }

}
