package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.pages.TopicPage;
import com.cab404.libph.util.PonyhawksProfile;

import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.statics.ProfileStore;
import ru.ponyhawks.android.utils.BatchedInsertHandler;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class TopicFragment extends ListFragment {
    public static final String KEY_TOPIC_ID = "topicId";
    ChumrollAdapter adapter;
    private int topicId;

    public static TopicFragment getInstance(int id){
        final TopicFragment topicFragment = new TopicFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_TOPIC_ID, id);

        topicFragment.setArguments(args);
        return topicFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        topicId = getArguments().getInt(KEY_TOPIC_ID, -1);

        adapter = new ChumrollAdapter();
        final TopicPart topicPart = new TopicPart();
        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                System.out.println(data.id);
            }
        });
        final CommentPart commentPart = new CommentPart();

        adapter.prepareFor(topicPart, commentPart);
        setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                PonyhawksProfile profile = ProfileStore.get();
                final TopicPage page = new TopicPage(topicId);
                page.setHandler(new BatchedInsertHandler(adapter)
                                .bind(MainPage.BLOCK_TOPIC_HEADER, topicPart)
                                .bind(MainPage.BLOCK_COMMENT, commentPart)
                );
                page.fetch(profile);
                Log.v("This", "Page fetched");
            }
        }.start();
    }

}
