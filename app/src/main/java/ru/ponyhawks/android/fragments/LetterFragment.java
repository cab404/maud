package ru.ponyhawks.android.fragments;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Letter;
import com.cab404.libph.data.Type;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.LetterPage;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.requests.CommentAddRequest;
import com.cab404.libph.requests.CommentEditRequest;
import com.cab404.libph.requests.RefreshCommentsRequest;
import com.cab404.moonlight.framework.Page;

import ru.ponyhawks.android.parts.LetterPart;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.MidnightSync;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class LetterFragment extends PublicationFragment {

    public static LetterFragment getInstance(int id) {
        final LetterFragment pfrag = new LetterFragment();
        final Bundle args = new Bundle();

        args.putInt(KEY_ID, id);

        pfrag.setArguments(args);
        return pfrag;
    }

    @Override
    void prepareAdapter(ChumrollAdapter adapter) {
        adapter.prepareFor(new LetterPart());
    }


    @NonNull
    @Override
    protected CommentAddRequest getCommentAddRequest(Editable message, int reply) {
        int id = getArguments().getInt(KEY_ID);
        return new CommentAddRequest(Type.TALK, id, reply, message.toString());
    }

    @NonNull
    @Override
    protected CommentEditRequest getCommentEditRequest(Editable message, int reply) {
        return new CommentEditRequest(reply, message.toString());
    }

    @Override
    protected Page getPageRequest() {
        int id = getArguments().getInt(KEY_ID);
        return new LetterPage(id);
    }

    @Override
    protected void bindModules(MidnightSync sync) {
        sync.bind(MainPage.BLOCK_LETTER_HEADER, LetterPart.class);
    }

    @Override
    protected void handleInitialLoad(final Object object, int key) {
        switch (key) {
            case BasePage.BLOCK_LETTER_HEADER:
                Meow.inMain(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null) return;
                        getActivity().setTitle(((Letter) object).title);
                        if (Build.VERSION.SDK_INT >= 21)
                            getActivity().setTaskDescription(new ActivityManager.TaskDescription(((Letter) object).title));
                    }
                });
                break;
        }
    }


    @Override
    protected RefreshCommentsRequest getRefreshRequest(int lastCommentId) {
        int id = getArguments().getInt(KEY_ID);
        return new RefreshCommentsRequest(
                Type.TALK, id, lastCommentId
        );
    }

    @Override
    protected String getLink(){
        int id = getArguments().getInt(KEY_ID);
        return String.format("http://ponyhawks.ru/talk/%d.html", id);
    }

}
