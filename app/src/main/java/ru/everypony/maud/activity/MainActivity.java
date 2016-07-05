package ru.everypony.maud.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.cab404.libtabun.data.Blog;
import com.cab404.libtabun.data.CommonInfo;
import com.cab404.libtabun.data.Profile;
import com.cab404.libtabun.util.Tabun;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.cab404.moonlight.util.SU;
import com.google.gson.Gson;

import java.util.List;

import ru.everypony.maud.R;
import ru.everypony.maud.fragments.ConfigureDrawerFragment;
import ru.everypony.maud.fragments.DrawerContentFragment;
import ru.everypony.maud.fragments.FavouritesFragment;
import ru.everypony.maud.fragments.LetterListFragment;
import ru.everypony.maud.fragments.LoginFragment;
import ru.everypony.maud.fragments.PublicationsFragment;
import ru.everypony.maud.fragments.PublicationsListFragment;
import ru.everypony.maud.fragments.SettingsFragment;
import ru.everypony.maud.parts.DrawerEntryPart;
import ru.everypony.maud.statics.Providers;
import ru.everypony.maud.utils.Meow;

public class MainActivity extends LoginDependentActivity implements DrawerContentFragment.DrawerClickCallback {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            onNewIntent(getIntent());
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerToggle.getToolbarNavigationClickListener();
        drawerLayout.setDrawerListener(drawerToggle);

        onDrawerItemSelected(new DrawerEntryPart.Data(null, DrawerContentFragment.ID_MAIN));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        drawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    DrawerEntryPart.Data currentSection = null;

    @Override
    public void onDrawerItemSelected(DrawerEntryPart.Data data) {
        Fragment use = null;
        int id = data.id;

        if (data.equals(currentSection))
            return;

        final CommonInfo info = Providers.UserInfo.getInstance().getInfo();
        if (info == null) {
            startActivity(new Intent(MainActivity.this, SplashActivity.class));
            finish();
            return;
        }

        switch (id) {
            case DrawerContentFragment.ID_MAIN:
                use = PublicationsListFragment.getInstance("/blog/good");
                break;
            case DrawerContentFragment.ID_EDIT_LIST:
                use = new ConfigureDrawerFragment();
                break;
            case DrawerContentFragment.ID_SETTINGS:
                use = new SettingsFragment();
//                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case DrawerContentFragment.ID_MESSAGES:
                use = LetterListFragment.getInstance();
                break;
            case DrawerContentFragment.ID_FAVOURITES:
                use = new FavouritesFragment();
                break;
            case DrawerContentFragment.ID_PUBLICATIONS:
                use = new PublicationsFragment();
                break;
            case DrawerContentFragment.ID_EXIT:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.logout_promt)
                        .setPositiveButton(Meow.getRandomOf(this, R.array.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            case DrawerContentFragment.ID_BLOG:
                Blog blog = Tabun.resolveURL(data.data);
                use = PublicationsListFragment.getInstance(blog.resolveURL());
                break;
        }


        if (use != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, use, "content")
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            currentSection = data;
        }
    }

    private Gson gson = new Gson();
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onDrawerItemSelected(
                gson.fromJson(
                        savedInstanceState.getString("selected"),
                        DrawerEntryPart.Data.class
                )
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selected", gson.toJson(
                currentSection
        ));
    }

    protected void logout() {
        Providers.Preferences.getInstance().get().edit()
                .remove(LoginFragment.KEY_USERNAME)
                .remove(LoginFragment.KEY_PASSWORD)
                .commit();
        Providers.Profile.getInstance().reset();

        final Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }


}
