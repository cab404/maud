package ru.ponyhawks.android.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.CommentEditFragment;
import ru.ponyhawks.android.fragments.TopicFragment;

public class TopicActivity extends BaseActivity {

    public static final String KEY_TOPIC_ID = "topicId";

    CommentEditFragment ced;
    TopicFragment topic;

    void backToMain() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("title"));

        if (Build.VERSION.SDK_INT >= 21)
            setTaskDescription(
                    new ActivityManager.TaskDescription(getIntent().getStringExtra("title"))
            );

//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_topic);

//        getSupportActionBar().setHideOnContentScrollEnabled(true);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction()
                .replace(
                        R.id.content_frame,
                        topic = TopicFragment.getInstance(getIntent().getIntExtra(KEY_TOPIC_ID, -1))
                )
                .commit();

        ced = (CommentEditFragment) getSupportFragmentManager().findFragmentById(R.id.comment_editor);
        topic.setCommentFragment(ced);

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onBackPressed() {
        backToMain();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                backToMain();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
