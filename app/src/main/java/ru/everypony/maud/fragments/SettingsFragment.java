package ru.everypony.maud.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 23:41 on 14/05/16
 *
 * @author cab404
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
        if (Build.VERSION.SDK_INT >= 21)
            addPreferencesFromResource(R.xml.settings_v21);
        getPreferenceManager().findPreference("theme").setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference("forceRussian").setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().recreate();
        }
        return true;
    }
}
