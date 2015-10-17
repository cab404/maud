package ru.ponyhawks.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.statics.Providers;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 04:36 on 08/10/15
 *
 * @author cab404
 */
public class PublicationsFragment extends Fragment {

    @Bind(R.id.viewPager)
    ViewPager pager;
    private float elevation;
    protected String topicsUrl;
    protected String commentsUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_publications, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        final String login = Providers.UserInfo.getInstance().getInfo().username;
        topicsUrl = "/profile/" + login + "/created/topics";
        commentsUrl = "/profile/" + login + "/created/comments";
        view.setBackgroundColor(getClass().hashCode());

        pager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

            private Fragment comments;
            private Fragment topics;

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        if (topics == null)
                            topics = new RefreshableListFragment();
                        return topics;
                    case 1:
                        if (comments == null)
                            comments = new RefreshableListFragment();
                        return comments;
                    default:
                        return null;
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getActivity().getResources().getStringArray(R.array.pub_pager_titles)[position];
            }


            @Override
            public int getCount() {
                return 2;
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        final ActionBar sab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (sab == null) throw new RuntimeException("We can't live without actionbar :(");

        elevation = sab.getElevation();
        sab.setElevation(0);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        final ActionBar sab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (sab == null) throw new RuntimeException("We can't live without actionbar :(");

        sab.setElevation(elevation);
    }
}

