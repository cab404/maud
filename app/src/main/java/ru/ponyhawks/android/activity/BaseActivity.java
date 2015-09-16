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
        final String s = sp.getString("theme", "Dark");
        final String[] array = getResources().getStringArray(R.array.themes);
        int i;
        for (i = 0; i < array.length; i++)
            if (array[i].equals(s))
                break;

        String theme = getResources().getStringArray(R.array.theme_values)[i];
        final int id = getResources().getIdentifier(theme, "style", getPackageName());
        setTheme(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
    }
}
