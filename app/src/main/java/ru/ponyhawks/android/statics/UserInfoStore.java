package ru.ponyhawks.android.statics;

import com.cab404.libph.data.CommonInfo;

import java.util.Observable;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:40 on 14/09/15
 *
 * @author cab404
 */
public class UserInfoStore extends Observable {
    private static UserInfoStore ourInstance = new UserInfoStore();

    public static UserInfoStore getInstance() {
        return ourInstance;
    }

    CommonInfo info = null;

    public synchronized CommonInfo getInfo() {
        return info;
    }

    public synchronized void setInfo(CommonInfo info) {
        if (info == null)
            return;
        this.info = info;
        notifyObservers(info);
    }
}
