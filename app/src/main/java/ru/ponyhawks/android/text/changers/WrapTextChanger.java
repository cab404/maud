package ru.ponyhawks.android.text.changers;

import android.support.annotation.DrawableRes;
import android.text.Editable;
import android.text.Selection;
import android.widget.EditText;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 15:50 on 26/09/15
 *
 * @author cab404
 */
public class WrapTextChanger extends SimpleTextChanger {

    private final int id;
    private final String start;
    private final String end;

    public WrapTextChanger(@DrawableRes int id, String start, String end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    @Override
    public void change(int start, int end, EditText text) {
        Editable editable = text.getText();
        editable.insert(end < 0 ? 0 : end, this.end);
        editable.insert(start < 0 ? 0 : start, this.start);
        text.setText(editable);

        final int newStart = start + this.start.length();
        final int newEnd = end + this.start.length();
        if (newStart == newEnd)
            text.setSelection(newStart);
        else
            text.setSelection(newStart, newEnd);

    }

    @Override
    public int getImageResource() {
        return id;
    }
}
