package ru.everypony.maud.fragments;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libtabun.data.LetterLabel;
import com.cab404.libtabun.pages.LetterTablePage;
import com.cab404.libtabun.pages.MainPage;
import com.cab404.libtabun.requests.LetterListRequest;

import java.util.Set;

import ru.everypony.maud.R;
import ru.everypony.maud.activity.LetterActivity;
import ru.everypony.maud.parts.LetterLabelPart;
import ru.everypony.maud.parts.MoonlitPart;
import ru.everypony.maud.parts.UpdateCommonInfoTask;
import ru.everypony.maud.utils.ClearAdapterTask;
import ru.everypony.maud.utils.Meow;
import ru.everypony.maud.utils.MidnightSync;
import ru.everypony.maud.utils.RequestManager;

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
    private LetterLabelPart letterPart;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);
        letterPart = new LetterLabelPart(this);
        adapter.prepareFor(letterPart);

        letterPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<LetterLabel>() {
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
        setRefreshing(true);
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

    @Override
    public void onDetach() {
        super.onDetach();
        letterPart.disconnect();
    }

    private void switchToPage(LetterLabel data) {
        Intent startTopicActivity = new Intent(getActivity(), LetterActivity.class);
        startTopicActivity.putExtra(PublicationFragment.KEY_ID, data.id);
        startTopicActivity.putExtra("title", data.title);
        boolean useMultitasking =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("multitasking", false);

        if (useMultitasking && Build.VERSION.SDK_INT >= 21) {
            /* Restarting activity if exists in background */
            for (ActivityManager.AppTask task : Meow.getTaskList(getActivity(), LetterActivity.class)) {
                final Intent running = task.getTaskInfo().baseIntent;
                if (running.getIntExtra(PublicationFragment.KEY_ID, -1) == data.id) {
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

    public void delete(final Set<Integer> ids) {
        final LetterListRequest lreq = new LetterListRequest(LetterListRequest.Action.DELETE, ids.toArray(new Integer[ids.size()]));
        setRefreshing(true);
        RequestManager
                .fromActivity(getActivity())
                .manage(lreq)
                .setCallback(new RequestManager.SimpleRequestCallback<LetterListRequest>() {
                    @Override
                    public void onSuccess(LetterListRequest what) {
                        super.onSuccess(what);
                        Meow.msg(getActivity(),
                                ids.size()
                                        + " "
                                        + getResources().getQuantityString(R.plurals.letter_num, ids.size())
                                        + " "
                                        + getResources().getString(R.string.deleted)
                                , Toast.LENGTH_LONG);
                        onRefresh();
                    }

                    @Override
                    public void onError(LetterListRequest what, Exception e) {
                        super.onError(what, e);
                        Meow.msg(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }

                    @Override
                    public void onFinish(LetterListRequest what) {
                        super.onFinish(what);
                        setRefreshing(false);
                    }

                }).start();
    }

    public void markRead(final Set<Integer> ids) {
        final LetterListRequest lreq = new LetterListRequest(LetterListRequest.Action.READ, ids.toArray(new Integer[ids.size()]));
        setRefreshing(true);
        RequestManager
                .fromActivity(getActivity())
                .manage(lreq)
                .setCallback(new RequestManager.SimpleRequestCallback<LetterListRequest>() {
                    @Override
                    public void onSuccess(LetterListRequest what) {
                        super.onSuccess(what);
                        Meow.msg(getActivity(),
                                ids.size()
                                + " "
                                + getResources().getQuantityString(R.plurals.letter_num, ids.size())
                                + " "
                                + getResources().getString(R.string.markedRead)
                                , Toast.LENGTH_LONG);
                        onRefresh();
                    }

                    @Override
                    public void onError(LetterListRequest what, Exception e) {
                        super.onError(what, e);
                        Meow.msg(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }

                    @Override
                    public void onFinish(LetterListRequest what) {
                        super.onFinish(what);
                        setRefreshing(false);
                    }

                }).start();
    }
}
