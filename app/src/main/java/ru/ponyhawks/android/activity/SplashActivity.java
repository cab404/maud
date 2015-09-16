package ru.ponyhawks.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.cab404.libph.pages.BasePage;
import com.cab404.moonlight.util.SU;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.LoginFragment;
import ru.ponyhawks.android.statics.ObscurePreferencesStore;
import ru.ponyhawks.android.statics.ProfileStore;
import ru.ponyhawks.android.statics.UserInfoStore;

public class SplashActivity extends BaseActivity implements LoginFragment.LoginCallback {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Bind(R.id.status)
    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        try {
            msg("phclient v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            msg("version unknown");
        }
        msg("=======");
        msg("starting login sequence");

        new Thread() {
            @Override
            public void run() {
                super.run();
                msg("attempting connection...");
                final BasePage basePage = new BasePage();
                try {
                    basePage.fetch(ProfileStore.get());
                } catch (Exception e) {
                    msg("failure: " + e.getLocalizedMessage());
                    return;
                }
                msg("connected to ponyhawks.ru");
                // checking if we are already logged in
                if (basePage.c_inf == null) {

                    msg("token is expired or nonexistent");
                    // otherwise trying to login with existing credentials if they are exist
                    SharedPreferences preferences = ObscurePreferencesStore.getInstance().get();
                    final String username = preferences.getString(LoginFragment.KEY_USERNAME, null);
                    final String password = preferences.getString(LoginFragment.KEY_PASSWORD, null);

                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        msg("found saved credentials, logging in as " + username);
                        try {
                            final boolean success = ProfileStore.get().login(username, password);
                            if (success) {
                                ProfileStore.getInstance().save();
                                onSuccess();
                            } else {
                                preferences.edit()
                                        .putString(LoginFragment.KEY_USERNAME, null)
                                        .putString(LoginFragment.KEY_PASSWORD, null)
                                        .apply();
                                msg("credentials are invalid");
                                syncLogin();
                            }
                        } catch (Exception e) {
                            msg("failure: " + e.getLocalizedMessage());
                        }
                    } else {
                        msg("no login data found");
                        syncLogin();
                    }

                } else {
                    UserInfoStore.getInstance().setInfo(basePage.c_inf);
                    msg("tokens are still valid, proceeding");
                    onSuccess();
                }
            }
        }.start();
    }

    void syncLogin() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                login();
            }
        });
    }

    void login() {
        msg("promting login screen");
        final LoginFragment loginFragment = new LoginFragment();
        loginFragment.setLoginCallback(this);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content_frame, loginFragment)
                .commit();
    }

    @Override
    public void onLogin(LoginFragment fragment) {
        msg("login reported success");
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(fragment)
                .commit();
        onSuccess();
    }

    void onSuccess() {

        if (UserInfoStore.getInstance().getInfo() == null) {
            msg("logged in, loading data");
            final BasePage data = new BasePage();
            try {
                data.fetch(ProfileStore.get());
            } catch (Exception e) {
                msg("failure: " + e.getLocalizedMessage());
                return;
            }
            UserInfoStore.getInstance().setInfo(data.c_inf);
        } else {
            msg("login sequence finished successfully");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }


    final StringBuilder messages = new StringBuilder();

    void msg(String message) {
        synchronized (messages) {
            messages.append(message).append('\n');
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (messages) {
                    status.setText(messages);
                }
            }
        });
    }

    void onFailure() {
        msg("fatal failure");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

}
