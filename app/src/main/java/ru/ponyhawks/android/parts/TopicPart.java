package ru.ponyhawks.android.parts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.libph.data.Topic;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.DateUtils;
import ru.ponyhawks.android.text.StaticWebView;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:58 on 14/09/15
 *
 * @author cab404
 */
public class TopicPart extends MoonlitPart<Topic> {
    public static final DisplayImageOptions IMG_CFG =
            new DisplayImageOptions.Builder().cacheInMemory(true).build();

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.text)
    StaticWebView text;
    @Bind(R.id.author)
    TextView author;
    @Bind(R.id.avatar)
    ImageView avatar;
    @Bind(R.id.comment_num)
    TextView comments;
    @Bind(R.id.date)
    TextView date;


    private TopicPartCallback callback;

    @Override
    public void convert(View view, final Topic data, int index, ViewGroup parent) {
        super.convert(view, data, index, parent);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        text.setText(data.text);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showActionDialog(data, v.getContext());
                return true;
            }
        });

        String cc = data.comments > 0 ? data.comments + " " : "";
        cc += data.comments_new > 0 ? "+" + data.comments_new + " ": "";
        cc += view.getResources().getQuantityString(
                R.plurals.comment_num,
                data.comments_new > 0 ? data.comments_new : data.comments
        );
        comments.setVisibility(cc.isEmpty() ? View.GONE : View.VISIBLE);
        comments.setText(cc);

        date.setText(DateUtils.formPreciseDate(data.date));

        author.setText(data.author.login);
        avatar.setImageDrawable(null);
        if (!data.author.is_system) {
            ImageLoader.getInstance().displayImage(data.author.small_icon, avatar, IMG_CFG);
        }

    }

    public interface TopicPartCallback {
        void onFavInvoked(Topic cm, Context context);

        void onShareInvoked(Topic cm, Context context);

        void onReplyInvoked(Topic cm, Context context);

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_topic;
    }

    void showActionDialog(final Topic topic, Context ctx) {
        final int theme = ctx
                .getTheme()
                .obtainStyledAttributes(new int[]{R.attr.alert_dialog_nobg_theme})
                .getResourceId(0, 0);

        @SuppressLint("InflateParams") final
        View controls = LayoutInflater.from(ctx)
                .inflate(R.layout.alert_topic_controls, null, false);

        final AlertDialog dialog = new AlertDialog
                .Builder(ctx, theme)
                .setView(controls)
                .show();

        final ImageView fav = (ImageView) controls.findViewById(R.id.fav);
        final ImageView share = (ImageView) controls.findViewById(R.id.copy_link);

        fav.setImageResource(
                topic.in_favourites ?
                        R.drawable.ic_star :
                        R.drawable.ic_star_outline
        );

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onFavInvoked(topic, v.getContext());
                dialog.dismiss();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onShareInvoked(topic, v.getContext());
                dialog.dismiss();
            }
        });
    }


}
