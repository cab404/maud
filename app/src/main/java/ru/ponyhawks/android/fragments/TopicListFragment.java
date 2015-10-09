package ru.ponyhawks.android.fragments;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.modules.CommentModule;
import com.cab404.libph.modules.TopicModule;
import com.cab404.libph.pages.MainPage;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.activity.TopicActivity;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.SpacePart;
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
public class TopicListFragment extends RefreshableListFragment {
    public static final String KEY_URL = "url";
    ChumrollAdapter adapter;

    private MidnightSync sync;
    private TopicPart topicPart;
    private String url;
    private CommentPart commentPart;


    public static TopicListFragment getInstance(String pageUrl) {
        final TopicListFragment fragment = new TopicListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, pageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_refreshable_list;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = getArguments().getString(KEY_URL);

        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
        topicPart = new TopicPart();
        commentPart = new CommentPart();
        SpacePart spacePart = new SpacePart();

        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                switchToPage(data);
            }
        });

        adapter.prepareFor(spacePart, topicPart, commentPart);
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

        final MainPage page = new MainPage() {
            @Override
            public String getURL() {
                return url;
            }

            @Override
            protected void bindParsers(ModularBlockParser base) {
                super.bindParsers(base);
                base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
                base.bind(new CommentModule(CommentModule.Mode.LIST), BLOCK_COMMENT);
            }
        };

        page.setHandler(
                new CompositeHandler(
                        new ClearAdapterTask(adapter, sync),
                        new UpdateCommonInfoTask(),
                        sync
                                .bind(MainPage.BLOCK_TOPIC_HEADER, TopicPart.class)
                                .bind(MainPage.BLOCK_COMMENT, CommentPart.class)
                )
        );

        RequestManager
                .fromActivity(getActivity())
                .manage(page)
                .setCallback(new RequestManager.SimpleRequestCallback<MainPage>() {
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
                    }
                })
                .start();

    }

}
