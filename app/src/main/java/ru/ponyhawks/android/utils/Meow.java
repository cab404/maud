package ru.ponyhawks.android.utils;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Randomness
 *
 * @author cab404
 */
public class Meow {

    @NonNull
    public static String getRandomOf(@NonNull Context ctx, @ArrayRes int stringArray){
        final String[] array = ctx.getResources().getStringArray(stringArray);
        return array[((int) (array.length * Math.random()))];
    }

}
