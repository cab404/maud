package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ViewConverter;
import com.cab404.libph.data.Topic;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.StaticWebView;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:58 on 14/09/15
 *
 * @author cab404
 */
public class TopicPart extends MoonlitPart<Topic> {
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.text)
    StaticWebView text;

    @Override
    public void convert(View view, Topic data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        text.setText(data.text);
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_topic;
    }

}
