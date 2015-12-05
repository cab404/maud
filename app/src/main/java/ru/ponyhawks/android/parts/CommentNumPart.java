package ru.ponyhawks.android.parts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;

import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:32 on 17/09/15
 *
 * @author cab404
 */
public class CommentNumPart extends MoonlitPart<Integer> {

    @Override
    public void convert(View view, Integer data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        super.convert(view, data, index, parent, adapter);
        ((TextView) view)
                .setText(
                        data + " " + view.getContext().getResources().getQuantityString(
                                R.plurals.comment_num, data
                        )
                );
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_label;
    }
}
