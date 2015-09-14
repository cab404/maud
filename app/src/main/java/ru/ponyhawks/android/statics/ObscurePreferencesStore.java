package ru.ponyhawks.android.statics;

import android.content.Context;
import android.content.SharedPreferences;

import com.cab404.libph.util.PonyhawksProfile;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 04:22 on 14/09/15
 *
 * @author cab404
 */
public class ObscurePreferencesStore {
    private static ObscurePreferencesStore ourInstance = new ObscurePreferencesStore();
    private SharedPreferences sharedPreferences;

    public static ObscurePreferencesStore getInstance() {
        return ourInstance;
    }

    void init(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(ObscurePreferencesStore.class.getName(), 0);
    }

    public SharedPreferences get() {
        return sharedPreferences;
    }
}
