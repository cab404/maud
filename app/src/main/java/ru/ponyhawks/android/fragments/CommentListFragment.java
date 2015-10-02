package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.modules.CommentModule;
import com.cab404.libph.pages.MainPage;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:19 on 30/09/15
 *
 * @author cab404
 */
public class CommentListFragment extends RefreshableListFragment {

    String url;
    MidnightSync sync;
    ChumrollAdapter adapter;


    public static CommentListFragment getInstance(String url) {
        final CommentListFragment clf = new CommentListFragment();
        Bundle args = new Bundle(1);
        args.putString("url", url);
        clf.setArguments(args);
        return clf;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = getArguments().getString("url");

        onRefresh();
        setRefreshing(true);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();


        adapter = new ChumrollAdapter();
        sync = new MidnightSync(adapter);

        MainPage comments = new MainPage() {

            @Override
            public String getURL() {
                return url;
            }

            @Override
            protected void bindParsers(ModularBlockParser base) {
                super.bindParsers(base);
                base.bind(new CommentModule(CommentModule.Mode.LIST), BLOCK_COMMENT);
            }

        };

        RequestManager
                .fromActivity(getActivity())
                .manage(comments)
                .setHandlers(new UpdateCommonInfoTask(), sync);

    }


}
