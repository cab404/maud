package ru.everypony.maud.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 05:30 on 05/07/16
 *
 * @author cab404
 */
public class SimpleButtonItem implements ViewConverter<SimpleButtonItem.ButtonData> {

    @Override
    public void convert(View view, ButtonData data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        ((TextView) view).setText(data.name);
        view.setOnClickListener(data.listener);
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return inflater.inflate(R.layout.part_simple_button, parent, false);
    }

    @Override
    public boolean enabled(ButtonData data, int index, ChumrollAdapter adapter) {
        return true;
    }

    public static class ButtonData {
        public String name;
        public View.OnClickListener listener;

        public ButtonData(String name, View.OnClickListener listener) {
            this.name = name;
            this.listener = listener;
        }
    }


}
