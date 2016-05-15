package ru.everypony.maud.utils;

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
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

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

    public static void msg(final Context ctx, final CharSequence what, final int duration) {
        inMain(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, what, duration).show();
            }
        });
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

    private static class TagColoring extends ForegroundColorSpan {
        public TagColoring(int color) {
            super(color);
        }
    }

    private static final int
            P_ST_IN_TEXT = 0,
            P_ST_IN_TAG = 1,
            P_ST_IN_PARAM = 2;

    public static void tintTags(Editable editable) {
        int p_state = P_ST_IN_TEXT;
        int startTag = 0;
        char paramType = 0;
        final int tagColor = 0xff888888;

        for (TagColoring c : editable.getSpans(0, editable.length(), TagColoring.class))
            editable.removeSpan(c);

        for (int i = 0; i < editable.length(); i++) {
            final char cChr = editable.charAt(i);
            switch (cChr) {
                case '<':
                    switch (p_state) {
                        case P_ST_IN_TEXT:
                            p_state = P_ST_IN_TAG;
                            startTag = i;
                            break;
                    }
                    break;
                case '>':
                    switch (p_state) {
                        case P_ST_IN_TAG:
                            p_state = P_ST_IN_TEXT;
                            editable.setSpan(
                                    new TagColoring(tagColor),
                                    startTag,
                                    i + 1,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                    }
                    break;
                case '\'':
                case '\"':
                    switch (p_state) {
                        case P_ST_IN_PARAM:
                            if (paramType != cChr) continue;
                            p_state = P_ST_IN_TAG;
                            break;
                        case P_ST_IN_TAG:
                            paramType = cChr;
                            p_state = P_ST_IN_PARAM;
                            break;
                    }
                    break;
            }

        }

        if (p_state == P_ST_IN_TAG) {
            editable.setSpan(
                    new TagColoring(tagColor),
                    startTag,
                    editable.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
        }

    }

}
