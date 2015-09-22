package ru.ponyhawks.android.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Editable;
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
import com.cab404.libph.requests.FavRequest;
import com.cab404.libph.requests.RefreshCommentsRequest;
import com.cab404.libph.util.PonyhawksProfile;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.CommentNumPart;
import ru.ponyhawks.android.parts.LoadingPart;
import ru.ponyhawks.android.parts.SpacePart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.statics.ProfileStore;
import ru.ponyhawks.android.statics.UserInfoStore;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.CompositeHandler;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class TopicFragment extends ListFragment implements CommentEditFragment.SendCallback, CommentPart.CommentPartCallback {
    public static final String KEY_TOPIC_ID = "topicId";

    private ChumrollAdapter adapter;
    private int topicId;
    private CommentPart commentPart;
    private MidnightSync sync;
    private Comment replyingTo = null;


    private CommentEditFragment commentFragment;

    public void setCommentFragment(CommentEditFragment commentFragment) {
        this.commentFragment = commentFragment;
        commentFragment.setSendCallback(this);
    }

    public static TopicFragment getInstance(int id) {
        final TopicFragment topicFragment = new TopicFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_TOPIC_ID, id);

        topicFragment.setArguments(args);
        return topicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        topicId = getArguments().getInt(KEY_TOPIC_ID, -1);

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

        final TopicPage page = new TopicPage(topicId);
        sync = new MidnightSync(adapter);
        page.setHandler(
                new CompositeHandler(
                        sync
                                .bind(MainPage.BLOCK_TOPIC_HEADER, topicPart)
                                .bind(MainPage.BLOCK_COMMENT, commentPart)
                                .bind(MainPage.BLOCK_COMMENT_NUM, commentNumPart),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                switch (key) {
                                    case BasePage.BLOCK_TOPIC_HEADER:
                                        onTopicAcquired((Topic) object);

                                        break;
                                    case MainPage.BLOCK_COMMON_INFO:
                                        UserInfoStore.getInstance().setInfo((CommonInfo) object);
                                        break;
                                    case MainPage.BLOCK_COMMENT:
                                        commentPart.register(((Comment) object));
                                        break;
                                }
                            }
                        }
                )
        );
        new Thread() {
            @Override
            public void run() {
                PonyhawksProfile profile = ProfileStore.get();
                try {
                    page.fetch(profile);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.removeById(loadingPartId);
                        }
                    });
                    final float dp = view.getResources().getDisplayMetrics().density;
                    // injecting bottom
                    sync.inject((int) (68 * dp), spacePart);
                } catch (Exception e) {
                    getActivity().finish();
                }
            }
        }.start();
    }

    volatile boolean updating = false;

    public void clearNew() {
        commentPart.clearNew();
        adapter.notifyDataSetChanged();
    }

    public void update() {
        if (updating) return;
        updating = true;

        final RefreshCommentsRequest request
                = new RefreshCommentsRequest(Type.TOPIC, topicId, commentPart.getLastCommentId()) {
            @Override
            protected void handleResponse(String response) {
                super.handleResponse(response);

                for (Comment cm : comments) {
                    sync.inject(cm, commentPart, commentPart);
                    commentPart.register(cm);
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                try {
                    request.fetch(ProfileStore.get());
                } finally {
                    updating = false;
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        commentPart.destroy();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.copy_link:
                final ClipboardManager cbman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                final String clip = String.format("http://ponyhawks.ru/blog/%d.html", topicId);
                cbman.setText(clip);
                Toast.makeText(getActivity(), "Ссылка на пост скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reply:
                onReplyInvoked(null, getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onTopicAcquired(final Topic topic) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
//                    final AppCompatActivity act = (AppCompatActivity) getActivity();
//                    final ActionBar actionBar = act.getSupportActionBar();
//                    if (actionBar != null) {
//                        actionBar.setSubtitle(topic.title);
//                    }
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_topic, menu);
    }

    @Override
    public void onFavInvoked(final Comment cm, final Context context) {
        new Thread() {
            @Override
            public void run() {
                final boolean target_state = !cm.in_favs;
                final FavRequest request = new FavRequest(Type.COMMENT, cm.id, target_state);
                final StringBuilder msg = new StringBuilder();
                try {
                    request.exec(ProfileStore.get());
                    if (request.success())
                        cm.in_favs = target_state;
                    msg.append(request.msg);
                } catch (Exception ex) {
                    msg.append(ex.getLocalizedMessage());
                } finally {
                    sync.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onShareInvoked(Comment cm, Context context) {
        final ClipboardManager cbman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        final String clip = String.format("http://ponyhawks.ru/blog/%d.html#comment%d", topicId, cm.id);
        cbman.setText(clip);
        Toast.makeText(getActivity(), "Ссылка на комментарий скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReplyInvoked(Comment cm, Context context) {
        replyingTo = cm;
        if (cm == null)
            commentFragment.setTarget("Отвечаем в топик");
        else
            commentFragment.setTarget("Отвечаем на комментарий " + cm.id + "@" + cm.author.login);
        commentFragment.expand();
    }

    @Override
    public void onSend(Editable text) {

    }

}
