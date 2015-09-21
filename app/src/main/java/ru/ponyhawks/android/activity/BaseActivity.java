package ru.ponyhawks.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;

import ru.ponyhawks.android.R;

/**
 * Base themed activity
 *
 * @author cab404
 */
public class BaseActivity extends AppCompatActivity {

    private void setupTheme() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String theme = sp.getString("theme", "AppThemeDark");

        int id = getResources().getIdentifier(theme, "style", getPackageName());
        if (id == 0) id = R.style.AppThemeDark;

        setTheme(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
    }
}
