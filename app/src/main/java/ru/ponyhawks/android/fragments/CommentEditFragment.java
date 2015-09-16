package ru.ponyhawks.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.utils.HideablePartBehavior;
import ru.ponyhawks.android.utils.IgnorantCoordinatorLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentEditFragment extends Fragment implements HideablePartBehavior.ChangeCallback {

    @Bind(R.id.text)
    EditText text;
    @Bind(R.id.target)
    TextView target;
    @Bind(R.id.send)
    ImageView send;

    private HideablePartBehavior behavior;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        final View commentFrame = view.findViewById(R.id.commentFrame);
        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) commentFrame.getLayoutParams();

        behavior = (HideablePartBehavior) layoutParams.getBehavior();
        ((IgnorantCoordinatorLayout) view.findViewById(R.id.root)).setResizeCallback(new IgnorantCoordinatorLayout.ResizeCallback() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                commentFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        behavior.sync(commentFrame);
                    }
                });
            }
        });
        behavior.setChangeCallback(this);
        view.post(new Runnable() {
            @Override
            public void run() {
                behavior.lockOn(commentFrame);
            }
        });
        behavior.sync(commentFrame);
    }

    boolean collapsed = true;

    @Override
    public void onHide(View view) {

    }

    @Override
    public void onExpand(View view) {
        if (!collapsed) return;
        if (getView() == null) return;
        collapsed = false;
        ((RelativeLayout.LayoutParams) send.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = 0;
        ((RelativeLayout.LayoutParams) target.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = R.id.send;
        ((RelativeLayout.LayoutParams) text.getLayoutParams()).getRules()[RelativeLayout.LEFT_OF] = 0;

        text.setVerticalScrollBarEnabled(true);
        text.setSingleLine(false);
        text.setMaxLines(5);
    }

    @Override
    public void onCollapse(View view) {
        if (collapsed) return;
        if (getView() == null) return;
        collapsed = true;
        ((RelativeLayout.LayoutParams) send.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = R.id.text;
        ((RelativeLayout.LayoutParams) target.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = 0;
        ((RelativeLayout.LayoutParams) text.getLayoutParams()).getRules()[RelativeLayout.LEFT_OF] = R.id.send;

        text.setSingleLine(true);
        text.setVerticalScrollBarEnabled(false);

        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

}
