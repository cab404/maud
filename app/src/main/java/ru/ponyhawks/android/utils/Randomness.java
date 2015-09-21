package ru.ponyhawks.android.utils;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:37 on 19/09/15
 *
 * @author cab404
 */
public class Randomness {

    @NonNull
    public static String getRandomOf(@NonNull Context ctx, @ArrayRes int stringArray){
        final String[] array = ctx.getResources().getStringArray(stringArray);
        return array[((int) (array.length * Math.random()))];
    }
}
