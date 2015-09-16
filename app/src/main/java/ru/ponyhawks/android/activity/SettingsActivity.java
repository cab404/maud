package ru.ponyhawks.android.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.PersistableBundle;
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
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:59 on 15/09/15
 *
 * @author cab404
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SETTINGS";

    boolean set = false;
    void set(){
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
        return super.onPreparePanel(featureId, view, menu);
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
