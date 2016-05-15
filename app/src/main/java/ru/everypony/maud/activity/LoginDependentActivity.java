package ru.everypony.maud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import ru.everypony.maud.statics.Providers;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:59 on 05/10/15
 *
 * @author cab404
 */
public class LoginDependentActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        if (Providers.UserInfo.getInstance().getInfo() == null) {
            final Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("returnTo", getIntent());
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState, persistentState);
    }
}
