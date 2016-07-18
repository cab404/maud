package ru.everypony.maud.text;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 18:39 on 14/09/15
 *
 * @author cab404
 */
public class SpanImageListener extends SimpleImageLoadingListener implements Runnable {
    private final ImageSpan span;
    private final TextView target;
    private final Spannable builder;
    private final ImageSize size;
    private Bitmap loadedImage;

    public SpanImageListener(TextView target, ImageSpan span, Spannable builder, ImageSize size) {
        this.span = span;
        this.target = target;
        this.builder = builder;
        this.size = size;
    }

    boolean replace(ImageSpan by) {
        int start = builder.getSpanStart(span);
        int end = builder.getSpanEnd(span);

        if (start == -1)
            return true;

        builder.removeSpan(span);
        builder.setSpan(
                by,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        target.setText(builder);
        return true;
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        this.loadedImage = loadedImage;
        run();
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    }

    int retries = 5;
    @Override
    public void run() {
        if (target.getMeasuredWidth() == 0) {
            if (retries-- > 0)
                target.postDelayed(this, 50);
            return;
        }

        int width = size.getWidth();
        int height = size.getHeight();
        ImageSpan span = new ImageSpan(target.getContext(), loadedImage);

        if (width == -1 && height == -1){
            width = loadedImage.getWidth();
            height = loadedImage.getHeight();
        }
        if (width == -1)
            width = (int) (loadedImage.getWidth() / (loadedImage.getHeight() / (float) height));
        if (height == -1)
            height = (int) (loadedImage.getHeight() / (loadedImage.getWidth() / (float) width));

        if (width > target.getMeasuredWidth()) {
            float v = target.getMeasuredWidth() / (float) width;
            width *= v;
            height *= v;
        }

        span.getDrawable().setBounds(0, 0, width, height);
        replace(span);
    }
}
