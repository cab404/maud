package ru.ponyhawks.android.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

/**
 * Randomness
 *
 * @author cab404
 */
public class Meow {

    @NonNull
    public static String getRandomOf(@NonNull Context ctx, @ArrayRes int stringArray) {
        final String[] array = ctx.getResources().getStringArray(stringArray);
        return array[((int) (array.length * Math.random()))];
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void inMain(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread())
            runnable.run();
        else
            handler.post(runnable);
    }
}
