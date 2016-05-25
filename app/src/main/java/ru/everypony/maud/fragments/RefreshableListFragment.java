package ru.everypony.maud.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import butterknife.BindView;
import ru.everypony.maud.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:53 on 23/09/15
 *
 * @author cab404
 */
public class RefreshableListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.swipe_to_refresh)
    protected SwipeRefreshLayout swipeToRefresh;

    public SwipeRefreshLayout getSwipeToRefresh() {
        return swipeToRefresh;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_refreshable_list;
    }

    @Override
    @SuppressWarnings("ResourceType")
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.colorPrimary, R.attr.inverse_action_bar_color}
        );
        swipeToRefresh.setColorSchemeColors(styledAttributes.getColor(1, 0), styledAttributes.getColor(1, 0));
        swipeToRefresh.setProgressBackgroundColorSchemeColor(styledAttributes.getColor(0, 0));
        styledAttributes.recycle();

        swipeToRefresh.setOnRefreshListener(this);

    }


    protected void setRefreshing(final boolean b) {
        swipeToRefresh.post(new Runnable() {
            @Override
            public void run() {
                // workaround for refresher to show up
                swipeToRefresh.measure(swipeToRefresh.getWidth(), swipeToRefresh.getHeight());
                swipeToRefresh.setRefreshing(b);
            }
        });
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onRefresh() {
    }

}
