package ru.ponyhawks.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    @Bind(R.id.scrim)
    View scrim;
    @Bind(R.id.commentFrame)
    RelativeLayout commentFrame;

    private HideablePartBehavior behavior;
    private SendCallback sendCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        final IgnorantCoordinatorLayout root = (IgnorantCoordinatorLayout) view.findViewById(R.id.root);

        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) commentFrame.getLayoutParams();

        commentFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        behavior = (HideablePartBehavior) layoutParams.getBehavior();
        behavior.setChangeCallback(this);

        root.setResizeCallback(new IgnorantCoordinatorLayout.ResizeCallback() {
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

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendCallback != null)
                    sendCallback.onSend(text.getText());
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                behavior.init(root, commentFrame);
            }
        });

    }

    boolean collapsed = true;

    @Override
    public void onHide(View view) {
        text.clearFocus();
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

    public void focus(){
        text.requestFocus();
    }

    public Editable getText(){
        return text.getText();
    }

    public interface SendCallback{
        void onSend(Editable text);
    }

    public void setSendCallback(SendCallback callback){
        sendCallback = callback;
    }

    public void hide(){
        behavior.hide(commentFrame);
    }
    public void collapse(){
        behavior.collapse(commentFrame);
    }
    public void expand(){
        behavior.expand(commentFrame);
    }

    @Override
    public void onExpandCollapse(float state) {
//        state = 1 - state;
//        scrim.setBackgroundColor(((int) (160 * state) << 24) + 0x333333);
//        if (state > 0){
//            scrim.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    return true;
//                }
//            });
//            scrim.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    behavior.collapse(commentFrame);
//                }
//            });
//        }
    }

}
