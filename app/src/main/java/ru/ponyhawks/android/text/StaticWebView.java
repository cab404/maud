package ru.ponyhawks.android.text;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * View wrapper arounf HtmlRipper
 * Created at 16:07 on 14/09/15
 *
 * @author cab404
 */
public class StaticWebView extends LinearLayout {
    HtmlRipper boundRipper = new HtmlRipper(this);


    public StaticWebView(Context context) {
        super(context);
        parametrize(context);
        setOrientation(VERTICAL);
    }

    public StaticWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parametrize(context);
        setOrientation(VERTICAL);
        final String value = attrs.getAttributeValue(null, "text");
        if (value != null)
            setText(value);
    }

    void parametrize(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        boundRipper.loadImages = sp.getBoolean("loadImages", false);
        boundRipper.loadVideos = sp.getBoolean("loadVideos", false);
        boundRipper.textIsSelectable = sp.getBoolean("textSelectable", false);
    }

    public HtmlRipper getRipper(){
        return boundRipper;
    }

    public void setRipper(HtmlRipper ripper){
        ripper.changeLayout(this);
        this.boundRipper = ripper;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        boundRipper.layout();
    }

    public void setText(String text) {
        boundRipper.escape(text);
    }

    @Override
    protected void finalize() throws Throwable {
        boundRipper.destroy();
        super.finalize();
    }
}
