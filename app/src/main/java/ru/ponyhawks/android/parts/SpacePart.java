package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;

/**
 * SPACE? SPAAACE?!
 * SPAAAAAAAAAAAAAAAAAAAAAAAAACE!
 * IM IN SPAAAAAAAAAAAAAAAAACEEEE
 * WOOOOOOOOOOOOOOOOOOEEEEEEHAAAA
 * SPAAAAAAAAAAACE!
 * Bored of space.
 *
 * @author cab404
 */
public class SpacePart implements ViewConverter<Integer> {
    @Override
    public void convert(View view, Integer data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        view.setLayoutParams(new AbsListView.LayoutParams(0, data));
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup parent, ChumrollAdapter adapter) {
        return new View(parent.getContext());
    }

    @Override
    public boolean enabled(Integer data, int index, ChumrollAdapter adapter) {
        return false;
    }
}
