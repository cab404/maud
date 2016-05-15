package ru.everypony.maud.fragments;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.everypony.maud.R;
import ru.everypony.maud.statics.Providers;

/**
 * Login
 */
public class LoginFragment extends Fragment {
    public static final String KEY_PASSWORD = "login.password";
    public static final String KEY_USERNAME = "login.username";
    private LoginCallback loginCallback;

    @Bind(R.id.username)
    EditText fUsername;
    @Bind(R.id.password)
    EditText fPassword;
    @Bind(R.id.save_pwd)
    CheckBox fRemember;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.login)
    void logIn() {
        final String usermane = fUsername.getText().toString().trim();
        final String password = fPassword.getText().toString().trim();
        final boolean remember = fRemember.isChecked();

        String err = null;
        if (TextUtils.isEmpty(password))
            err = getActivity().getString(R.string.password_empty);
        if (TextUtils.isEmpty(usermane))
            err = getActivity().getString(R.string.username_empty);

        if (err != null) {
            toast(err);
            return;
        }
        loading.setVisibility(View.VISIBLE);

        new Thread() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void run() {
                super.run();
                try {
                    boolean success = Providers.Profile.get().login(usermane, password);
                    if (success) {
                        // Yay
                        if (remember) {
                            SharedPreferences store = Providers.Preferences.getInstance().get();
                            store.edit()
                                    .putString(KEY_USERNAME, usermane)
                                    .putString(KEY_PASSWORD, password)
                                    .commit();
                        }
                        Providers.Profile.getInstance().save();
                        if (loginCallback != null)
                            loginCallback.onLogin(LoginFragment.this);
                    } else {
                        toast(getActivity().getString(R.string.wrong_credentials));
                    }
                } catch (Exception e) {
                    // Errors
                    toast("Shit happens: " + e.getLocalizedMessage());
                    e.printStackTrace();
                } finally {
                    if (getView() != null)
                        getView().post(new Runnable() {
                            @Override
                            public void run() {
                                loading.setVisibility(View.GONE);
                            }
                        });
                }
            }
        }.start();

    }

    void toast(final String message) {
        if (getView() != null)
            getView().post(new Runnable() {
                @Override
                public void run() {
                    final Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
    }

    public LoginCallback getLoginCallback() {
        return loginCallback;
    }

    public void setLoginCallback(LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    public interface LoginCallback {
        void onLogin(LoginFragment fragment);
    }

}
