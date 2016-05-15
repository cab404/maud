package ru.everypony.maud.parts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.modules.CommentTreeModule;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.everypony.maud.R;
import ru.everypony.maud.text.DateUtils;
import ru.everypony.maud.text.HtmlRipper;
import ru.everypony.maud.text.StaticWebView;
import ru.everypony.maud.utils.DoubleClickListener;
import ru.everypony.maud.utils.MidnightSync;

/**
 * Rated Insane.
 * <p/>
 * <p/>
 * Created at 00:26 on 14/09/15
 *
 * @author cab404
 */
public class CommentPart extends MoonlitPart<Comment> implements MidnightSync.InsertionRule<Comment> {
    Map<Integer, Comment> data = new HashMap<>();
    Map<Integer, Integer> ids = new HashMap<>();

    public boolean saveState = true;
    Map<Integer, HtmlRipper> savedStates = new HashMap<>();
    private CommentPartCallback callback;

    private int baseIndex = 2;
    public static final DisplayImageOptions AVATARS_CFG = new DisplayImageOptions.Builder().cacheInMemory(true).build();
    private int selectedId;

    public synchronized void register(Comment comment) {
        data.put(comment.id, comment);
    }

    public void updateFrom(CommentTreeModule data){
        for (Integer id : data.parents.keySet()){
            this.data.get(id).parent = data.parents.get(id);
        }
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

    public Collection<Comment> getComments() {
        return data.values();
    }

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

    public void offsetToId(AbsListView parent, int id) {
        final int lv = (int) (parent.getContext().getResources().getDisplayMetrics().density * 16);
        final int level = levelOf(id) * lv;
        offset(parent, level);
    }

    public int getLastCommentId() {
        int max = 0;
        for (Integer id : data.keySet()) max = Math.max(id, max);
        return max;
    }

    void resetOffset(View view, Comment data) {
        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        int level = levelOf(data.id);
        int padding = -lv * level + savedOffset;
        view.scrollTo(padding, view.getScrollY());
    }

    public void updateIndexes(ChumrollAdapter adapter) {
        int sid = adapter.typeIdOf(this);
        for (int i = 0; i < adapter.getCount(); i++)
            if (sid == adapter.getItemViewType(i)) {
                final Comment data = (Comment) adapter.getData(i);
                ids.put(data.id, adapter.idOf(i));
            }
    }

    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.date)
    TextView date;
    @Bind(R.id.avatar)
    ImageView avatar;
    @Bind(R.id.userspace)
    View userspace;
    @Bind(R.id.delimiter)
    View delimeter;
    @Bind(R.id.root)
    View root;

