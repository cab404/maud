package ru.ponyhawks.android.statics;

import android.app.Application;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:46 on 14/09/15
 *
 * @author cab404
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProfileProvider.getInstance().init(this);
    }
}
