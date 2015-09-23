package ru.ponyhawks.android.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
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

import com.cab404.moonlight.parser.Tag;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.utils.HideablePartBehavior;
import ru.ponyhawks.android.utils.IgnorantCoordinatorLayout;
import ru.ponyhawks.android.utils.ImageChooser;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentEditFragment extends Fragment implements HideablePartBehavior.ChangeCallback, ImageChooser.ImageUrlHandler {

    @Bind(R.id.text)
    EditText text;
    @Bind(R.id.target)
    TextView target;
    @Bind(R.id.send)
    ImageView send;
    @Bind(R.id.commentFrame)
    RelativeLayout commentFrame;
    @Bind(R.id.root)
    IgnorantCoordinatorLayout root;

    private HideablePartBehavior behavior;
    private SendCallback sendCallback;
    private ImageChooser chooser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) commentFrame.getLayoutParams();

        chooser = new ImageChooser(this, this);

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

        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    expand();
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
        text.requestFocus();
        text.setMaxLines(5);

        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(text, 0);
    }

    @Override
    public void onCollapse(View view) {
        if (collapsed) return;
        if (getView() == null) return;
        collapsed = true;
        ((RelativeLayout.LayoutParams) send.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = R.id.text;
        ((RelativeLayout.LayoutParams) target.getLayoutParams()).getRules()[RelativeLayout.ALIGN_BOTTOM] = 0;
        ((RelativeLayout.LayoutParams) text.getLayoutParams()).getRules()[RelativeLayout.LEFT_OF] = R.id.send;

        text.clearFocus();
        text.setSingleLine(true);
        text.setVerticalScrollBarEnabled(false);

        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    public void focus() {
        text.requestFocus();
    }

    public Editable getText() {
        return text.getText();
    }

    public void setTarget(CharSequence target) {
        this.target.setText(target);
    }

    public void pin() {
        behavior.lockOn(commentFrame);
    }

    public void clear() {
        text.setText("");
    }

    public interface SendCallback {
        void onSend(Editable text);
    }

    public void setSendCallback(SendCallback callback) {
        sendCallback = callback;
    }

    public void hide() {
        behavior.hide(commentFrame);
    }

    public void collapse() {
        behavior.collapse(commentFrame);
    }

    public void expand() {
        behavior.expand(commentFrame);
    }

    public void finishTranslations() {
        behavior.syncImmediate(commentFrame);
    }

    @Override
    public void onExpandCollapse(float state) {}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        chooser.handleActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.image)
    public void onImageChooseClick(){
        chooser.requestImageSelection(false);
    }

    @Override
    public void handleImage(final String image) {
        System.out.println("IMAGE SELECTED !!! " + image);
        text.post(new Runnable() {
            @Override
            public void run() {
                int cursor = text.getSelectionStart();
                if (cursor == -1) cursor = 0;

                final Tag tag = new Tag();
                tag.name = "img";
                tag.props.put("src", image);
                tag.type = Tag.Type.STANDALONE;
                final Editable editable = text.getText();
                editable.insert(cursor, tag.toString());
                text.setText(editable);
            }
        });
    }


}
