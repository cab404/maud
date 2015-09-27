package ru.ponyhawks.android.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.changers.ImportImageTextChanger;
import ru.ponyhawks.android.text.changers.SimpleChangers;
import ru.ponyhawks.android.text.changers.TextChanger;
import ru.ponyhawks.android.text.changers.TextPrism;
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
    @Bind(R.id.commentFrame)
    RelativeLayout commentFrame;
    @Bind(R.id.root)
    IgnorantCoordinatorLayout root;
    @Bind(R.id.instruments)
    LinearLayout instrumentsLayout;

    private HideablePartBehavior behavior;
    private SendCallback sendCallback;

    private List<TextChanger> instruments;

    {
        instruments = new ArrayList<>();
        instruments.add(new ImportImageTextChanger());
        instruments.add(SimpleChangers.BOLD);
        instruments.add(SimpleChangers.ITALIC);
        instruments.add(SimpleChangers.UNDERLINE);
        instruments.add(SimpleChangers.STRIKETHROUGH);
        instruments.add(SimpleChangers.SPAN_LEFT);
        instruments.add(SimpleChangers.SPAN_CENTER);
        instruments.add(SimpleChangers.SPAN_RIGHT);
    }

    private List<TextPrism> postprocessors;

    {
        postprocessors = new ArrayList<>();

    }

    private int selectedInstrument = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

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

        instrumentsLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        for (final TextChanger changer : instruments) {
            View button = inflater.inflate(R.layout.include_instrument_button, instrumentsLayout, false);
            ((ImageView) button.findViewById(R.id.icon)).setImageResource(changer.getImageResource());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changer.onSelect(CommentEditFragment.this, text);
                    selectedInstrument = instruments.indexOf(changer);
                }
            });
            instrumentsLayout.addView(button);
        }

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
    public void onExpandCollapse(float state) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        instruments.get(selectedInstrument).onActivityResult(requestCode, resultCode, data);
    }

    @OnFocusChange(R.id.text)
    public void onTextFocusChange(boolean hasFocus) {
        if (hasFocus)
            expand();
    }

    @OnClick(R.id.send)
    public void onSendInvoked() {
        if (sendCallback != null) {
            Editable text = this.text.getText();
            for (TextPrism prism : postprocessors)
                text = prism.purify(text);
            sendCallback.onSend(text);
        }
    }

}
