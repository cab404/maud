package ru.ponyhawks.android.activity;

import android.app.ActivityManager;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.CommentEditFragment;
import ru.ponyhawks.android.fragments.TopicFragment;
import ru.ponyhawks.android.utils.HideablePartBehavior;

public class TopicActivity extends BaseActivity {

    public static final String KEY_TOPIC_ID = "topicId";

    CommentEditFragment ced;
    TopicFragment topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("title"));
        if (Build.VERSION.SDK_INT >= 21)
            setTaskDescription(
                    new ActivityManager.TaskDescription(getIntent().getStringExtra("title"))
            );

        setContentView(R.layout.activity_topic);
        ButterKnife.bind(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, TopicFragment.getInstance(getIntent().getIntExtra(KEY_TOPIC_ID, -1)))
                .commit();
        final CommentEditFragment editFragment = (CommentEditFragment) getSupportFragmentManager().findFragmentById(R.id.comment_editor);
        editFragment.hide();
        editFragment.finishTranslations();
    }

    void backToMain() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
