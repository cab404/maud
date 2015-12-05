package ru.ponyhawks.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.cab404.libph.pages.BasePage;
import com.cab404.libph.requests.LSRequest;
import com.cab404.libph.requests.LoginRequest;
import com.cab404.moonlight.framework.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.LoginFragment;
import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.RequestManager;

public class SplashActivity extends BaseActivity implements LoginFragment.LoginCallback {

    public static final String HAWK = "             :+?7   77?:  \n" +
            "           I     ?    ? 7~\n" +
            "          =     7?    , 7?\n" +
            "         =         7?I  77\n" +
            "         I               I\n" +
            "        +777+7777777=   I \n" +
            "       : 7 :I     7+      \n" +
            "       =777 777777 ?      \n" +
            "       777~~77777777      \n" +
            "      ,777,+77777777,     \n" +
            "      =  ? 77?777777      \n" +
            "      ?7 :~,   777 =      \n" +
            "      I   ,  :77777,      \n" +
            "       7+ +? ?7777:       \n" +
            "     , 7~ 7= 7777?        \n" +
            "     ~ 7 ?7 +777+         \n" +
            "     + =~ :,77 :          \n" +
            "     ? :+7 +77=           \n" +
            "    ,7?:7,:7I             \n" +
            "    :7~+? +I              \n" +
            "    I?,? ,                \n" +
            "   =? +                   \n" +
            "   I,,:                   \n" +
            "  +                       \n" +
            " ,,                       \n";
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Bind(R.id.status)
    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        msg(HAWK);

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
                // App update thread
                String appv;
                try {
                    appv = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException("", e);
                }

                String server_version = null;
                try {
                    final HttpURLConnection connection = (HttpURLConnection) new URL("http://cab404.ru/all/bin/ph/version.txt").openConnection();
                    connection.setInstanceFollowRedirects(false);
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    server_version = reader.readLine();

                } catch (IOException e) {
                    System.out.println("do not know newest version");
                }

                if (server_version != null && server_version.length() > 30)
                    server_version = null;

                if (server_version == null) {
                    System.out.println("okay, no data on new versions");
                } else if (appv.equals(server_version)) {
                    System.out.println("our version is okay");
                } else {
                    // now updating.
                    System.out.println("okay, update");

                    Meow.inMain(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(SplashActivity.this)
                                    .setMessage(R.string.wehavegotanupdate)
                                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cab404.ru/all/bin/ph/client.apk"));
                                            view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(view);
                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            threadLoginSeq();
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            threadLoginSeq();
                                        }
                                    }).show();
                        }
                    });
                    return;
                }
                threadLoginSeq();
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

    void threadLoginSeq() {
        new Thread() {
            @Override
            public void run() {
                try {
                    loginSeq();
                } catch (InterruptedException e) {
                    throw new RuntimeException("WHAT", e);
                }
            }
        }.start();
    }

    void loginSeq() throws InterruptedException {

        msg("attempting connection...");
        final BasePage basePage = new BasePage();
        basePage.setHandler(new UpdateCommonInfoTask());
        getRequestManager()
                .manage(basePage)
                .setCallback(new RetryCallback<BasePage>())
                .run();

        // checking if we are already logged in
        if (basePage.c_inf == null) {
            msg("token is expired or nonexistent");
            // otherwise trying to login with existing credentials if they are exist
            SharedPreferences preferences = Providers.Preferences.getInstance().get();
            final String username = preferences.getString(LoginFragment.KEY_USERNAME, null);
            final String password = preferences.getString(LoginFragment.KEY_PASSWORD, null);

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                msg("found saved credentials, logging in as " + username);
                login(username, password);
            } else {
                msg("no login data found");
                syncLogin();
            }

        } else {
            Providers.UserInfo.getInstance().setInfo(basePage.c_inf);
            msg("tokens are still valid, proceeding");
            onSuccess();
        }
    }

    void login(String username, String password) {
        if (!Providers.Profile.get().cookies.containsKey(LSRequest.LS_KEY_ENTRY))
            getRequestManager()
                    .manage(new BasePage())
                    .setCallback(new RetryCallback<BasePage>())
                    .run();
        getRequestManager()
                .manage(new LoginRequest(username, password))
                .setCallback(new RetryCallback<LoginRequest>() {
                    @Override
                    public void onSuccess(LoginRequest what) {
                        if (what.success()) {
                            msg("re-logged in");
                            SplashActivity.this.onSuccess();
                            Providers.Profile.getInstance().save();
                        } else {
                            Providers.Preferences.getInstance().get().edit()
                                    .putString(LoginFragment.KEY_USERNAME, null)
                                    .putString(LoginFragment.KEY_PASSWORD, null)
                                    .apply();
                            msg("credentials are invalid");
                            syncLogin();
                        }
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

    /* Checks if userdata is in place */
    void preFlightCheck() {
        if (Providers.UserInfo.getInstance().getInfo() == null) {
            msg("no user data found, loading data for navigation");
            final BasePage basePage = new BasePage();
            basePage.setHandler(new UpdateCommonInfoTask());
            getRequestManager().manage(basePage)
                    .setCallback(new RetryCallback<BasePage>())
                    .run();
        } else {
            msg("navigation data is in place");
        }
    }

    void onSuccess() {
        preFlightCheck();
        msg("everything is ready to go");
        if (getIntent().hasExtra("returnTo"))
            msg("returning back to caller");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent returnTo = new Intent(SplashActivity.this, MainActivity.class);
                if (getIntent().getParcelableExtra("returnTo") != null)
                    returnTo = getIntent().getParcelableExtra("returnTo");
                startActivity(returnTo);
                finish();
            }
        }, 1500);
    }


    final StringBuilder messages = new StringBuilder();

    void msg(final String message) {
        synchronized (messages) {
            messages.append(message).append('\n');
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (messages) {
                    float count = 0;
                    int i = 0;
                    int ni;

                    float[] width = new float[1];
                    status.getPaint().getTextWidths(" ", width);

                    float lineWidth = status.getWidth();
                    if (lineWidth != 0) {
                        while ((ni = messages.indexOf("\n", i + 1)) != -1) {
                            count += Math.ceil(width[0] / lineWidth * (ni - i));
                            i = ni;
                        }
                        final float limit = status.getHeight() / status.getLineHeight() * 0.9f;
                        while (count-- > limit)
                            messages.delete(0, messages.indexOf("\n") + 1);
                    }

                    status.setText(messages);
                }
            }
        });
    }

    private class RetryCallback<A extends Request> extends RequestManager.SimpleRequestCallback<A> {
        int retries = 0;

        @Override
        public void onError(A what, Exception e) {
            msg(retries++ + " - failure: " + e.getLocalizedMessage());
            msg("retrying in 3 seconds");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            retry(what);
        }

        @Override
        public void onSuccess(A what) {
            msg("done");
        }
    }
}
