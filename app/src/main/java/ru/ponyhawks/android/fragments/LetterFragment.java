package ru.ponyhawks.android.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import com.cab404.libph.data.Letter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.data.Type;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.LetterPage;
import com.cab404.libph.pages.MainPage;
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

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.CommentNumPart;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.LetterPart;
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
public class LetterFragment extends ListFragment implements CommentEditFragment.SendCallback, CommentPart.CommentPartCallback {
    public static final String KEY_LETTER_ID = "letterId";

    private ChumrollAdapter adapter;
    private MidnightSync sync;
    private CommentPart commentPart;

    private Comment replyingTo = null;
    private boolean editing = false;

    private int letterId;


    private CommentEditFragment commentFragment;
    private Letter letter;

    public void setCommentFragment(CommentEditFragment commentFragment) {
        this.commentFragment = commentFragment;
        commentFragment.setSendCallback(this);
    }

    public static LetterFragment getInstance(int id) {
        final LetterFragment rootFragment = new LetterFragment();
        Bundle args = new Bundle();

        args.putInt(KEY_LETTER_ID, id);

        rootFragment.setArguments(args);
        return rootFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        letterId = getArguments().getInt(KEY_LETTER_ID, -1);

        adapter = new ChumrollAdapter();
        final LetterPart letterPart = new LetterPart();

        commentPart = new CommentPart();
        commentPart.setCallback(this);
        commentPart.saveState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("saveCommentState", true);

        final SpacePart spacePart = new SpacePart();

        final CommentNumPart commentNumPart = new CommentNumPart();
        adapter.prepareFor(letterPart, commentPart, spacePart, new LoadingPart(), commentNumPart);
        final int loadingPartId = adapter.add(LoadingPart.class, null);
        setAdapter(adapter);

        sync = new MidnightSync(adapter);

        RequestManager.fromActivity(getActivity())
                .manage(new LetterPage(letterId))
                .setHandlers(
                        sync
                                .bind(MainPage.BLOCK_LETTER_HEADER, letterPart)
                                .bind(MainPage.BLOCK_COMMENT, commentPart)
                                .bind(MainPage.BLOCK_COMMENT_NUM, commentNumPart),
                        new UpdateCommonInfoTask(),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                switch (key) {
                                    case BasePage.BLOCK_LETTER_HEADER:
                                        letter = (Letter) object;
                                        view.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                getActivity().setTitle(((Letter) object).title);
                                            }
                                        });
                                        getActivity().supportInvalidateOptionsMenu();
                                        break;
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
                .setCallback(new RequestManager.SimpleRequestCallback<LetterPage>() {

                    @Override
                    public void onError(LetterPage what, Exception e) {
                        super.onError(what, e);
                        getActivity().finish();
                        e.printStackTrace();
                    }

                    @Override
                    public void onSuccess(LetterPage what) {
                        final float dp = view.getResources().getDisplayMetrics().density;
                        sync.inject((int) (68 * dp), spacePart);
                        for (Comment cm : what.comments)
                            if (cm.is_new)
                                newCommentsStack.add(cm.id);
                    }

                    @Override
                    public void onFinish(LetterPage what) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.removeById(loadingPartId);
                                updateSpinnerNum();
                            }
                        });
                    }
                })
                .start();

        if (commentFragment != null)
            commentFragment.setTarget(getActivity().getString(R.string.replying_topic));

    }

    volatile boolean updating = false;

    List<Integer> newCommentsStack = new ArrayList<>();
    Comparator<Integer> levelIDs = new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return commentPart.getIndex(lhs, adapter) - commentPart.getIndex(rhs, adapter);
        }
    };

    public void updateSpinnerNum() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (spinningWheel == null) return;
                spinningWheel.setNum(newCommentsStack.size());
                spinningWheel.setMenuIcon(updateItem, getView());
            }
        });
    }

    public void nextNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (newCommentsStack.isEmpty()) return;

                Collections.sort(newCommentsStack, levelIDs);

                int next = newCommentsStack.remove(0);
                final int index = commentPart.getIndex(next, adapter);

                if (Build.VERSION.SDK_INT >= 11)
                    list.smoothScrollToPositionFromTop(index, 0, 200);
                else
                    list.smoothScrollToPosition(index);

                commentPart.offsetToId(list, next);

                updateSpinnerNum();
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
                updateSpinnerNum();
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void update(final boolean clearNew) {
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

        final RefreshCommentsRequest request = new RefreshCommentsRequest(
                Type.TALK, letterId, commentPart.getLastCommentId()
        );
        RequestManager.fromActivity(getActivity())
                .manage(request)
                .setCallback(new RequestManager.SimpleRequestCallback<RefreshCommentsRequest>() {
                    @Override
                    public void onSuccess(final RefreshCommentsRequest what) {
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                if (clearNew) {
                                    clearNew();
                                }
                                for (Comment cm : what.comments) {
                                    sync.inject(cm, commentPart, commentPart);
                                    commentPart.register(cm);
                                    if (cm.is_new) newCommentsStack.add(cm.id);
                                }
                                sync.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateSpinnerNum();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(RefreshCommentsRequest what, Exception e) {
                        super.onError(what, e);
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
        commentPart.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.copy_link:
                final String clip = String.format("http://ponyhawks.ru/talk/%d.html", letterId);
                setClipboard(clip);
                Toast.makeText(getActivity(), R.string.topic_link_copied, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reply:
                if (commentFragment.getState() == HideablePartBehavior.State.EXPANDED) {
                    commentFragment.collapse();
                } else
                    reply(null, getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    MenuItem updateItem;
    UpdateDrawable spinningWheel;

    void setUpdating(boolean updating) {
        spinningWheel.setSpinning(updating);
        if (updating)
            spinningWheel.setMenuIcon(updateItem, getView());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (letter == null) return;
        inflater.inflate(R.menu.menu_topic, menu);

        spinningWheel = new UpdateDrawable(getActivity());

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
                        sync.post(new Runnable() {
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
        final String clip = String.format("http://ponyhawks.ru/talk/%d.html#comment%d", letterId, cm.id);
        setClipboard(clip);
        Toast.makeText(getActivity(), R.string.comment_link_copied, Toast.LENGTH_SHORT).show();
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
            req = new CommentEditRequest(reply, message.toString());
        else
            req = new CommentAddRequest(Type.TALK, letterId, reply, message.toString());

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

    @Override
    public void onCommentActionInvoked(Action act, Comment cm, Context context) {
        switch (act) {
            case REPLY:
                reply(cm, context);
                break;
            case SHARE:
                share(cm, context);
                break;
            case FAV:
                fav(cm, context);
                break;
            case EDIT:
                edit(cm, context);
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
        list.setSelection(commentPart.getIndex(value, adapter));
    }
}
