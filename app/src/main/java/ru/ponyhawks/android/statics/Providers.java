package ru.ponyhawks.android.statics;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.cab404.libph.data.CommonInfo;
import com.cab404.libph.util.PonyhawksProfile;

import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Observable;

import ru.ponyhawks.android.utils.Imgur;

/**
 * Bunch of static managers
 * <p/>
 * Created at 01:22 on 23/09/15
 *
 * @author cab404
 */
public class Providers {

    /**
     * Manages imgur gateway token
     * <p/>
     * Created at 01:17 on 23/09/15
     *
     * @author cab404
     */
    public static class ImgurGateway {

        private static Imgur.Gateway gateway;

        static void init(Context context) {
            try {
                String token = context
                        .getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                        .metaData.getString("imgur-api-key");
                gateway = new Imgur.Gateway(token);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException("", e);
            }
        }

        public static Imgur.Gateway get() {
            return gateway;
        }

    }

    /**
     * Manages profile saving and loading
     * <p/>
     * Created at 02:36 on 14/09/15
     *
     * @author cab404
     */
    public static class Profile {
        public static final String KEY_SESSION = "session";
        private static Profile ourInstance = new Profile();
        private SharedPreferences sharedPreferences;

        public static Profile getInstance() {
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

        public void reset() {
            regen(null);
            save();
            sharedPreferences.edit().commit();
        }

        private Profile() {
            regen(null);
        }

        void init(Context ctx) {
            sharedPreferences = ctx.getSharedPreferences(Profile.class.getName(), 0);
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

    /**
     * Manages unlisted preferences
     * <p/>
     * Created at 04:22 on 14/09/15
     *
     * @author cab404
     */
    public static class Preferences {
        private static Preferences ourInstance = new Preferences();
        private SharedPreferences sharedPreferences;

        public static Preferences getInstance() {
            return ourInstance;
        }

        void init(Context ctx) {
            sharedPreferences = ctx.getSharedPreferences(Preferences.class.getName(), 0);
        }

        public SharedPreferences get() {
            return sharedPreferences;
        }
    }

    /**
     * Manages CommonInfo object, and its traversal through activities
     * <p/>
     * Created at 14:40 on 14/09/15
     *
     * @author cab404
     */
    public static class UserInfo extends Observable {
        private static UserInfo ourInstance = new UserInfo();

        public static UserInfo getInstance() {
            return ourInstance;
        }

        CommonInfo info = null;

        public synchronized CommonInfo getInfo() {
            return info;
        }

        public synchronized void setInfo(CommonInfo info) {
            if (info == null)
                return;
            setChanged();
            this.info = info;
            notifyObservers(info);
        }
    }
}
