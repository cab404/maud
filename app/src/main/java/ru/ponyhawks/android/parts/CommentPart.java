package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ViewConverter;
import com.cab404.libph.data.Comment;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:26 on 14/09/15
 *
 * @author cab404
 */
public class CommentPart extends MoonlitPart<Comment> {
    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.text)
    TextView text;

    @Override
    public void convert(View view, Comment data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        author.setText(data.author.login);
        text.setText(data.text);
    }

    @Override
    public int getId() {
        return R.layout.part_comment;
    }

}
