package ru.ponyhawks.android.text;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 16:07 on 14/09/15
 *
 * @author cab404
 */
public class StaticWebView extends LinearLayout {
    HtmlRipper boundRipper = new HtmlRipper(this);

    public StaticWebView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public StaticWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final String value = attrs.getAttributeValue(null, "text");
        if (value != null)
            setText(value);
        setOrientation(VERTICAL);
    }

    public void setText(String text){
        boundRipper.escape(text);
    }

    @Override
    protected void finalize() throws Throwable {
        boundRipper.destroy();
        super.finalize();
    }
}
