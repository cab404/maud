package ru.ponyhawks.android.fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.modules.TopicModule;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.util.PonyhawksProfile;
import com.cab404.moonlight.framework.ModularBlockParser;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.activity.TopicActivity;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.SpacePart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.ClearAdapterTask;
import ru.ponyhawks.android.utils.CompositeHandler;
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
public class TopicListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String KEY_URL = "url";
    ChumrollAdapter adapter;

    @Bind(R.id.swipe_to_refresh)
    SwipeRefreshLayout swipe;
    private MidnightSync sync;
    private TopicPart topicPart;


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
        ButterKnife.bind(this, view);

        final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.colorPrimary, R.attr.inverse_action_bar_color}
        );
        swipe.setColorSchemeColors(styledAttributes.getColor(1, 0), styledAttributes.getColor(1, 0));
        swipe.setProgressBackgroundColorSchemeColor(styledAttributes.getColor(0, 0));
        swipe.setOnRefreshListener(this);

        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
        topicPart = new TopicPart();
        SpacePart spacePart = new SpacePart();

        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                switchToPage(data);
            }
        });

        adapter.prepareFor(spacePart, topicPart);
        setAdapter(adapter);
        onRefresh();
        view.post(new Runnable() {
            @Override
            public void run() {
                // workaround for refresher to show up
                swipe.measure(swipe.getWidth(), swipe.getHeight());
                swipe.setRefreshing(true);
            }
        });
    }

    private void switchToPage(Topic data) {
        Intent startTopicActivity = new Intent(getActivity(), TopicActivity.class);
        startTopicActivity.putExtra(TopicActivity.KEY_TOPIC_ID, data.id);
        startTopicActivity.putExtra("title", data.title);
        boolean useMultitasking =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("multitasking", false);

        if (useMultitasking && Build.VERSION.SDK_INT >= 21) {
            /* Restarting activity if exists in background */
            ActivityManager man = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            final ComponentName topicActivityComponent = new ComponentName(getActivity(), TopicActivity.class);
            for (ActivityManager.AppTask task : man.getAppTasks()) {
                final ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();

                if (topicActivityComponent.equals(taskInfo.baseIntent.getComponent())) {
                    final Intent running = taskInfo.baseIntent;
                    if (running.getIntExtra(TopicActivity.KEY_TOPIC_ID, -1) == data.id) {
                        task.moveToFront();
                        return;
                    }
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
                return getArguments().getString(KEY_URL);
            }

            @Override
            protected void bindParsers(ModularBlockParser base) {
                super.bindParsers(base);
                base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
            }
        };

        page.setHandler(
                new CompositeHandler(
                        new ClearAdapterTask(adapter, sync),
                        sync.bind(MainPage.BLOCK_TOPIC_HEADER, topicPart)
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
                    }

                    @Override
                    public void onFinish(MainPage what) {
                        super.onFinish(what);
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                swipe.setRefreshing(false);
                            }
                        });
                    }
                })
                .start();

    }

}
