package ru.ponyhawks.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Base themed activity
 *
 * @author cab404
 */
public class BaseActivity extends AppCompatActivity {

    RequestManager manager = new RequestManager(Providers.Profile.get());

    private void setupTheme() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String theme = sp.getString("theme", "AppThemeDark");

        int id = getResources().getIdentifier(theme, "style", getPackageName());
        if (id == 0) id = R.style.AppThemeDark;

        setTheme(id);

    }

    public RequestManager getRequestManager() {
        return manager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.cancelAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("removeShadow", false))
            if (getSupportActionBar() != null)
                getSupportActionBar().setElevation(0);
    }

}
