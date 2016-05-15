package ru.everypony.maud.parts;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:51 on 14/09/15
 *
 * @author cab404
 */
public class MoonlitPart<A> implements ViewConverter<A> {

    public interface OnDataClickListener<A> {
        void onClick(A data, View view);
    }

    OnDataClickListener<A> onDataClickListener;

    public OnDataClickListener<A> getOnDataClickListener() {
        return onDataClickListener;
    }

    public void setOnDataClickListener(OnDataClickListener<A> onDataClickListener) {
        this.onDataClickListener = onDataClickListener;
    }

    @LayoutRes
    public int getLayoutId() {
        return -1;
    }

    @Override
    public void convert(View view, final A data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDataClickListener != null)
                    onDataClickListener.onClick(data, v);
            }
        });
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return inflater.inflate(getLayoutId(), parent, false);
    }

    @Override
    public boolean enabled(A data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
