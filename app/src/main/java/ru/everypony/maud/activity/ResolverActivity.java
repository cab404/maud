package ru.everypony.maud.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.util.List;
import java.util.Map;

import ru.everypony.maud.utils.Meow;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 07:32 on 07/10/15
 *
 * @author cab404
 */
public class ResolverActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        System.out.println(intent.getExtras());
        System.out.println("URI " + uri);
        System.out.println("URI " + intent.getDataString());
        System.out.println("URI " + intent);
        if (uri == null) return;
        if (uri.getPathSegments().isEmpty()) return;
        System.out.println("URI " + uri);

        if ("blog".equals(uri.getPathSegments().get(0))) {
            System.out.println("BLOG " + uri);
            final Map.Entry<Integer, Integer> path = Meow.resolvePostUrl(uri);
            if (path == null) return;
            final List<TopicActivity> instances = BaseActivity.getRunning(TopicActivity.class);
            System.out.println("DISPATCH " + instances.size());

            // Checking if we already have what we need running
            for (TopicActivity act : instances) {
                System.out.println(act.getId());
                if (act.getId() == path.getKey()) {
                    System.out.println(act.getId());
                    // It's not critical to bring it to front, actually.
                    if (Build.VERSION.SDK_INT >= 11)
                        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).moveTaskToFront(act.getTaskId(), 0);
                    // What's more important is to handle move to comment
                    act.moveToComment(path.getValue());
                    return;
                }
            }
            // ...else
            final Intent start = new Intent(this, TopicActivity.class);
            if (Build.VERSION.SDK_INT >= 21)
                start.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            start.setData(uri);
            startActivity(start);

        }

        if ("talk".equals(uri.getPathSegments().get(0))) {
            System.out.println("BLOG " + uri);
            final Map.Entry<Integer, Integer> path = Meow.resolvePostUrl(uri);
            if (path == null) return;
            final List<LetterActivity> instances = BaseActivity.getRunning(LetterActivity.class);
            System.out.println("DISPATCH " + instances.size());

            // Checking if we already have what we need running
            for (LetterActivity act : instances) {
                System.out.println(act.getId());
                if (act.getId() == path.getKey()) {
                    System.out.println(act.getId());
                    // It's not critical to bring it to front, actually.
                    if (Build.VERSION.SDK_INT >= 11)
                        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).moveTaskToFront(act.getTaskId(), 0);
                    // What's more important is to handle move to comment
                    act.moveToComment(path.getValue());
                    return;
                }
            }
            // ...else
            final Intent start = new Intent(this, LetterActivity.class);
            if (Build.VERSION.SDK_INT >= 21)
                start.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            start.setData(uri);
            startActivity(start);

        }


    }
}
