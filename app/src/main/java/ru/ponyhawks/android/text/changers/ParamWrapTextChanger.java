package ru.ponyhawks.android.text.changers;

import android.widget.EditText;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 12:20 on 20/10/15
 *
 * @author cab404
 */
public class ParamWrapTextChanger extends RequestTextChanger {
    private final String start;
    private final int icon_id;
    private final int hint_id;

    public ParamWrapTextChanger(String start, int icon_id, int hint_id) {
        this.start = start;
        this.icon_id = icon_id;
        this.hint_id = hint_id;
    }

    @Override
    public void handleText(EditText text, int ss, int se, String s) {
        insert(text, start, ss, se, s);
    }

    @Override
    public int getHint() {
        return hint_id;
    }

    @Override
    public int getImageResource() {
        return icon_id;
    }
}
