package ru.ponyhawks.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.CommonInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.DrawerEntryPart;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.UserHeaderPart;
import ru.ponyhawks.android.statics.Providers;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawerContentFragment extends ListFragment implements Observer {

    DrawerClickCallback callback;
    ChumrollAdapter adapter;

    public static final int
            ID_MESSAGES = 0,
            ID_SETTINGS = 1,
            ID_FAVOURITES = 2,
            ID_PUBLICATIONS = 3,
            ID_EXIT = 4,
            ID_MAIN = 5;

    public DrawerContentFragment() {
        adapter = new ChumrollAdapter();
        final DrawerEntryPart entryPart = new DrawerEntryPart();
        adapter.prepareFor(entryPart, new UserHeaderPart());
        entryPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<DrawerEntryPart.Data>() {
            @Override
            public void onClick(DrawerEntryPart.Data data, View view) {
                if (callback != null)
                    callback.onDrawerItemSelected(data.id);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DrawerClickCallback)
            callback = (DrawerClickCallback) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Providers.UserInfo.getInstance().addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Providers.UserInfo.getInstance().deleteObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        update(Providers.UserInfo.getInstance().getInfo());
    }

    void update(CommonInfo info) {
        if (info == null) {
            return;
        }
        setAdapter(null);
        adapter.clear();

        adapter.add(UserHeaderPart.class, info);
        List<DrawerEntryPart.Data> points = new ArrayList<>();
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.main_page_label), ID_MAIN));
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.messages_label), ID_MESSAGES, info.new_messages));
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.publications_label), ID_PUBLICATIONS));
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.favourites), ID_FAVOURITES));
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.settings_label), ID_SETTINGS));
        points.add(new DrawerEntryPart.Data(getActivity().getString(R.string.logout_label), ID_EXIT));
        adapter.addAll(DrawerEntryPart.class, points);

        setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer_content, container, false);
    }

    @Override
    public void update(Observable observable, final Object data) {
        if (getView() != null)
            getView().post(new Runnable() {
                @Override
                public void run() {
                    update((CommonInfo) data);
                }
            });
    }

    public interface DrawerClickCallback {
        void onDrawerItemSelected(int id);
    }

}
