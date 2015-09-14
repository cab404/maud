package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.modules.TopicModule;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.util.PonyhawksProfile;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.TopicPart;
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
    ChumrollAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChumrollAdapter();
        final TopicPart topicPart = new TopicPart();
        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                System.out.println(data.id);
            }
        });
        adapter.prepareFor(topicPart);
        setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                Log.v("This", "Started cycle");
                PonyhawksProfile profile = new PonyhawksProfile();
                Log.v("This", "Login finished");
                final MainPage page = new MainPage() {
                    @Override
                    protected void bindParsers(ModularBlockParser base) {
                        super.bindParsers(base);
                        base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
                    }
                };
                page.setHandler(new BatchedInsertHandler(adapter).bind(MainPage.BLOCK_TOPIC_HEADER, topicPart));
                page.fetch(profile);
                Log.v("This", "Page fetched");
            }
        }.start();
    }


//    @Override
//    public void handle(Object object, int key) {
//        Log.v("This", "Got object ");
//        switch (key) {
//            case MainPage.BLOCK_TOPIC_HEADER:
//                adapter.add(TopicPart.class, ((Topic) object));
//                System.out.println(((Topic) object).title);
//                break;
//        }
//    }
}
