package ru.everypony.maud.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import ru.everypony.maud.statics.Providers;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:08 on 08/10/15
 *
 * @author cab404
 */
public class FavouritesFragment extends PublicationsFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String login = Providers.UserInfo.getInstance().getInfo().username;
        topicsUrl = "/profile/" + login + "/favourites/topics";
        commentsUrl = "/profile/" + login + "/favourites/comments";
    }
}
