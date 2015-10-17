package ru.ponyhawks.android.fragments;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.modules.CommentModule;
import com.cab404.libph.modules.TopicModule;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.MainPage;
import com.cab404.moonlight.framework.ModularBlockParser;

import java.util.concurrent.atomic.AtomicInteger;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.activity.TopicActivity;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.ContinuationPart;
import ru.ponyhawks.android.parts.LoadingPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.utils.ClearAdapterTask;
import ru.ponyhawks.android.utils.CompositeHandler;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;

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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = getArguments().getString(KEY_URL);

        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
        TopicPart topicPart = new TopicPart();
        CommentPart commentPart = new CommentPart();

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
        startTopicActivity.putExtra("title", data.title);
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
                                            case BasePage.BLOCK_COMMENT:
                                            case BasePage.BLOCK_TOPIC_HEADER:
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

}
