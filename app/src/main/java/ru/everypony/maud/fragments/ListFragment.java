package ru.everypony.maud.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 23:37 on 13/09/15
 *
 * @author cab404
 */
public class ListFragment extends Fragment {

    @Bind(R.id.list)
    AbsListView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    protected int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        //noinspection ConstantConditions
        if (((AppCompatActivity) getActivity()).getSupportActionBar().isHideOnContentScrollEnabled()) {
            list.setPadding(
                    list.getPaddingLeft(),
                    list.getPaddingTop()
                            + getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),
                    list.getPaddingRight(),
                    list.getPaddingBottom()
            );
            list.setClipToPadding(false);
        }
    }

    @SuppressLint("NewApi")
    public void setAdapter(ListAdapter adapter) {
        if (list != null)
            if (Build.VERSION.SDK_INT > 10)
                list.setAdapter(adapter);
            else
                // That cast IS VERY IMPORTANT.
                //noinspection RedundantCast
                ((ListView) list).setAdapter(adapter);
    }


}
