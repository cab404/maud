package ru.ponyhawks.android.text;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.cab404.moonlight.parser.Tag;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 18:39 on 14/09/15
 *
 * @author cab404
 */
public class SpanImageListener extends SimpleImageLoadingListener {
    private final ImageSpan span;
    private final TextView target;
    private final Spannable builder;
    private int width;
    private int height;

    public SpanImageListener(TextView target, ImageSpan span, Spannable builder) {
        this.span = span;
        this.target = target;
        this.builder = builder;
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
        replace(new ImageSpan(target.getContext(), loadedImage));
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    }
}