    @Override
    public void convert(View view, final Comment cm, int index, final ViewGroup parent, ChumrollAdapter adapter) {
        register(cm);
        super.convert(view, cm, index, parent, adapter);

        ButterKnife.bind(this, view);

        view.setBackgroundColor(selectedId == cm.id ? 0x80000000 : cm.is_new ? 0x40000000 : 0);

        boolean showDelimeters = PreferenceManager
                .getDefaultSharedPreferences(view.getContext())
                .getBoolean("showDelimiters", false);
        delimeter.setVisibility(showDelimeters ? View.VISIBLE : View.GONE);

        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        view.setOnClickListener(new DoubleClickListener() {
            @Override
            public void act(View v) {
                int offset = levelOf(cm.id) * lv;
                if (savedOffset == offset) offset = 0;
                offset((AbsListView) parent, offset);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showActionDialog(cm, v.getContext());
                return true;
            }
        });

        final View.OnClickListener showControls = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showActionDialog(cm, v.getContext());
            }
        };
        userspace.setOnClickListener(showControls);

        resetOffset(view, cm);

        if (saveState)
            if (savedStates.containsKey(cm.id))
                text.setRipper(savedStates.get(cm.id));
            else
                savedStates.put(cm.id, text.setText(cm.text));
        else
            text.setText(cm.text);


        avatar.setVisibility(cm.author.mid_icon == null ? View.GONE : View.VISIBLE);

        author.setText(cm.author.login);
        avatar.setImageDrawable(null);

        date.setText(DateUtils.formPreciseDate(cm.date));
        root.setBackgroundColor(cm.deleted ? 0x40000000 : 0);

        if (cm.author.mid_icon != null) {
            ImageLoader.getInstance().displayImage(cm.author.small_icon, avatar, AVATARS_CFG);
        }
    }

    public void setCallback(CommentPartCallback callback) {
        this.callback = callback;
    }

    public int getIndex(int cid, ChumrollAdapter adapter) {
        return adapter.indexOf(data.get(cid));
    }

    public void setSelectedId(int selectedId) {
        this.selectedId = selectedId;
    }

    public void invalidateCommentText(int id) {
        if (saveState) {
            final HtmlRipper state = savedStates.remove(id);
            if (state != null)
                state.destroy();
        }
    }

    public interface CommentPartCallback {
        enum Action {
            EDIT, FAV, REPLY, SHARE
        }

        void onCommentActionInvoked(Action act, Comment cm, Context context);

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_comment;
    }

    public void destroy() {
        for (HtmlRipper ripper : savedStates.values())
            ripper.destroy();
    }

    public final static CommentComparator CC_INST = new CommentComparator();

    public void clearNew() {
        for (Comment cm : data.values())
            cm.is_new = false;
    }

    private final static class CommentComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment lhs, Comment rhs) {
            return lhs.id - rhs.id;
        }
    }

    List<Comment> collectChildren(int parent, ChumrollAdapter adapter) {
        List<Comment> children = new ArrayList<>();
        for (Comment c : data.values())
            if (c.parent == parent)
                children.add(c);
        for (int i = 0; i < children.size(); )
            if (adapter.indexOf(children.get(i)) == -1)
                children.remove(i);
            else
                i++;
        return children;
    }

    /**
     * Finds last index in children's tree
     */
    int upfall(ChumrollAdapter adapter, Comment parent) {
        final List<Comment> parentsNeighbours = collectChildren(parent.id, adapter);
        Collections.sort(parentsNeighbours, CC_INST);

        if (parentsNeighbours.size() == 0)
            return adapter.indexOfId(ids.get(parent.id));
        else
            return upfall(adapter, parentsNeighbours.get(parentsNeighbours.size() - 1));
    }


    /**
     * Fuck yes.
     */
    @Override
    public int indexFor(Comment newC, ViewConverter<Comment> converter, ChumrollAdapter adapter) {
        updateIndexes(adapter);

        List<Comment> nbrs = collectChildren(newC.parent, adapter);

        for (Comment cm : nbrs)
            if (cm.id == newC.id)
                return -1;

        if (nbrs.size() == 0)
            return newC.parent == 0 ? baseIndex : (adapter.indexOfId(ids.get(newC.parent)) + 1);

        nbrs.add(newC);
        Collections.sort(nbrs, CC_INST);
        int index = nbrs.indexOf(newC);

        if (index == nbrs.size() - 1)
            return upfall(adapter, nbrs.get(nbrs.size() - 2)) + 1;
        else
            return adapter.indexOfId(ids.get(nbrs.get(index + 1).id));

    }

    void showActionDialog(final Comment cm, Context ctx) {
        final int theme = ctx
                .getTheme()
                .obtainStyledAttributes(new int[]{R.attr.alert_dialog_nobg_theme})
                .getResourceId(0, 0);

        @SuppressLint("InflateParams") final
        View controls = LayoutInflater.from(ctx)
                .inflate(R.layout.alert_comment_controls, null, false);

        final AlertDialog dialog = new AlertDialog
                .Builder(ctx, theme)
                .setView(controls)
                .show();

        final ImageView fav = (ImageView) controls.findViewById(R.id.fav);
        final ImageView edit = (ImageView) controls.findViewById(R.id.edit);
        final ImageView reply = (ImageView) controls.findViewById(R.id.reply);
        final ImageView share = (ImageView) controls.findViewById(R.id.copy_link);

        fav.setImageResource(
                cm.in_favs ?
                        R.drawable.ic_star :
                        R.drawable.ic_star_outline
        );

        final View.OnClickListener acl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentPartCallback.Action act;
                switch (v.getId()) {
                    case R.id.fav:
                        act = CommentPartCallback.Action.FAV;
                        break;
                    case R.id.edit:
                        act = CommentPartCallback.Action.EDIT;
                        break;
                    case R.id.reply:
                        act = CommentPartCallback.Action.REPLY;
                        break;
                    case R.id.copy_link:
                        act = CommentPartCallback.Action.SHARE;
                        break;
                    default:
                        act = null;
                }

                if (callback != null)
                    callback.onCommentActionInvoked(act, cm, v.getContext());
                dialog.dismiss();
            }
        };
        fav.setOnClickListener(acl);
        edit.setOnClickListener(acl);
        share.setOnClickListener(acl);
        reply.setOnClickListener(acl);
    }

}
