package ru.ponyhawks.android.text.changers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.widget.EditText;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:50 on 26/09/15
 *
 * @author cab404
 */
public abstract class SimpleTextChanger implements TextChanger {

    @Override
    public void onSelect(Fragment ctx, EditText text) {
        change(text.getSelectionStart(), text.getSelectionEnd(), text);
    }

    public abstract void change(int start, int end, EditText editable);

    @Override
    public void onConfigure(Context ctx) {}

    @Override
    public boolean configurable() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
