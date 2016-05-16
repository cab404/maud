package ru.everypony.maud.statics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 04:52 on 16/05/16
 *
 * @author cab404
 */
public class ConnectivityChangeBL extends BroadcastReceiver {

    public static boolean isNetDenied(Context ctx) {
        boolean denied = !PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean("allowMobileNetworkDownloads", false);
        if (denied){
            NetworkInfo activeNetworkInfo =
                    ((ConnectivityManager) ctx
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo();
            return activeNetworkInfo != null && ConnectivityManager.TYPE_MOBILE == activeNetworkInfo.getType();
        } else
            return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateUILDownloadStats(context);
    }

    public static void updateUILDownloadStats(Context context){
        ImageLoader.getInstance().denyNetworkDownloads(isNetDenied(context));
    }
}
