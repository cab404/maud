package ru.everypony.maud.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.modules.CommentModule;
import com.cab404.libtabun.modules.TopicModule;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.pages.MainPage;
import com.cab404.libtabun.requests.FavRequest;
import com.cab404.libtabun.requests.VoteRequest;
import com.cab404.libtabun.util.LS;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.util.SU;

import java.util.concurrent.atomic.AtomicInteger;

import ru.everypony.maud.R;
import ru.everypony.maud.activity.TopicActivity;
import ru.everypony.maud.parts.CommentPart;
import ru.everypony.maud.parts.ContinuationPart;
import ru.everypony.maud.parts.LoadingPart;
import ru.everypony.maud.parts.MoonlitPart;
import ru.everypony.maud.parts.TopicPart;
import ru.everypony.maud.parts.UpdateCommonInfoTask;
import ru.everypony.maud.utils.ClearAdapterTask;
import ru.everypony.maud.utils.CompositeHandler;
import ru.everypony.maud.utils.Meow;
import ru.everypony.maud.utils.MidnightSync;
import ru.everypony.maud.utils.RequestManager;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class PublicationsListFragment extends RefreshableListFragment {
    public static final String KEY_URL = "url";
    ChumrollAdapter adapter;

    private MidnightSync sync;
    private String url;

    int cPage = 1;
    private ContinuationPart continuationPart;

    public static PublicationsListFragment getInstance(String pageUrl) {
        final PublicationsListFragment fragment = new PublicationsListFragment();
        Bundle args = new Bundle();
        System.out.println("NEW INSTANCE " + pageUrl);
        args.putString(KEY_URL, pageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_refreshable_list;
    }

    protected String getUrl() {
        return url + (cPage > 0 ? "/page" + cPage : "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = getArguments().getString(KEY_URL);

        TopicPart topicPart = new TopicPart();
        CommentPart commentPart = new CommentPart();
        commentPart.setMoveToPostVisible(true);
        commentPart.setReplyVisible(false);
        commentPart.setCallback(new CommentPart.CommentPartCallback() {
            @Override
            public void onCommentActionInvoked(Action act, Comment cm, Context context) {
                switch (act) {
                    case OPEN_POST:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cm.link)));
                        break;
                    case FAV:
                        fav(cm, getActivity());
                        break;
                    case VOTE_MINUS:
                        vote(cm, -1, context);
                        break;
                    case VOTE_PLUS:
                        vote(cm, +1, context);
                        break;
                    case SHARE:
                        share(cm);
                        break;
                }
            }
        });

        continuationPart = new ContinuationPart(adapter) {
            @Override
            public void onEndReached() {
                cPage++;
                loadPage(false);
            }
        };

        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                switchToPage(data);
            }
        });

        adapter.prepareFor(topicPart, commentPart, continuationPart, new LoadingPart());
        setAdapter(adapter);

        setRefreshing(true);
        onRefresh();
    }

    private void switchToPage(Topic data) {
        Intent startTopicActivity = new Intent(getActivity(), TopicActivity.class);
        startTopicActivity.putExtra(TopicActivity.KEY_ID, data.id);
        startTopicActivity.putExtra("title", SU.deEntity(data.title));
        boolean useMultitasking =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("multitasking", false);

        if (useMultitasking && Build.VERSION.SDK_INT >= 21) {
            /* Restarting activity if exists in background */
            for (ActivityManager.AppTask task : Meow.getTaskList(getActivity(), TopicActivity.class)) {
                final Intent running = task.getTaskInfo().baseIntent;
                if (running.getIntExtra(TopicActivity.KEY_ID, -1) == data.id) {
                    task.moveToFront();
                    return;
                }
            }
            /* Otherwise just adding things to intent. */
            startTopicActivity.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            );
        }

        startActivity(startTopicActivity);
    }

    @Override
    public void onRefresh() {
        loadPage(true);
    }

    void loadPage(boolean clear) {

        setRefreshing(true);

        final MainPage page = getPage();

        if (!clear)
            adapter.add(LoadingPart.class, null);
        else
            cPage = 1;

        final AtomicInteger count = new AtomicInteger(0);
        RequestManager
                .fromActivity(getActivity())
                .manage(page)
                .setHandlers(
                        new CompositeHandler(
                                clear ? new ClearAdapterTask(adapter, sync) : null,
                                new UpdateCommonInfoTask(),
                                sync
                                        .bind(MainPage.BLOCK_TOPIC_HEADER, TopicPart.class)
                                        .bind(MainPage.BLOCK_COMMENT, CommentPart.class),
                                new ModularBlockParser.ParsedObjectHandler() {
                                    @Override
                                    public void handle(Object object, int key) {
                                        switch (key) {
                                            case TabunPage.BLOCK_COMMENT:
                                            case TabunPage.BLOCK_TOPIC_HEADER:
                                                count.incrementAndGet();
                                        }
                                    }
                                }
                        )
                ).setCallback(new RequestManager.SimpleRequestCallback<MainPage>() {
            @Override
            public void onError(MainPage what, final Exception e) {
                super.onError(what, e);
                sync.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(
                                        getActivity(),
                                        getActivity().getString(R.string.page_loading_failed) + e.getLocalizedMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onFinish(MainPage what) {
                super.onFinish(what);
                sync.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeToRefresh.setRefreshing(false);
                    }
                });
                if (count.get() > 0)
                    sync.inject(null, continuationPart);
            }
        }).start();
    }

    @NonNull
    private MainPage getPage() {
        return new MainPage() {
            @Override
            public String getURL() {
                System.out.println(cPage);
                return getUrl();
            }

            @Override
            protected void bindParsers(ModularBlockParser base) {
                super.bindParsers(base);
                base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
                base.bind(new CommentModule(CommentModule.Mode.LIST), BLOCK_COMMENT);
            }
        };
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

    public void share(Comment cm) {
        setClipboard(cm.link);
        Toast.makeText(getActivity(), R.string.comment_link_copied, Toast.LENGTH_SHORT).show();
    }
}
