package ru.ponyhawks.android.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v7.internal.widget.ThemeUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ru.ponyhawks.android.R;

/**
 * Settings
 * <p/>
 * Created at 13:59 on 15/09/15
 *
 * @author cab404
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SETTINGS";

    boolean set = false;

    void set() {
        if (set) return;
        set = true;
        final TypedArray id = getTheme().obtainStyledAttributes(new int[]{R.attr.settings_theme});
        setTheme(id.getResourceId(0, 0));
    }

    @Override
    public void setContentView(int layoutResID) {
        set();
        super.setContentView(layoutResID);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().findPreference("theme").setOnPreferenceChangeListener(this);
        return super.onPreparePanel(featureId, view, menu);
    }

    boolean changedTheme = false;

    protected void onExit() {
        if (changedTheme) {
            Intent start = new Intent(this, MainActivity.class);
            if (Build.VERSION.SDK_INT >= 11)
                start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(start);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        onExit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onExit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("theme".equals(preference.getKey()))
            changedTheme = true;
        System.out.println("CH");
        return true;
    }
}
