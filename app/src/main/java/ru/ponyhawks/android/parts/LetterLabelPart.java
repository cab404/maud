package ru.ponyhawks.android.parts;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.libph.data.LetterLabel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:24 on 23/09/15
 *
 * @author cab404
 */
public class LetterLabelPart extends MoonlitPart<LetterLabel> {
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.recipients)
    TextView recipients;
    @Bind(R.id.date)
    TextView date;
    @Bind(R.id.comment_num)
    TextView commentNum;
    @Bind(R.id.new_comments)
    TextView newComments;

    @Override
    public int getLayoutId() {
        return R.layout.part_letter_label;
    }

    @Override
    public void convert(View view, LetterLabel data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        commentNum.setText(data.comments + "");
        newComments.setVisibility(data.comments_new > 0 ? View.VISIBLE : View.GONE);
        newComments.setText("+" + data.comments_new);
        recipients.setText(TextUtils.join(", ", data.recipients));
        date.setText(
                SimpleDateFormat.getDateInstance(
                        DateFormat.LONG
                ).format(data.date.getTime())
        );
    }

}
