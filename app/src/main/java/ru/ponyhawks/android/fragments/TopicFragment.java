package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Comment;
import com.cab404.libph.data.CommonInfo;
import com.cab404.libph.data.Topic;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.pages.TopicPage;
import com.cab404.libph.util.PonyhawksProfile;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.LabelPart;
import ru.ponyhawks.android.parts.LoadingPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.SpacePart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.statics.ProfileStore;
import ru.ponyhawks.android.statics.UserInfoStore;
import ru.ponyhawks.android.utils.BatchedInsertHandler;
import ru.ponyhawks.android.utils.CompositeHandler;

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
    private CommentPart commentPart;

    public interface CommentCallback {
        public void onComment(int commentId);
    }

    public static TopicFragment getInstance(int id) {
        final TopicFragment topicFragment = new TopicFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_TOPIC_ID, id);

        topicFragment.setArguments(args);
        return topicFragment;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        topicId = getArguments().getInt(KEY_TOPIC_ID, -1);

        adapter = new ChumrollAdapter();
        final TopicPart topicPart = new TopicPart();

        commentPart = new CommentPart();
        commentPart.saveState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("saveCommentState", true);

        final SpacePart spacePart = new SpacePart();

        final LabelPart labelPart = new LabelPart();
        adapter.prepareFor(topicPart, commentPart, spacePart, new LoadingPart(), labelPart);
        final int loadingPartId = adapter.add(LoadingPart.class, null);
        setAdapter(adapter);

        final TopicPage page = new TopicPage(topicId);
        final BatchedInsertHandler insertHandler = new BatchedInsertHandler(adapter);
        page.setHandler(
                new CompositeHandler(
                        insertHandler
                                .bind(MainPage.BLOCK_TOPIC_HEADER, topicPart)
                                .bind(MainPage.BLOCK_COMMENT, commentPart)
                                .bind(MainPage.BLOCK_COMMENT_NUM, labelPart),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                switch (key) {
                                    case BasePage.BLOCK_TOPIC_HEADER:
                                        setTitleFromStream(((Topic) object).title);
                                        break;
                                    case MainPage.BLOCK_COMMON_INFO:
                                        UserInfoStore.getInstance().setInfo((CommonInfo) object);
                                        break;
                                    case MainPage.BLOCK_COMMENT:
                                        commentPart.register(((Comment) object));
                                        break;
                                }
                            }
                        }
                )
        );
        new Thread() {
            @Override
            public void run() {
                PonyhawksProfile profile = ProfileStore.get();
                try {
                    page.fetch(profile);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.removeById(loadingPartId);
                        }
                    });
                    final float dp = view.getResources().getDisplayMetrics().density;
                    // injecting bottom
                    insertHandler.inject((int) (60 * dp), spacePart);
                } catch (Exception e) {
                    getActivity().finish();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        commentPart.destroy();
    }

    void setTitleFromStream(final String title) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null)
                    //noinspection ConstantConditions
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
            }
        });
    }

}
