package ru.everypony.maud.text.changers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.EditText;

/**
 * Text editor plugin
 * <p/>
 * Created at 07:25 on 26/09/15
 *
 * @author cab404
 */
public interface TextChanger {
    int getImageResource();

    void onSelect(Fragment ctx, EditText text);

    void onConfigure(Context ctx);

    boolean configurable();

    void onActivityResult(int requestCode, int resultCode, Intent data);
}
