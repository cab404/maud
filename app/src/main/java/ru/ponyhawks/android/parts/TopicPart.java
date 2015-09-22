package ru.ponyhawks.android.parts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.libph.data.Comment;
import com.cab404.libph.data.Topic;
import com.cab404.libph.requests.CommentAddRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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
    public static final DisplayImageOptions IMG_CFG =
            new DisplayImageOptions.Builder().cacheInMemory(true).build();

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.avatar)
    ImageView avatar;

    @Override
    public void convert(View view, Topic data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        text.setText(data.text);

        author.setText(data.author.login);
        avatar.setImageDrawable(null);
        if (!data.author.is_system) {
            ImageLoader.getInstance().displayImage(data.author.small_icon, avatar, IMG_CFG);
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_topic;
    }

}
