package ru.everypony.maud.text.changers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.widget.EditText;

import com.cab404.moonlight.parser.Tag;

import ru.everypony.maud.R;
import ru.everypony.maud.utils.ImageChooser;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 19:03 on 26/09/15
 *
 * @author cab404
 */
public class ImportImageTextChanger implements TextChanger, ImageChooser.ImageUrlHandler, ImageChooser.StartForResultMethod {
    private ImageChooser chooser;
    private Fragment fr;
    private EditText text;

    @Override
    public int getImageResource() {
        return R.drawable.ic_image;
    }

    @Override
    public void onSelect(Fragment fr, EditText text) {
        this.fr = fr;
        this.text = text;
        chooser = new ImageChooser(this, fr.getActivity(), this);
        chooser.requestImageSelection(false);
    }

    @Override
    public void onConfigure(Context ctx) {

    }

    @Override
    public boolean configurable() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        chooser.handleActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void handleImage(final String image) {
        text.post(new Runnable() {
            @Override
            public void run() {
                int cursor = text.getSelectionStart();
                if (cursor == -1) cursor = 0;

                final Tag tag = new Tag();
                tag.name = "img";
                tag.props.put("src", image);
                tag.type = Tag.Type.STANDALONE;
                final Editable editable = text.getText();
                final String img = tag.toString();
                editable.insert(cursor, img);

                text.setText(editable);
                text.setSelection(cursor, cursor + img.length());
            }
        });
    }

    @Override
    public void startActivityForResult(Intent intent, int code) {
        fr.startActivityForResult(intent, code);
    }

    @Override
    public void finish() {
        fr.getActivity().finish();
    }
}
