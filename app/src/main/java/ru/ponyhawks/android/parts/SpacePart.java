package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

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
    public void convert(View view, Integer data, int index, ViewGroup parent) {
        view.setLayoutParams(new AbsListView.LayoutParams(0, data));
    }

    @Override
    public View createView(ViewGroup parent, LayoutInflater inflater) {
        return new View(parent.getContext());
    }

    @Override
    public boolean enabled(Integer data, int index) {
        return false;
    }
}
