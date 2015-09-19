package ru.ponyhawks.android.parts;

import android.graphics.Rect;
import android.support.v4.widget.PopupMenuCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
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
import ru.ponyhawks.android.text.HtmlRipper;
import ru.ponyhawks.android.text.StaticWebView;
import ru.ponyhawks.android.utils.DoubleClickListener;

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
    Map<Integer, HtmlRipper> savedStates = new HashMap<>();
    public boolean saveState = true;

    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.avatar)
    ImageView avatar;

    public void register(Comment comment) {
        parents.put(comment.id, comment.parent);
    }

    private int levelOf(int id, int cl) {
        if (!parents.containsKey(id)) return 0;
        int parent = parents.get(id);
        if (parent != 0)
            return levelOf(parent, cl + 1);
        else
            return cl;
    }

    protected int levelOf(int id) {
        return levelOf(id, 0);
    }

    int savedOffset = 0;

    public void offset(AbsListView parent, int offset) {
        savedOffset = offset;
        final int position = parent.getFirstVisiblePosition();
        ChumrollAdapter adapter = (ChumrollAdapter) parent.getAdapter();
        int type = adapter.typeIdOf(this);
        for (int i = position; i < position + parent.getChildCount(); i++) {
            if (adapter.getItemViewType(i) == type) {
                View view = parent.getChildAt(i - position);
                Comment data = (Comment) adapter.getData(i);
                resetOffset(view, data);
            }
        }

    }


    Rect inv = new Rect();

    void resetOffset(View view, Comment data) {
        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        int level = levelOf(data.id);
        int padding = -lv * level + savedOffset;
        view.scrollTo(padding, view.getScrollY());

    }

    @Override
    public void convert(View view, final Comment cm, int index, final ViewGroup parent) {
        parents.put(cm.id, cm.parent);

        super.convert(view, cm, index, parent);
        ButterKnife.bind(this, view);

        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        view.setOnClickListener(new DoubleClickListener() {
            @Override
            public void act(View v) {
                offset((AbsListView) parent, levelOf(cm.id) * lv);
            }
        });

        resetOffset(view, cm);

        author.setText(cm.author.login);

        if (saveState) {
            if (savedStates.containsKey(cm.id)) {
                System.out.println("Got ripper for " + cm.id);
                text.setRipper(savedStates.get(cm.id));
            } else {
                savedStates.put(cm.id, text.setText(cm.text));
            }
        } else {
            text.setText(cm.text);
        }

        avatar.setVisibility(cm.author.is_system ? View.GONE : View.VISIBLE);

        avatar.setImageDrawable(null);
        if (!cm.author.is_system) {
            final DisplayImageOptions cfg = new DisplayImageOptions.Builder().cacheInMemory(true).build();
            ImageLoader.getInstance().displayImage(cm.author.small_icon, avatar, cfg);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_comment;
    }

    public void destroy() {
        for (HtmlRipper ripper : savedStates.values())
            ripper.destroy();
    }

}
