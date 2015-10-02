package ru.ponyhawks.android.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        if (Build.VERSION.SDK_INT <= 10) return;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't. Ask. Why.
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                openOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        System.out.println("SettingsActivity.onCreatePanelMenu");
        System.out.println("featureId = [" + featureId + "], menu = [" + menu + "]");
        set();
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        System.out.println("SettingsActivity.onMenuOpened");
        System.out.println("featureId = [" + featureId + "], menu = [" + menu + "]");
        return super.onMenuOpened(featureId, menu);
    }

    boolean created = false;

    void menu() {
        if (created) return;
        created = true;
        addPreferencesFromResource(R.xml.settings);
        if (Build.VERSION.SDK_INT >= 21)
            addPreferencesFromResource(R.xml.settings_v21);
        getPreferenceManager().findPreference("theme").setOnPreferenceChangeListener(SettingsActivity.this);
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        System.out.println("SettingsActivity.onPreparePanel");
        System.out.println("featureId = [" + featureId + "], view = [" + view + "], menu = [" + menu + "]");
        super.onPreparePanel(featureId, view, menu);
        menu();
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
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
