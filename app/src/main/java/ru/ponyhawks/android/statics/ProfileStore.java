package ru.ponyhawks.android.statics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cab404.libph.util.PonyhawksProfile;

import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpRequestBase;

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

    private void regen(final String from) {
        profile = new PonyhawksProfile() {
            {
                if (from != null)
                    setUpFromString(from);
            }

            @Override
            public HttpResponse exec(HttpRequestBase request, boolean follow, int timeout) {
                final RequestLine rl = request.getRequestLine();
                Log.d("Moonlight", rl.getMethod() + " " + rl.getUri());
                return super.exec(request, follow, timeout);
            }
        };

    }

    public void reset(){
        regen(null);
        save();
        sharedPreferences.edit().commit();
    }

    private ProfileStore() {
        regen(null);
    }

    void init(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(ProfileStore.class.getName(), 0);
        final String session = sharedPreferences.getString(KEY_SESSION, null);
        if (session != null)
            try {
                regen(session);
            } catch (Exception e) {
                regen(null);
                e.printStackTrace();
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
        sharedPreferences.edit().putString(KEY_SESSION, profile.serialize()).apply();
    }


}
