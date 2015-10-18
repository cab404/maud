package ru.ponyhawks.android.parts;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.LetterLabel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.LetterListFragment;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:24 on 23/09/15
 *
 * @author cab404
 */
public class LetterLabelPart extends MoonlitPart<LetterLabel> {
    private Set<Integer> selection = new HashSet<>();
    private final LetterListFragment act;
    private AbsListView list;

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.recipients)
    TextView recipients;
    @Bind(R.id.date)
    TextView date;
    @Bind(R.id.comment_num)
    TextView commentNum;
    @Bind(R.id.new_comments)
    TextView newComments;
    @Bind(R.id.selected)
    CheckBox selected;
    private ActionMode actionMode;

    private Callback actionModeCallback = new Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Action mode!");
            mode.getMenuInflater().inflate(R.menu.letters, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    act.delete(new HashSet<>(selection));
                    break;
                case R.id.markread:
                    act.markRead(new HashSet<>(selection));
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearActionMode();
            ((ChumrollAdapter) list.getAdapter()).notifyDataSetChanged();
        }

        private void clearActionMode() {
            actionMode = null;
            selection.clear();
        }
    };

    public LetterLabelPart(LetterListFragment act) {
        this.act = act;
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_letter_label;
    }

    @Override
    public void convert(View view, final LetterLabel data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        list = (AbsListView) parent;

        view.setBackgroundColor(data.is_new ? 0x40000000 : 0);

        commentNum.setText(data.comments + "");
        newComments.setVisibility(data.comments_new > 0 ? View.VISIBLE : View.GONE);
        newComments.setText("+" + data.comments_new);

        recipients.setText(TextUtils.join(", ", data.recipients));

        date.setText(
                SimpleDateFormat.getDateInstance(
                        DateFormat.LONG
                ).format(data.date.getTime())
        );

        Boolean checked = selection.contains(data.id);
        selected.setChecked(checked);

        selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean checked = ((CheckBox) v).isChecked();
                if (checked)
                    selection.add(data.id);
                else
                    selection.remove(data.id);
                processModes();
            }
        });
    }

    void processModes() {
        if (selection.size() > 0) {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) act.getActivity())
                        .startSupportActionMode(actionModeCallback);
            }
            actionMode.setTitle(
                    selection.size()
                            + " " +
                            act.getResources().getQuantityString(
                                    R.plurals.letter_num,
                                    selection.size()
                            )
            );
        } else {
            if (actionMode != null)
                actionMode.finish();
            actionMode = null;
        }
    }

    public void disconnect() {
        if (actionMode != null)
            actionMode.finish();
    }
}
