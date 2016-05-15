package ru.everypony.maud.parts;

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
    public void convert(View view, Void data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        onEndReached();
        this.adapter.remove(index);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        final View view = new View(parent.getContext());
        view.setLayoutParams(new AbsListView.LayoutParams(1,1));
        view.setVisibility(View.GONE);
        return view;
    }

    @Override
    public boolean enabled(Void data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
