package ru.everypony.maud.statics;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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

    String appv = "unknown";

    @Override
    public void onCreate() {
        super.onCreate();

        Providers.Preferences.getInstance().init(this);
        Providers.Profile.getInstance().init(this);

        try {
            appv = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("", e);
        }

        final ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(this)
                        .build();
        ImageLoader.getInstance().init(config);
        ConnectivityChangeBL.updateUILDownloadStats(this);

        Providers.ImgurGateway.init(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("forceRussian", false)) {
            getResources().getConfiguration().locale = new Locale("ru");
            getResources().updateConfiguration(
                    getResources().getConfiguration(),
                    getResources().getDisplayMetrics()
            );
        }

//        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//
//            File errsave = new File(Environment.getExternalStorageDirectory(), "pherrlog.txt");
//
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//                Log.e("Error", "Error", ex);
//                try {
//                    final PrintWriter writer = new PrintWriter(new FileOutputStream(errsave));
//                    ex.printStackTrace(writer);
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException("Error while writing error :/", e);
//                }
//
//                Intent email = new Intent(Intent.ACTION_SEND);
//                email.setType("text/plain");
//                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"me@cab404.ru"});
//                email.putExtra(Intent.EXTRA_SUBJECT,
//                        "phclient v" + appv + " crash on "
//                                + Build.PRODUCT +
//                                ", API " + Build.VERSION.SDK_INT);
//
//                email.putExtra(Intent.EXTRA_TEXT, "well, we've crashed. i'm not even sorry.\n" + ex.getLocalizedMessage());
//                email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(errsave));
//                email.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(email);
//
//                throw new RuntimeException();
//            }
//
//        });

    }

}
