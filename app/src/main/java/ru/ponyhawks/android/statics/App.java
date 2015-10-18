package ru.ponyhawks.android.statics;

import android.app.Application;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Locale;

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

        Providers.Preferences.getInstance().init(this);
        Providers.Profile.getInstance().init(this);

        final ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(this)
                        .build();
        ImageLoader.getInstance().init(config);

        Providers.ImgurGateway.init(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("forceRussian", false)) {
            getResources().getConfiguration().locale = new Locale("ru");
            getResources().updateConfiguration(
                    getResources().getConfiguration(),
                    getResources().getDisplayMetrics()
            );
        }

//        final File errsave = new File(Environment.getExternalStorageDirectory(), "pherrlog.txt");
//        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//                try {
//                    final PrintWriter writer = new PrintWriter(new FileOutputStream(errsave));
//                    ex.printStackTrace(writer);
//                    writer.close();
//                    throw new RuntimeException("Error on main thread", ex);
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException("Error while writing error :/", e);
//                }
//
//            }
//        });
    }


}
