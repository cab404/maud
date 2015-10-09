package ru.ponyhawks.android.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<ActivityManager.AppTask> getTaskList(Context ctx, Class component) {

        ActivityManager man = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> taskList = new ArrayList<>(man.getAppTasks());
        System.out.println("tasks size : " + taskList.size());

        for (ActivityManager.AppTask task : taskList) {
            System.out.println("tasks class : " + task.getTaskInfo().baseIntent.getComponent());
            System.out.println("tasks original : " + task.getTaskInfo().origActivity);
            System.out.println("tasks desc : " + task.getTaskInfo().description);
            System.out.println("tasks desc : " + task.getTaskInfo().taskDescription.getLabel());
            System.out.println("===");
        }
        ComponentName activityComponent = new ComponentName(ctx, component);
        Iterator<ActivityManager.AppTask> tasks = taskList.iterator();

        while (tasks.hasNext())
            if (!activityComponent.equals(tasks.next().getTaskInfo().baseIntent.getComponent()))
                tasks.remove();

        return taskList;

    }


    public static Map.Entry<Integer, Integer> resolvePostUrl(Uri uri) {
        if (uri == null || uri.getLastPathSegment() == null) return null;

        String tail = uri.getLastPathSegment().replace(".html", "");
        int target = -1, topicId;
        System.out.println(tail);

        try {
            topicId = Integer.parseInt(tail);

            if (uri.getFragment() != null)
                if (uri.getFragment().startsWith("comment"))
                    target = Integer.parseInt(uri.getFragment().replace("comment", ""));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        System.out.println(topicId + "#" + target);
        return new AbstractMap.SimpleEntry<>(topicId, target);
    }

    public static void configureDocument(){

    }

}
