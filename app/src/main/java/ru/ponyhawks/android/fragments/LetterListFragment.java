package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.pages.LetterTablePage;
import com.cab404.libph.pages.MainPage;

import ru.ponyhawks.android.parts.LetterLabelPart;
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
        adapter.prepareFor(new LetterLabelPart());

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

}
