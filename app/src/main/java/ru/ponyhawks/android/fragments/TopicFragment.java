package ru.ponyhawks.android.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Comment;
import com.cab404.libph.data.CommonInfo;
import com.cab404.libph.data.Topic;
import com.cab404.libph.data.Type;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.pages.TopicPage;
import com.cab404.libph.requests.CommentAddRequest;
import com.cab404.libph.requests.CommentEditRequest;
import com.cab404.libph.requests.FavRequest;
import com.cab404.libph.requests.LSRequest;
import com.cab404.libph.requests.RefreshCommentsRequest;
import com.cab404.moonlight.framework.ModularBlockParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.CommentNumPart;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.LoadingPart;
import ru.ponyhawks.android.parts.SpacePart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.HideablePartBehavior;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;
import ru.ponyhawks.android.utils.UpdateDrawable;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class TopicFragment extends ListFragment implements CommentEditFragment.SendCallback, CommentPart.CommentPartCallback {
    public static final String KEY_ID = "id";

    private ChumrollAdapter adapter;
    private MidnightSync sync;
    private CommentPart commentPart;

    private Comment replyingTo = null;
    private boolean editing = false;

    private boolean commentsEnabled = false;

    private int topicId;

    private CommentEditFragment commentFragment;

    public void setCommentFragment(CommentEditFragment commentFragment) {
        this.commentFragment = commentFragment;
        commentFragment.setSendCallback(this);
    }

    public static TopicFragment getInstance(int id) {
        final TopicFragment topicFragment = new TopicFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_ID, id);

        topicFragment.setArguments(args);
        return topicFragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_topic;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        topicId = getArguments().getInt(KEY_ID, -1);

        adapter = new ChumrollAdapter();
        final TopicPart topicPart = new TopicPart();

        commentPart = new CommentPart();
        commentPart.setCallback(this);
        commentPart.saveState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("saveCommentState", true);

        final SpacePart spacePart = new SpacePart();

        final CommentNumPart commentNumPart = new CommentNumPart();
        adapter.prepareFor(topicPart, commentPart, spacePart, new LoadingPart(), commentNumPart);
        final int loadingPartId = adapter.add(LoadingPart.class, null);
        setAdapter(adapter);

        sync = new MidnightSync(adapter);

        spinningWheel = new UpdateDrawable(getActivity());
        updateButton.setImageDrawable(spinningWheel);

        bindModules(sync);
        RequestManager.fromActivity(getActivity())
                .manage(new TopicPage(topicId))
                .setHandlers(
                        sync
                                .bind(MainPage.BLOCK_COMMENT, commentPart)
                                .bind(MainPage.BLOCK_COMMENT_NUM, commentNumPart),
                        new UpdateCommonInfoTask(),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                handleInitialLoad(object, key);
                                switch (key) {
                                    case MainPage.BLOCK_COMMON_INFO:
                                        Providers.UserInfo.getInstance().setInfo((CommonInfo) object);
                                        break;
                                    case MainPage.BLOCK_COMMENT:
                                        commentPart.register(((Comment) object));
                                        break;
                                    case MainPage.BLOCK_COMMENTS_ENABLED:
                                        break;
                                }
                            }
                        })
                .setCallback(new RequestManager.SimpleRequestCallback<TopicPage>() {

                    @Override
                    public void onError(TopicPage what, final Exception e) {
                        super.onError(what, e);
                        if (getActivity() == null) return;
                        getActivity().finish();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        };
                        Meow.inMain(runnable);
                        e.printStackTrace();
                    }

                    @Override
                    public void onSuccess(TopicPage what) {
                        final float dp = view.getResources().getDisplayMetrics().density;
                        sync.inject((int) (68 * dp), spacePart);
                        for (Comment cm : what.comments)
                            if (cm.is_new)
                                newCommentsStack.add(cm.id);
                    }

                    @Override
                    public void onFinish(TopicPage what) {
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.removeById(loadingPartId);
                                spinningWheel.setNum(newCommentsStack.size());
                            }
                        });
                    }
                })
                .start();

        if (commentFragment != null)
            commentFragment.setTarget(getActivity().getString(R.string.replying_topic));

    }

    private void bindModules(MidnightSync sync) {
        sync.bind(MainPage.BLOCK_TOPIC_HEADER, TopicPart.class);
    }

    public void handleInitialLoad(final Object object, int key) {
        switch (key) {
            case BasePage.BLOCK_TOPIC_HEADER:
                commentsEnabled = true;
                Meow.inMain(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().setTitle(((Topic) object).title);
                        if (Build.VERSION.SDK_INT >= 21)
                            getActivity().setTaskDescription(new ActivityManager.TaskDescription(((Topic) object).title));
                    }
                });
                break;
        }
    }

    volatile boolean updating = false;

    List<Integer> newCommentsStack = new ArrayList<>();
    Comparator<Integer> levelIDs = new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return commentPart.getIndex(lhs, adapter) - commentPart.getIndex(rhs, adapter);
        }
    };

    public void nextNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (newCommentsStack.isEmpty()) return;

                Collections.sort(newCommentsStack, levelIDs);

                int next = newCommentsStack.remove(0);
                final int index = commentPart.getIndex(next, adapter);

                list.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setSelection(index);
                    }
                });

                commentPart.offsetToId(list, next);

                spinningWheel.setNum(newCommentsStack.size());
                commentPart.setSelectedId(next);
                adapter.notifyDataSetChanged();
            }
        });

    }

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

        final RefreshCommentsRequest request = getRefreshRequest();
        request.setSelfIdComment(selfCommentId);

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

    @NonNull
    private RefreshCommentsRequest getRefreshRequest() {
        System.out.println("lkid:" + commentPart.getLastCommentId());
        return new RefreshCommentsRequest(
                Type.TOPIC, topicId, commentPart.getLastCommentId()
        ) {
            @Override
            protected void handleResponse(String response) {
                System.out.println(response);
                super.handleResponse(response);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        commentPart.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.copy_link:
                final String clip = String.format("http://ponyhawks.ru/blog/%d.html", topicId);
                setClipboard(clip);
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
    boolean onRefreshForced(){
        update(true);
        return true;
    }

    @Bind(R.id.colorful_button)
    FloatingActionButton updateButton;
    UpdateDrawable spinningWheel;

    void setUpdating(boolean updating) {
        spinningWheel.setSpinning(updating);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_topic, menu);
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

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected void setClipboard(String to) {
        final ClipboardManager cbman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cbman.setText(to);
    }


    public void share(Comment cm, Context context) {
        final String clip = String.format("http://ponyhawks.ru/blog/%d.html#comment%d", topicId, cm.id);
        setClipboard(clip);
        Toast.makeText(getActivity(), R.string.comment_link_copied, Toast.LENGTH_SHORT).show();
    }

    public void reply(Comment cm, Context context) {
        if (!commentsEnabled) return;

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

    @NonNull
    private CommentAddRequest getCommentAddRequest(Editable message, int reply) {
        return new CommentAddRequest(Type.BLOG, topicId, reply, message.toString());
    }

    @NonNull
    private CommentEditRequest getCommentEditRequest(Editable message, int reply) {
        return new CommentEditRequest(reply, message.toString());
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
        }
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

    public void moveToComment(int value) {
        commentPart.setSelectedId(value);
        adapter.notifyDataSetChanged();
        list.setSelection(commentPart.getIndex(value, adapter));
    }
}
