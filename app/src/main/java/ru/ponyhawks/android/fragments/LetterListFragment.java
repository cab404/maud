package ru.ponyhawks.android.fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.LetterLabel;
import com.cab404.libph.pages.LetterTablePage;
import com.cab404.libph.pages.MainPage;

import ru.ponyhawks.android.activity.LetterActivity;
import ru.ponyhawks.android.activity.TopicActivity;
import ru.ponyhawks.android.parts.LetterLabelPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.utils.ClearAdapterTask;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:43 on 23/09/15
 *
 * @author cab404
 */
public class LetterListFragment extends RefreshableListFragment {

    private ChumrollAdapter adapter;
    private MidnightSync sync;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
        final LetterLabelPart letterLabelPart = new LetterLabelPart();
        adapter.prepareFor(letterLabelPart);

        letterLabelPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<LetterLabel>() {
            @Override
            public void onClick(LetterLabel data, View view) {
                switchToPage(data);
            }
        });

        setAdapter(adapter);
        setRefreshing(true);
        onRefresh();
    }


    @Override
    public void onRefresh() {
        RequestManager
                .fromActivity(getActivity())
                .manage(new LetterTablePage())
                .setHandlers(
                        new UpdateCommonInfoTask(),
                        new ClearAdapterTask(adapter, sync),
                        sync.bind(MainPage.BLOCK_LETTER_LABEL, LetterLabelPart.class)
                )
                .setCallback(new RequestManager.SimpleRequestCallback<LetterTablePage>() {
            @Override
            public void onFinish(LetterTablePage what) {
                setRefreshing(false);
            }
        })
                .start();
    }

    public static LetterListFragment getInstance() {
        return new LetterListFragment();
    }

    private void switchToPage(LetterLabel data) {
        Intent startTopicActivity = new Intent(getActivity(), LetterActivity.class);
        startTopicActivity.putExtra(LetterActivity.KEY_LETTER_ID, data.id);
        startTopicActivity.putExtra("title", data.title);
        boolean useMultitasking =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("multitasking", false);

        if (useMultitasking && Build.VERSION.SDK_INT >= 21) {
            /* Restarting activity if exists in background */
            ActivityManager man = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            final ComponentName activityComponent = new ComponentName(getActivity(), LetterFragment.class);
            for (ActivityManager.AppTask task : man.getAppTasks()) {
                final ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();

                if (activityComponent.equals(taskInfo.baseIntent.getComponent())) {
                    final Intent running = taskInfo.baseIntent;
                    if (running.getIntExtra(LetterFragment.KEY_LETTER_ID, -1) == data.id) {
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

}
