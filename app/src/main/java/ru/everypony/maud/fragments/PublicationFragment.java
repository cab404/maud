package ru.everypony.maud.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.pages.MainPage;
import com.cab404.libtabun.requests.CommentAddRequest;
import com.cab404.libtabun.requests.CommentEditRequest;
import com.cab404.libtabun.requests.FavRequest;
import com.cab404.libtabun.requests.LSRequest;
import com.cab404.libtabun.requests.RefreshCommentsRequest;
import com.cab404.libtabun.requests.VoteRequest;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.framework.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ru.everypony.maud.R;
import ru.everypony.maud.activity.RefreshRatePickerDialog;
import ru.everypony.maud.parts.CommentNumPart;
import ru.everypony.maud.parts.CommentPart;
import ru.everypony.maud.parts.LoadingPart;
import ru.everypony.maud.parts.UpdateCommonInfoTask;
import ru.everypony.maud.utils.HideablePartBehavior;
import ru.everypony.maud.utils.Meow;
import ru.everypony.maud.utils.MidnightSync;
import ru.everypony.maud.utils.RequestManager;
import ru.everypony.maud.utils.UpdateDrawable;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public abstract class PublicationFragment extends ListFragment implements
        CommentEditFragment.SendCallback,
        CommentPart.CommentPartCallback,
        RefreshRatePickerDialog.RefreshPickedListener {

    public static final String KEY_ID = "id";
    volatile boolean updating = false;

    List<Integer> newCommentsStack = new ArrayList<>();
    RefreshRatePickerDialog.SavedRefreshState refreshState = new RefreshRatePickerDialog.SavedRefreshState();

    @BindView(R.id.colorful_button)
    ImageView updateButton;

    UpdateDrawable spinningWheel;

    long refreshRateMs = 0;
    private ChumrollAdapter adapter;
    private MidnightSync sync;
    private CommentPart commentPart;
    Comparator<Integer> levelIDs = new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return commentPart.getIndex(lhs, adapter) - commentPart.getIndex(rhs, adapter);
        }
    };
    private Comment replyingTo = null;
    private boolean editing = false;
    private CommentEditFragment commentFragment;
    private boolean atLeastSomethingIsHere;
    private boolean broken;
    private int selectedCommentId = -1;
    Runnable updateCycle = new Runnable() {
        @Override
        public void run() {
            if (isDetached()) return;
            update(false);
            list.postDelayed(this, refreshRateMs);
        }
    };

    public void setCommentFragment(CommentEditFragment commentFragment) {
        this.commentFragment = commentFragment;
        commentFragment.setSendCallback(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_topic;
    }

    abstract void prepareAdapter(ChumrollAdapter adapter);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        System.out.println("ON CREATE");
    }

    void rootPrint(View view, int offset) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < offset; i++) {
            line.append("  ");
        }
        line.append(view);
        System.out.println(line);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                rootPrint(((ViewGroup) view).getChildAt(i), offset + 1);
            }
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("ON VIEW CREATED");
        if (list == null) {
            System.err.println("*** NULL RECREATE ***");
            rootPrint(view, 0);
            if (view != null && view.findViewById(R.id.list) != null) {
                System.err.println("possible recreation?");
                list = (AbsListView) view.findViewById(R.id.list);
                updateButton = (ImageView) view.findViewById(R.id.colorful_button);
            }
        }
        onFinishedCreating();
    }

    void onFinishedCreating() {

        TypedArray attributes = getView().getContext().obtainStyledAttributes(new int[]{R.attr.colorPrimary});
        int primaryColor = attributes.getColor(0, 0);
        attributes.recycle();
        updateButton.getBackground().setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP);

        adapter = new ChumrollAdapter();

        commentPart = new CommentPart();
        commentPart.setCallback(this);
        commentPart.setMoveToPostVisible(false);
        commentPart.setBaseIndex(getCommentBaseIndex());
        commentPart.saveState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("saveCommentState", true);

        final CommentNumPart commentNumPart = new CommentNumPart();
        adapter.prepareFor(commentPart, new LoadingPart(), commentNumPart);
        prepareAdapter(adapter);
        setAdapter(adapter);

        sync = new MidnightSync(adapter);

        spinningWheel = new UpdateDrawable(getActivity());
        updateButton.setImageDrawable(spinningWheel);

        bindModules(sync);
        sync
                .bind(MainPage.BLOCK_COMMENT, commentPart)
                .bind(MainPage.BLOCK_COMMENT_NUM, commentNumPart);
        fullReload();
        if (commentFragment != null)
            commentFragment.setTarget(getActivity().getString(R.string.replying_topic));

    }

    public void nextNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (newCommentsStack.isEmpty() || adapter == null) return;

                Collections.sort(newCommentsStack, levelIDs);

                final int next = newCommentsStack.remove(0);
                final int index = commentPart.getIndex(next, adapter);

                list.post(new Runnable() {
                    @Override
                    public void run() {
                        commentPart.offsetToId(list, next);
                        list.setSelection(index);
                    }
                });

                spinningWheel.setNum(newCommentsStack.size());
                commentPart.setSelectedId(next);
                adapter.notifyDataSetChanged();
            }
        });

    }

    public abstract int getCommentBaseIndex();

    public void clearNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                commentPart.clearNew();
                newCommentsStack.clear();
                spinningWheel.setNum(newCommentsStack.size());
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void update(final boolean clearNew, int selfCommentId) {
        if (updating) return;
        updating = true;
        setUpdating(true);
        commentPart.setSelectedId(0);
        list.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        if (broken) {
            fullReload();
            return;
        }

        final RefreshCommentsRequest request = getRefreshRequest(getCommentPart().getLastCommentId());
        request.setSelfIdComment(selfCommentId);
        if (getActivity() != null)
            RequestManager.fromActivity(getActivity())
                    .manage(request)
                    .setCallback(new RequestManager.SimpleRequestCallback<RefreshCommentsRequest>() {
                        @Override
                        public void onStart(RefreshCommentsRequest what) {
                            super.onStart(what);
                        }

                        @Override
                        public void onSuccess(final RefreshCommentsRequest what) {
                            list.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (clearNew) {
                                        clearNew();
                                    }
                                    for (Comment cm : what.comments) {
                                        sync.inject(cm, commentPart, commentPart);
                                        commentPart.register(cm);
                                        if (cm.is_new)
                                            newCommentsStack.add(cm.id);
                                    }
                                    spinningWheel.setNum(newCommentsStack.size());
                                }
                            });
                        }

                        @Override
                        public void onError(RefreshCommentsRequest what, final Exception e) {
                            super.onError(what, e);
                            e.printStackTrace();
                            Meow.inMain(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onFinish(RefreshCommentsRequest what) {
                            updating = false;
                            setUpdating(false);
                        }
                    })
                    .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (commentPart != null)
            commentPart.destroy();
    }

    public void fullReload() {
        updating = true;
        setUpdating(true);
        adapter.clear();
        final int loadingPartId = adapter.add(LoadingPart.class, null);
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (commentFragment != null)
                    commentFragment.collapse();
            }
        });
        RequestManager.fromActivity(getActivity())
                .manage(getPageRequest())
                .setHandlers(
                        sync,
                        new UpdateCommonInfoTask(),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                handleInitialLoad(object, key);
                                atLeastSomethingIsHere = true;
                                switch (key) {
                                    case MainPage.BLOCK_COMMENT:
                                        final Comment cm = (Comment) object;
                                        commentPart.register(cm);
                                        if (cm.is_new)
                                            newCommentsStack.add(cm.id);
                                        break;
                                    case MainPage.BLOCK_COMMENTS_ENABLED:
                                        break;
                                }
                            }
                        })
                .setCallback(new RequestManager.SimpleRequestCallback<Page>() {

                    @Override
                    public void onError(Page what, final Exception e) {
                        super.onError(what, e);
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() == null) return;
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                broken = true;
                                if (!atLeastSomethingIsHere) {
                                    getActivity().finish();
                                }
                            }
                        };
                        Meow.inMain(runnable);
                        e.printStackTrace();
                    }

                    @Override
                    public void onFinish(Page what) {
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.removeById(loadingPartId);
                                spinningWheel.setNum(newCommentsStack.size());
                                System.out.println("SelectPhase");

                                int index = commentPart.getIndex(selectedCommentId, adapter);
                                System.out.println("index " + index);
                                if (index > 0)
                                    moveToComment(selectedCommentId);
                            }
                        });

                        updating = false;
                        setUpdating(false);
                    }

                    @Override
                    public void onSuccess(Page what) {
                        super.onSuccess(what);
                        broken = false;
                    }
                })
                .start();
    }

    public CommentPart getCommentPart() {
        return commentPart;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.continuous_refresh:
                final RefreshRatePickerDialog dialog = new RefreshRatePickerDialog(getActivity());
                dialog.setListener(this);
                dialog.setState(refreshState);
                dialog.show();
                break;
            case R.id.copy_link:
                setClipboard(getLink());
                Toast.makeText(getActivity(), R.string.topic_link_copied, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reply:
                if (commentFragment.getState() == HideablePartBehavior.State.EXPANDED) {
                    commentFragment.collapse();
                } else
                    reply(null, getActivity());
                return true;
            case R.id.to_the_bottom:
                list.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setSelection(adapter.getCount() - 1);
                    }
                });
                return true;
            case R.id.search:
                final EditText search = new EditText(getActivity());
                search.setHint(android.R.string.search_go);
                new AlertDialog.Builder(getActivity())
                        .setView(search)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                newCommentsStack.clear();
                                for (Comment comment : commentPart.getComments())
                                    if (search.getText() != null && comment.text != null
                                            && comment.text.toLowerCase().contains(
                                            (search.getText() + "").toLowerCase())
                                            )
                                        newCommentsStack.add(comment.id);
                                nextNew();
                            }
                        }).show();
                return true;
            case R.id.reload:
                if (!updating)
                    fullReload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.colorful_button)
    void onRefreshClicked() {
        if (newCommentsStack.isEmpty())
            update(true);
        else
            nextNew();
    }

    @OnLongClick(R.id.colorful_button)
    boolean onRefreshForced() {
        update(true);
        return true;
    }

    void setUpdating(boolean updating) {
        spinningWheel.setSpinning(updating);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.publication, menu);
    }

    public void fav(final Comment cm, final Context context) {
        final boolean target_state = !cm.in_favs;
        final FavRequest request = new FavRequest(Type.COMMENT, cm.id, target_state);
        RequestManager.fromActivity(getActivity())
                .manage(request)
                .setCallback(new RequestManager.SimpleRequestCallback<FavRequest>() {

                    @Override
                    public void onSuccess(FavRequest what) {
                        if (request.success())
                            cm.in_favs = target_state;
                        msg(request.msg);
                    }

                    @Override
                    public void onError(FavRequest what, Exception e) {
                        msg(e.getLocalizedMessage());
                    }

                    void msg(final String msg) {
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .start();

    }

    public void vote(final Comment cm, int side, final Context context) {
        final VoteRequest request = new VoteRequest(cm.id, side, Type.COMMENT);
        RequestManager.fromActivity(getActivity())
                .manage(request)
                .setCallback(new RequestManager.SimpleRequestCallback<VoteRequest>() {

                    @Override
                    public void onSuccess(VoteRequest what) {
                        if (request.success())
                            cm.votes = (int) what.result;
                        msg(request.msg);
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onError(VoteRequest what, Exception e) {
                        msg(e.getLocalizedMessage());
                    }

                    void msg(final String msg) {
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .start();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected void setClipboard(String to) {
        final ClipboardManager cbman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cbman.setText(to);
    }

    public void reply(Comment cm, Context context) {
        replyingTo = cm;
        editing = false;

        if (cm == null)
            commentFragment.setTarget(context.getString(R.string.replying_topic));
        else
            commentFragment.setTarget(String.format(context.getString(R.string.replying_comment), cm.id, cm.author.login));
        commentFragment.expand();
    }

    @Override
    public void onSend(final Editable message) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getActivity().getString(R.string.sending_messsge));
        dialog.show();

        int reply = replyingTo == null ? 0 : replyingTo.id;

        LSRequest req;
        if (editing)
            req = getCommentEditRequest(message, reply);
        else
            req = getCommentAddRequest(message, reply);

        RequestManager
                .fromActivity(getActivity())
                .manage(req)
                .setCallback(new RequestManager.SimpleRequestCallback<LSRequest>() {
                    @Override
                    public void onSuccess(final LSRequest what) {
                        super.onSuccess(what);
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(what.msg))
                                    Toast.makeText(getActivity(), what.msg, Toast.LENGTH_SHORT).show();

                                if (what.success()) {
                                    if (editing) {
                                        replyingTo.text = message.toString();
                                        commentPart.invalidateCommentText(replyingTo.id);
                                    }

                                    if (what instanceof CommentAddRequest)
                                        update(false, ((CommentAddRequest) what).id);
                                    else
                                        update(false);

                                    commentFragment.hide();
                                    commentFragment.clear();

                                    if (TextUtils.isEmpty(what.msg))
                                        Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                                }

                                if (!what.success() && TextUtils.isEmpty(what.msg))
                                    Toast.makeText(getActivity(), R.string.undefined_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(LSRequest what, final Exception e) {
                        super.onError(what, e);
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFinish(LSRequest what) {
                        super.onFinish(what);
                        dialog.dismiss();
                    }
                })
                .start();
    }

    private void update(boolean reset) {
        update(reset, 0);
    }

    @Override
    public void onCommentActionInvoked(Action act, Comment cm, Context context) {
        switch (act) {
            case REPLY:
                reply(cm, context);
                break;
            case SHARE:
                share(cm, context);
                break;
            case EDIT:
                edit(cm, context);
                break;
            case FAV:
                fav(cm, context);
                break;
            case VOTE_MINUS:
                vote(cm, -1, context);
                break;
            case VOTE_PLUS:
                vote(cm, +1, context);
                break;
        }
    }

    public void share(Comment cm, Context context) {
        int id = getArguments().getInt(KEY_ID);
        final String clip = String.format("https://tabun.everypony.ru/blog/%d.html#comment%d", id, cm.id);
        setClipboard(clip);
        Toast.makeText(getActivity(), R.string.comment_link_copied, Toast.LENGTH_SHORT).show();
    }

    private void edit(Comment cm, Context context) {
        if (commentFragment.getState() == HideablePartBehavior.State.EXPANDED) {
            commentFragment.collapse();
            return;
        }
        commentFragment.setText(cm.text);
        replyingTo = cm;
        editing = true;

        commentFragment.setTarget(String.format(context.getString(R.string.editing_comment), cm.id, cm.author.login));
        commentFragment.expand();
    }

    public void moveToComment(final int value) {
        commentPart.setSelectedId(value);
        adapter.notifyDataSetChanged();
        final int index = commentPart.getIndex(value, adapter);
        list.post(new Runnable() {
            @Override
            public void run() {
                commentPart.offsetToId(list, value);
                list.setSelection(index);
            }
        });
    }

    @Override
    public void onRefreshRatePicked(boolean enabled, long rate_ms) {
        list.removeCallbacks(updateCycle);
        if (enabled) list.postDelayed(updateCycle, refreshRateMs = rate_ms);
    }

    protected abstract Page getPageRequest();

    protected abstract void bindModules(MidnightSync sync);

    protected abstract void handleInitialLoad(final Object object, int key);

    protected abstract RefreshCommentsRequest getRefreshRequest(int lastCommentId);

    protected abstract String getLink();

    protected abstract CommentAddRequest getCommentAddRequest(Editable message, int reply);

    protected abstract CommentEditRequest getCommentEditRequest(Editable message, int reply);


    public void setSelectedCommentId(int selectedCommentId) {
        this.selectedCommentId = selectedCommentId;
    }
}
