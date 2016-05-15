package ru.everypony.maud.fragments;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.modules.CommentTreeModule;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.pages.MainPage;
import com.cab404.libtabun.pages.TopicPage;
import com.cab404.libtabun.requests.CommentAddRequest;
import com.cab404.libtabun.requests.CommentEditRequest;
import com.cab404.libtabun.requests.RefreshCommentsRequest;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.framework.Page;

import ru.everypony.maud.parts.TopicPart;
import ru.everypony.maud.utils.Meow;
import ru.everypony.maud.utils.MidnightSync;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class TopicFragment extends PublicationFragment {

    private static final int MID_TREEFIXER = 98765;
    CommentTreeModule treeFixer = new CommentTreeModule();

    @Override
    public int getCommentBaseIndex() {
        return 2;
    }

    public static TopicFragment getInstance(int id) {
        final TopicFragment pfrag = new TopicFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_ID, id);
        pfrag.setArguments(args);
        return pfrag;
    }

    @Override
    void prepareAdapter(ChumrollAdapter adapter) {
        adapter.prepareFor(new TopicPart());
    }


    @NonNull
    @Override
    protected CommentAddRequest getCommentAddRequest(Editable message, int reply) {
        int id = getArguments().getInt(KEY_ID);
        return new CommentAddRequest(Type.BLOG, id, reply, message.toString());
    }

    @NonNull
    @Override
    protected CommentEditRequest getCommentEditRequest(Editable message, int reply) {
        return new CommentEditRequest(reply, message.toString());
    }

    @Override
    protected Page getPageRequest() {
        int id = getArguments().getInt(KEY_ID);
        return new TopicPage(id){
            @Override
            protected void bindParsers(ModularBlockParser base) {
                super.bindParsers(base);
                base.bind(treeFixer, MID_TREEFIXER);
            }

            @Override
            public void finished() {
                super.finished();
                System.out.println(treeFixer.parents);
            }

        };
    }

    @Override
    protected void bindModules(MidnightSync sync) {
        sync.bind(MainPage.BLOCK_TOPIC_HEADER, TopicPart.class);
    }

    @Override
    protected void handleInitialLoad(final Object object, int key) {
        switch (key) {
            case TabunPage.BLOCK_TOPIC_HEADER:
                Meow.inMain(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null) return;
                        getActivity().setTitle(((Topic) object).title);
                        if (Build.VERSION.SDK_INT >= 21)
                            getActivity().setTaskDescription(new ActivityManager.TaskDescription(((Topic) object).title));
                    }
                });
                break;
            case MID_TREEFIXER:
                getCommentPart().updateFrom(treeFixer);
        }
    }


    @Override
    protected RefreshCommentsRequest getRefreshRequest(int lastCommentId) {
        int id = getArguments().getInt(KEY_ID);
        return new RefreshCommentsRequest(
                Type.TOPIC, id, lastCommentId
        );
    }

    @Override
    protected String getLink(){
        int id = getArguments().getInt(KEY_ID);
        return String.format("https://tabun.everypony.ru/blog/%d.html", id);
    }

}
