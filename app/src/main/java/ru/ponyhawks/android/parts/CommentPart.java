package ru.ponyhawks.android.parts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.chumroll.ViewConverter;
import com.cab404.libph.data.Comment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.StaticWebView;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:26 on 14/09/15
 *
 * @author cab404
 */
public class CommentPart extends MoonlitPart<Comment> {
    Map<Integer, Integer> parents = new HashMap<>();

    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.avatar)
    ImageView avatar;

    public void register(Comment comment){
        parents.put(comment.id, comment.parent);
    }

    private int levelOf(int id, int cl) {
        int parent = parents.get(id);
        if (parent != 0)
            return levelOf(parent, cl + 1);
        else
            return cl;
    }

    protected int levelOf(int id) {
        return levelOf(id, 0);
    }


    @Override
    public void convert(View view, Comment data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);

        parents.put(data.id, data.parent);

        int level = levelOf(data.id);
        int padding = (int) (view.getContext().getResources().getDisplayMetrics().density * 16) * level;
        FrameLayout layout = (FrameLayout) view;
        layout.setPadding(padding, 0, 0, -padding);

        author.setText(data.author.login);
        text.setText(data.text);

        avatar.setVisibility(data.author.is_system ? View.GONE : View.VISIBLE);
        avatar.setImageDrawable(null);
        if (!data.author.is_system) {
            final DisplayImageOptions cfg = new DisplayImageOptions.Builder().cacheInMemory(true).build();
            ImageLoader.getInstance().displayImage(data.author.small_icon, avatar, cfg);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_comment;
    }

}
