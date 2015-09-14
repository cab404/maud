package ru.ponyhawks.android.statics;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:40 on 14/09/15
 *
 * @author cab404
 */
public class UserInfoStore {
    private static UserInfoStore ourInstance = new UserInfoStore();

    public static UserInfoStore getInstance() {
        return ourInstance;
    }



    private UserInfoStore() {

    }
}
