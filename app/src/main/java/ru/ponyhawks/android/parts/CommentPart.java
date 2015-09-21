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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.HtmlRipper;
import ru.ponyhawks.android.text.StaticWebView;
import ru.ponyhawks.android.utils.DoubleClickListener;
import ru.ponyhawks.android.utils.UniteSynchronization;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:26 on 14/09/15
 *
 * @author cab404
 */
public class CommentPart extends MoonlitPart<Comment> implements UniteSynchronization.InsertionRule<Comment> {
    Map<Integer, Comment> data = new HashMap<>();
    Map<Integer, Integer> parents = new HashMap<>();
    Map<Integer, HtmlRipper> savedStates = new HashMap<>();

    public boolean saveState = true;

    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.avatar)
    ImageView avatar;
    private int baseIndex;

    public void register(Comment comment) {
        data.put(comment.id, comment);
    }

    private int levelOf(int id, int cl) {
        if (!data.containsKey(id)) return 0;
        int parent = data.get(id).parent;
        if (parent != 0)
            return levelOf(parent, cl + 1);
        else
            return cl;
    }

    protected int levelOf(int id) {
        return levelOf(id, 0);
    }

    /**
     * Sets new base index (where the tree starts).
     * Needed for when you add to empty tree by
     */
    public void setBaseIndex(int index) {
        baseIndex = index;
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

    public int getLastCommentId() {
        int max = 0;
        for (Integer id : parents.keySet()) max = Math.max(id, max);
        return max;
    }

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

        if (saveState)
            if (savedStates.containsKey(cm.id))
                text.setRipper(savedStates.get(cm.id));
            else
                savedStates.put(cm.id, text.setText(cm.text));
        else
            text.setText(cm.text);


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

    private final static CommentComparator CC_INST = new CommentComparator();

    private final static class CommentComparator implements Comparator<Comment> {

        @Override
        public int compare(Comment lhs, Comment rhs) {
            return lhs.id - rhs.id;
        }
    }

    List<Comment> collectChildren(int parent) {
        List<Comment> children = new ArrayList<>();
        for (Comment c : data.values())
            if (c.parent == parent)
                children.add(c);
        return children;
    }

    /**
     * Finds next index to parent's tree
     */
    int downfall(ChumrollAdapter adapter, Comment parent) {
        final List<Comment> parentsNeighbours = collectChildren(parent.parent);
        Collections.sort(parentsNeighbours, CC_INST);

        final int index = parentsNeighbours.indexOf(parent);
        if (index == parentsNeighbours.size() - 1)
            if (parent.parent == 0)
                return baseIndex + data.size();
            else
                return downfall(adapter, data.get(parent.parent));
        else
            return adapter.indexOf(parentsNeighbours.get(index + 1));
    }

    /**
     * Fuck yes. It's a complete, movable tree function, not some sort of offset bullshit >X)
     */
    @Override
    public int indexFor(Comment newC, ViewConverter<Comment> converter, ChumrollAdapter adapter) {
        List<Comment> parentNeighbours = collectChildren(newC.parent);

        if (parentNeighbours.size() == 0)
            return newC.parent == 0 ? baseIndex : (adapter.indexOf(data.get(newC.parent)) + 1);

        parentNeighbours.add(newC);
        Collections.sort(parentNeighbours, CC_INST);
        int index = parentNeighbours.indexOf(newC);

        if (index == parentNeighbours.size() - 1)
            if (newC.parent == 0)
                // Play dirty!
                return baseIndex + index;
            else if (index < parentNeighbours.size() - 2)
                // just before next neighbour
                return adapter.indexOf(parentNeighbours.get(index + 1));
            else
                // well, shit. Before next parent's neighbour, if exists
                return downfall(adapter, data.get(newC.parent));
        else
            return adapter.indexOf(parentNeighbours.get(index + 1));

    }
}
