package ru.everypony.maud.text.changers;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;

import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:12 on 20/10/15
 *
 * @author cab404
 */
public abstract class RequestTextChanger implements TextChanger {

    int ss, se;

    public static void insert(EditText ed, String wrapping, int ss, int se, String param) {
        if (ss < 0) ss = -1;
        if (se < 0) se = ss;

        final Editable text = ed.getText();


        String ready = wrapping
                .replace("%", param);

        int insi = ready.indexOf("$");

        ready = ready
                .replace("$", text.subSequence(ss, se));

        text.replace(ss, se, ready);

        if (insi == -1)
            if (ss != se)
                ed.setSelection(ss, ss + ready.length());
            else
                ed.setSelection(ss);
        else
            if (ss != se)
                ed.setSelection(insi, insi + se - ss);
            else
                ed.setSelection(insi);
    }

    @Override
    @SuppressLint("NewApi")
    public void onSelect(Fragment ctx, final EditText text) {
        final EditText windowET = new EditText(ctx.getActivity());
        ss = text.getSelectionStart();
        se = text.getSelectionEnd();

        windowET.setHint(R.string.enter_image_url);
        windowET.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

        final ClipboardManager cbm =
                (ClipboardManager)
                        ctx.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        @SuppressWarnings("deprecation")
        CharSequence clipboard = cbm.getText();

        if (clipboard != null) {
            try {
                new URL(clipboard.toString());
            } catch (MalformedURLException e) {
                clipboard = "";
            }
        }
        //noinspection ResourceType
        windowET.setHint(getHint());
        windowET.setText(clipboard);

        new android.app.AlertDialog.Builder(ctx.getActivity())
                .setView(windowET)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleText(text, ss, se, windowET.getText().toString());
                    }
                }).show();
    }

    public abstract void handleText(EditText text, int ss, int se, String s);

    @DrawableRes
    public abstract int getHint();

    @Override
    public void onConfigure(Context ctx) {
    }

    @Override
    public boolean configurable() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
