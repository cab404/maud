package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:59 on 14/10/15
 *
 * @author cab404
 */
public abstract class ContinuationPart implements ViewConverter<Void> {

    private final ChumrollAdapter adapter;
    public abstract void onEndReached();

    public ContinuationPart(ChumrollAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void convert(View view, Void data, int index, ViewGroup parent) {
        onEndReached();
        adapter.remove(index);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View createView(ViewGroup parent, LayoutInflater inflater) {
        final View view = new View(parent.getContext());
        view.setLayoutParams(new AbsListView.LayoutParams(1,100));
        return view;
    }

    @Override
    public boolean enabled(Void data, int index) {
        return false;
    }
}
