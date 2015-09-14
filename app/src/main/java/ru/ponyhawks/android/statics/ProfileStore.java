package ru.ponyhawks.android.statics;

import android.content.Context;
import android.content.SharedPreferences;

import com.cab404.libph.util.PonyhawksProfile;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:36 on 14/09/15
 *
 * @author cab404
 */
public class ProfileStore {
    public static final String KEY_SESSION = "session";
    private static ProfileStore ourInstance = new ProfileStore();
    private SharedPreferences sharedPreferences;

    public static ProfileStore getInstance() {
        return ourInstance;
    }

    PonyhawksProfile profile;

    private ProfileStore() {
        profile = new PonyhawksProfile();
    }

    void init(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(ProfileStore.class.getName(), 0);
        final String session = sharedPreferences.getString(KEY_SESSION, null);
        if (session != null)
            try {
                profile = PonyhawksProfile.parseString(session);
            } catch (Exception e) {
                profile = new PonyhawksProfile();
                save();
            }
    }

    public static PonyhawksProfile get() {
        return getInstance().getProfile();
    }

    public PonyhawksProfile getProfile() {
        return profile;
    }

    public void save() {
        sharedPreferences.edit().putString(KEY_SESSION, profile.toString()).apply();
    }


}
