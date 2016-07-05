package ru.everypony.maud.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libtabun.data.Profile;
import com.cab404.libtabun.pages.ProfilePage;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ru.everypony.maud.R;
import ru.everypony.maud.activity.BaseActivity;
import ru.everypony.maud.parts.DrawerItemConfigPart;
import ru.everypony.maud.statics.Providers;
import ru.everypony.maud.utils.DrawerItemData;
import ru.everypony.maud.utils.RequestManager;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:17 on 28/06/16
 *
 * @author cab404
 */
public class ConfigureDrawerFragment extends ListFragment {

    ChumrollAdapter adapter = new ChumrollAdapter(){
        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            save();
        }
    };

    public static class DrawerItemContainer {
        public List<DrawerItemData> data = new ArrayList<>();
    }

    private Gson gson = new Gson();
    private void save() {
        if (getView() != null)
            getView().post(new Runnable() {
                @Override
                public void run() {
                    DrawerItemContainer container = new DrawerItemContainer();
                    int dcipId = adapter.typeIdOf(adapter.getConverters().getInstance(DrawerItemConfigPart.class));
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItemViewType(i) == dcipId){
                            DrawerItemData item = (DrawerItemData) adapter.getData(i);
                            container.data.add(item);
                        }
                    }
                    Providers.Preferences.getInstance().get()
                            .edit()
                            .putString("drawerItems", gson.toJson(container))
                            .apply();
                }
            });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter.prepareFor(new DrawerItemConfigPart());

        DrawerItemContainer container = gson.fromJson(
                Providers.Preferences.getInstance().get().getString("drawerItems", "{}"),
                DrawerItemContainer.class
        );

        adapter.addAll(DrawerItemConfigPart.class, container.data);

        setAdapter(adapter);
    }

    {setHasOptionsMenu(true);}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.configure, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:
                reloadData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void reloadData(){
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage(getString(R.string.loading_user_data));
        dialog.setCancelable(false);
        dialog.show();

        String username = Providers.UserInfo.getInstance().getInfo().username;
        ((BaseActivity) getActivity()).getRequestManager()
                .manage(new ProfilePage(username))
                .setHandlers(new ModularBlockParser.ParsedObjectHandler() {
                        @Override
                        public void handle(final Object object, int key) {
                            if (key == TabunPage.BLOCK_USER_INFO)
                                if (getView() != null)
                                    getView().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            update((Profile) object);
                                        }
                                    });

                        }
                    }
        ).setCallback(new RequestManager.SimpleRequestCallback<ProfilePage>() {
            @Override
            public void onError(ProfilePage what, Exception e) {
                super.onError(what, e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

            @Override
            public void onSuccess(ProfilePage what) {
                super.onSuccess(what);
                dialog.cancel();
            }
        }).start();
    }

    protected void update(Profile profile){
        List<DrawerItemData> data = new ArrayList<>();

        if (profile.get(Profile.UserInfoType.BELONGS) != null){
            HTMLTree tags = new HTMLTree(
                    profile.get(Profile.UserInfoType.BELONGS)
            );
            for (Tag a : tags.xPath("a")) {
                DrawerItemData drawerItemData = new DrawerItemData();
                drawerItemData.data = a.get("href");
                drawerItemData.name = tags.getContents(a);
                drawerItemData.deletable = true;
                data.add(drawerItemData);
            }
        }
        if (profile.get(Profile.UserInfoType.ADMIN) != null){
            HTMLTree tags = new HTMLTree(
                    profile.get(Profile.UserInfoType.ADMIN)
            );
            for (Tag a : tags.xPath("a")) {
                DrawerItemData drawerItemData = new DrawerItemData();
                drawerItemData.data = a.get("href");
                drawerItemData.name = tags.getContents(a);
                drawerItemData.deletable = true;
                data.add(drawerItemData);
            }
        }

        adapter.clear();
        adapter.addAll(DrawerItemConfigPart.class, data);

    }



}
