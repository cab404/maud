package ru.ponyhawks.android.text;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;
import com.cab404.moonlight.util.SU;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.spans.BaselineJumpSpan;
import ru.ponyhawks.android.text.spans.DoubleClickableSpan;
import ru.ponyhawks.android.text.spans.LitespoilerSpan;

/**
 * Rips html into view list
 *
 * @author cab404
 */
public class HtmlRipper {

    private ViewGroup layout;
    private Collection<Runnable> onDestroy;
    private Collection<Runnable> onLayout;
    private List<View> cached_contents;
    private Context ctx;

    public String hostname = "ponyhawks.ru";
    public String scheme = "http";

    public final int replacerImage = android.R.drawable.ic_menu_gallery;
    public final int errorImage = android.R.drawable.ic_dialog_alert;

    public int spoilerLabelColor = 0xffffffff;
    public int codeColor = 0xffffffff;
    public int green = 0xff00c155;
    public int blue = 0xff009ad1;
    public int red = 0xffd30035;

    public boolean textIsSelectable;
    public boolean loadVideos;
    public boolean loadImages;

    public int cutBackground = R.drawable.quote_background;
    public int codeBackground = R.drawable.quote_background;
    public int quoteBackground = R.drawable.quote_background;

    public int spoilerBodyLayout = R.layout.ripper_part_spoiler;
    public int spoilerHeaderId = R.id.header;
    public int spoilerBodyId = R.id.body;

    public final Rect imageReplacerSize;
    public int internalMargins;


    public HtmlRipper(ViewGroup layout) {
        cached_contents = new ArrayList<>();
        onDestroy = new ArrayList<>();
        onLayout = new ArrayList<>();
        ctx = layout.getContext();
        this.layout = layout;
        float dp = ctx.getResources().getDisplayMetrics().density;
        imageReplacerSize = new Rect(0, 0, (int) (30 * dp), (int) (30 * dp));
        internalMargins = (int) (8 * dp);
    }


    /**
     * Запускайте перед уничтожением этого объекта - эта штука выключает видео.
     */
    public void destroy() {
        for (Runnable runnable : onDestroy)
            runnable.run();
    }

    /**
     * Просит всяческие куски разметки перерасчитать себя. Работает пока только для iframe-ов.
     */
    public void layout() {
        for (Runnable runnable : onLayout)
            runnable.run();
    }

    /**
     * Перемещает view-шки из предыдущего layout-а (если он ещё жив) в данный.
     */
    public void changeLayout(ViewGroup group) {
        layout = group;
        layout.removeAllViews();

        for (View view : cached_contents) {
            if (view.getParent() != null)
                ((ViewGroup) view.getParent()).removeView(view);
            layout.addView(view);
        }

        layout.invalidate();
        layout.requestLayout();
    }

    public void escape(final String text) {
        destroy();
        onDestroy.clear();
        cached_contents.clear();

        escape(text, layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            cached_contents.add(layout.getChildAt(i));
        }
    }

    private static int indexOf(CharSequence toProcess, int start, char ch) {
        if (start >= toProcess.length()) return -1;

        for (int i = start; i < toProcess.length(); i++)
            if (toProcess.charAt(i) == ch) return i;

        return -1;
    }

    /**
     * Searches for HTML tags and removes them.
     */
    private static SpannableStringBuilder removeAllTags(SpannableStringBuilder toProcess) {
        int s;

        while ((s = indexOf(toProcess, 0, '<')) != -1) {
            int f = indexOf(toProcess, s, '>');
            if (f == -1) break;
            toProcess.delete(s, f + 1);
        }

        return toProcess;
    }

    /**
     * Немного переписанный SU.deEntity
     */
    public static void deEntity(Editable data) {

        int index = 0;
        int end_index;

        while ((index = SU.indexOf('&', data, index)) != -1) {

            end_index = SU.indexOf(';', data, index);

            if (end_index == -1) break;

            String inner = String.valueOf(data.subSequence(index + 1, end_index));

            // Если это числовой тег (?), то попытаемся его воспроизвести.
            if (inner.startsWith("#"))
                try {

                    char uni = (char) Integer.parseInt(inner.substring(1), 16);

                    data.replace(index, end_index + 1, String.valueOf(uni));

                } catch (NumberFormatException | IndexOutOfBoundsException e) {

                    index++;

                }
            else if (SU.HTML_ESCAPE_SEQUENCES.containsKey(inner)) {

                data.replace(index, end_index + 1, String.valueOf(SU.HTML_ESCAPE_SEQUENCES.get(inner)));

            } else index++;

        }

    }

    /**
     * Deletes recurring chars.<br/>
     * <pre>("  a  b  c", ' ') = " a b c"</pre>
     */
    public static void removeRecurringChars(Editable modify, char remove) {

        for (int i = 0; i < modify.length() - 1; ) {
            if (modify.charAt(i) == remove) {
                while ((i + 1 < modify.length() - 1) && modify.charAt(i + 1) == remove) {
                    modify.delete(i, i + 1);
                }
            }
            i++;
        }

    }

    /* ... */
    private final static String header_end = "</header>\r\n\n\t\n\t\t\t";

    /**
     * Превращает HTML в понятный Android-у CharSequence и пихает его в данный ему TextView.
     */
    private void simpleEscape(final TextView target, final String text, final Context context) {

		/*
         * Исправляем проблему с header-ом.
		 * Даже не знаю, какой умный пегас умудрился панель действий отправить в текст.
		 */
        final int header_end_index = text.indexOf(header_end);
        final SpannableStringBuilder builder =
                new SpannableStringBuilder(
                        header_end_index == -1 ?
                                text
                                :
                                text.substring(header_end_index + header_end.length())
                );

        HTMLTree tree = new HTMLTree(builder.toString());

		/*
         * Отклонение от индексов. Мы меняем текст дерева, но так как теги привязаны, нам нужно учитывать это вручную.
		 * Надо написать insert для HTMLTree.
		 */
        int off = 0;
        /* Просто куча кейсов на кучу тегов. Не буду комментировать в подробностях. */
        for (final Tag tag : tree) {
            try {
                if (tag.isOpening())
                    switch (tag.name) {
                        case "strong":
                            builder.setSpan(
                                    new StyleSpan(Typeface.BOLD),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "em":
                            builder.setSpan(
                                    new StyleSpan(Typeface.ITALIC),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "s":
                            builder.setSpan(
                                    new StrikethroughSpan(),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "u":
                            builder.setSpan(
                                    new UnderlineSpan(),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "li":
                            builder.insert(off + tag.start, "\n\t•\t");
                            off += 4;
                            break;
                        // Пока так.
                        case "sup":
                        case "small":
                        case "sub":
                            BaselineJumpSpan.Gravity h = BaselineJumpSpan.Gravity.BOTTOM;
                            switch (tag.name) {
                                case "sup":
                                    h = BaselineJumpSpan.Gravity.TOP;
                                    break;
                                case "small":
                                    h = BaselineJumpSpan.Gravity.CENTER;
                                    break;
                                case "sub":
                                    h = BaselineJumpSpan.Gravity.BOTTOM;
                                    break;
                            }
                            builder.setSpan(
                                    new BaselineJumpSpan(h),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            builder.setSpan(
                                    new RelativeSizeSpan(0.5f),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "a":
                            String link = tag.get("href");

                            if (link.isEmpty())
                                continue;

                            if (link.startsWith("//"))
                                link = scheme + ":" + link;
                            if (link.startsWith("/"))
                                link = scheme + "://" + hostname + link;

                            builder.setSpan(
                                    new URLSpan(link),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "span":
                            if (!tag.get("align").isEmpty())
                                switch (tag.get("align")) {
                                    case "right":
                                        builder.setSpan(
                                                new AlignmentSpan() {
                                                    @Override
                                                    public Layout.Alignment getAlignment() {
                                                        return Layout.Alignment.ALIGN_OPPOSITE;
                                                    }
                                                },
                                                off + tag.end,
                                                off + tree.get(tree.getClosingTag(tag)).start,
                                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                        );
                                        break;
                                    case "center":
                                        builder.setSpan(
                                                new AlignmentSpan() {
                                                    @Override
                                                    public Layout.Alignment getAlignment() {
                                                        return Layout.Alignment.ALIGN_CENTER;
                                                    }
                                                },
                                                off + tag.end,
                                                off + tree.get(tree.getClosingTag(tag)).start,
                                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                        );
                                        break;
                                    case "left":
                                        builder.setSpan(
                                                new AlignmentSpan() {
                                                    @Override
                                                    public Layout.Alignment getAlignment() {
                                                        return Layout.Alignment.ALIGN_NORMAL;
                                                    }
                                                },
                                                off + tag.end,
                                                off + tree.get(tree.getClosingTag(tag)).start,
                                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                        );
                                        break;
                                }
                            switch (tag.get("class")) {
                                case "red":
                                    builder.setSpan(
                                            new ForegroundColorSpan(red),
                                            off + tag.end,
                                            off + tree.get(tree.getClosingTag(tag)).start,
                                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    );
                                    break;
                                case "green":
                                    builder.setSpan(
                                            new ForegroundColorSpan(green),
                                            off + tag.end,
                                            off + tree.get(tree.getClosingTag(tag)).start,
                                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    );
                                    break;
                                case "blue":
                                    builder.setSpan(
                                            new ForegroundColorSpan(blue),
                                            off + tag.end,
                                            off + tree.get(tree.getClosingTag(tag)).start,
                                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    );
                                    break;
                                case "spoiler-gray":
                                    builder.setSpan(
                                            new LitespoilerSpan(),
                                            off + tag.end,
                                            off + tree.get(tree.getClosingTag(tag)).start,
                                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    );
                                    break;
                            }
                            break;
                        case "h4":
                            builder.setSpan(
                                    new RelativeSizeSpan(1.5f),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            builder.insert(off++ + tag.start, "\n");
                            builder.insert(off++ + tree.get(tree.getClosingTag(tag)).start, "\n");
                            break;
                        case "h5":
                            builder.setSpan(
                                    new RelativeSizeSpan(1.25f),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            builder.insert(off++ + tag.start, "\n");
                            builder.insert(off++ + tree.get(tree.getClosingTag(tag)).start, "\n");
                            break;
                        case "h6":
                            builder.setSpan(
                                    new RelativeSizeSpan(1.2f),
                                    off + tag.end,
                                    off + tree.get(tree.getClosingTag(tag)).start,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            builder.insert(off++ + tag.start, "\n");
                            builder.insert(off++ + tree.get(tree.getClosingTag(tag)).start, "\n");
                            break;
                    }
                else if (tag.isStandalone())
                    switch (tag.name) {
                        case "hr":
                            final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                            bitmap.setPixel(0, 0, Color.BLACK);
                            builder.setSpan(
                                    new ImageSpan(context, bitmap),
                                    off + tag.start,
                                    off + tag.end,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            break;
                        case "br":
//							builder.insert(off++ + tag.end, "\n");       // Используем расставление из html, ибо pre.
                            break;
                        case "img":
                            if (tag.get("src").isEmpty()) continue;
                            final String repl = "I";
                            builder.insert(off + tag.start, repl);

                            final String src = tag.get("src");

                            Drawable dr = context.getResources().getDrawable(replacerImage);
                            if (dr != null)
                                dr.setBounds(imageReplacerSize);

                            final ImageSpan replacer = new ImageSpan(dr);

                            builder.setSpan(
                                    replacer,
                                    off + tag.start,
                                    off + tag.start + repl.length(),
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );
                            builder.setSpan(
                                    new DoubleClickableSpan() {
                                        @Override
                                        public void onDoubleClick(View widget) {
                                            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(src)));
                                        }
                                    },
                                    off + tag.start,
                                    off + tag.start + repl.length(),
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            );

                            int width, height;
                            if (tag.get("width").isEmpty())
                                width = (int) (layout.getWidth() * 0.9f);
                            else
                                width = Integer.parseInt(tag.get("width"));

                            if (tag.get("height").isEmpty())
                                height = -1;//(int) (layout.getWidth() * 0.6f);
                            else
                                height = Integer.parseInt(tag.get("height"));

                            final SpanImageListener imageAware = new SpanImageListener(target, replacer, builder);
                            ImageSize size = new ImageSize(width, height);
                            DisplayImageOptions opt = new DisplayImageOptions.Builder()
                                    .cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .imageScaleType(ImageScaleType.EXACTLY).build();

                            if (loadImages && ImageLoader.getInstance().getDiskCache().get(src) == null)
                                ImageLoader.getInstance().loadImage(src, size, opt, imageAware);
                            else
                                ImageLoader.getInstance().displayImage(
                                        src,
                                        new NonViewAware(src, size, ViewScaleType.FIT_INSIDE),
                                        opt,
                                        imageAware
                                );

                            off += repl.length();
                            break;
                    }
            } catch (HTMLTree.TagNotFoundException e) {
                Log.e("Tag escaper", "tag not found : " + tag, e);
            }
        }

		/* Не менять местами removeAllTags и deEntity: рискуешь остаться без всего экранированного */
        removeAllTags(builder);
        removeRecurringChars(builder, '\n');
        deEntity(builder);
        target.setText(builder);
    }

    private TextView form(String text, Context context) {
        TextView view = new SpanIgnoringTextView(context);
        view.setTextAppearance(
                context,
                context
                        .getTheme()
                        .obtainStyledAttributes(
                                new int[]{
                                        android.R.attr.textAppearance
                                }
                        )
                        .getResourceId(0, android.R.style.TextAppearance)
        );
        simpleEscape(view, text, context);

        if (Build.VERSION.SDK_INT > 10) {
            view.setTextIsSelectable(textIsSelectable);
        }

        view.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    /**
     * Вставляет в группу новый набор контента. Удаляет предыдущий.
     */
    @SuppressWarnings("deprecation")
    private void escape(String text, final ViewGroup group) {
        final Context context = group.getContext();
        group.removeViews(0, group.getChildCount());
        HTMLTree tree = new HTMLTree(text);

        int start_index = 0;

        for (int i = 0; i < tree.size(); i++) {

            Tag tag = tree.get(i);

            // Спойлеры
            if ("span".equals(tag.name) && "spoiler".equals(tag.get("class"))) {
                // Заливаем набранный чистый текст.
                TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
                group.addView(pre_text);

                View spoiler = formSpoiler(tree.getContents(tag), context, group);

                // Если тела спойлера нет, то возвращается null.
                if (spoiler != null)
                    group.addView(spoiler);

                // Закрываем и двигаем индекс.
                Tag closing = tree.get(tree.getClosingTag(tag));
                i = closing.index - tree.offset();
                start_index = closing.end;
            }

            // Видео и пр.
            if ("iframe".equals(tag.name)) {
                // Заливаем набранный чистый текст.
                TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
                group.addView(pre_text);

                final String src = tag.get("src");
                if (loadVideos) {

                    final WebView iframe = new WebView(context);

                    //noinspection Annotator
                    iframe.getSettings().setJavaScriptEnabled(true);
                    iframe.setBackgroundColor(Color.TRANSPARENT);
                    iframe.getSettings().setPluginState(WebSettings.PluginState.ON);
                    iframe.loadUrl(src);
                    iframe.setWebChromeClient(new WebChromeClient());
                    iframe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setSelected(true);
                            System.out.println("Click");
                        }
                    });

                    group.addView(iframe);

                    onDestroy.add(new Runnable() {
                        @Override
                        public void run() {
                            if (iframe.getParent() != null)
                                ((ViewGroup) iframe.getParent()).removeView(iframe);
                            iframe.destroy();
                        }
                    });

                    onLayout.add(new Runnable() {
                        @Override
                        public void run() {

                            int nHeight = (int) (group.getWidth() * (2f / 3));
                            int mHeight = (int) (ctx.getResources().getDisplayMetrics().heightPixels * 0.8f);
                            nHeight = Math.min(mHeight, nHeight);

                            if (nHeight != iframe.getLayoutParams().height)
                                iframe.requestLayout();

                            iframe.getLayoutParams().height = nHeight;

                        }
                    });

                    iframe.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    iframe.getLayoutParams().height = (int) (context.getResources().getDisplayMetrics().widthPixels * (2f / 3));

                    iframe.requestLayout();

                } else {
                    /* Возможен injection, надо проверить. */
                    group.addView(form("<a href=\"" + src + "\">&lt;iframe " + src + " &gt;</a>", context));
                }

                // Закрываем и двигаем индекс.
                Tag closing = tree.get(tree.getClosingTag(tag));
                i = closing.index - tree.offset();
                start_index = closing.end;
            }

            // Кат
            if ("a".equals(tag.name) && tag.get("href") != null && tag.get("href").endsWith("#cut") && !tag.isStandalone()) {
                TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);

                group.addView(pre_text);

                // Немного костыльно, но сойдёт.
                TextView cut = form(tag + tree.getContents(tag).trim() + "</a>", context);
                if (cutBackground != 0)
                    cut.setBackgroundResource(cutBackground);
                cut.setPadding(internalMargins, internalMargins, internalMargins, internalMargins);
                group.addView(cut);
                cut.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                cut.requestLayout();

                // Закрываем и двигаем индекс.
                Tag closing = tree.get(tree.getClosingTag(tag));
                i = closing.index - tree.offset();
                start_index = closing.end;
            }

            // Цитата
            if ("blockquote".equals(tag.name)) {
                TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
                group.addView(pre_text);

                TextView quote = form(tree.getContents(tag).trim(), context);
                quote.setBackgroundResource(quoteBackground);
                quote.setPadding(internalMargins, internalMargins, internalMargins, internalMargins);

                group.addView(quote);

                // Закрываем и двигаем индекс.
                Tag closing = tree.get(tree.getClosingTag(tag));
                i = closing.index - tree.offset();
                start_index = closing.end;
            }

            // Код
            if ("pre".equals(tag.name) && !tag.isStandalone()) {
                TextView pre_text = form(tree.html.subSequence(start_index, tag.start).toString(), context);
                group.addView(pre_text);

                TextView code = form(tree.getContents(tag).trim(), context);
                code.setBackgroundResource(codeBackground);
                code.setPadding(internalMargins, internalMargins, internalMargins, internalMargins);
                code.setTypeface(Typeface.MONOSPACE);
                code.setTextSize(code.getTextSize() * 0.8f);
                code.setTextColor(codeColor);

                group.addView(code);

                // Закрываем и двигаем индекс.
                Tag closing = tree.get(tree.getClosingTag(tag));
                i = closing.index - tree.offset();
                start_index = closing.end;
            }

        }

        if (start_index < tree.html.length()) {
            group.addView(
                    form(
                            tree.html.subSequence(
                                    start_index,
                                    tree.html.length()
                            ).toString(),
                            context
                    )
            );
        }

    }

    /**
     * Создаёт спойлер. Контент спойлера расчитывается только при раскрытии.
     */
    private View formSpoiler(String text, Context context, ViewGroup group) {
        final HTMLTree tree = new HTMLTree(text);

        final View view = LayoutInflater.from(context).inflate(spoilerBodyLayout, group, false);

        Tag header = null;
        Tag body_search = null;

        for (Tag tag : tree) {
            if ("spoiler-title".equals(tag.get("class")) && header == null) header = tag;
            if ("spoiler-body".equals(tag.get("class")) && body_search == null) body_search = tag;
        }

        final Tag body = body_search;

        if (body == null)
            return null;

        ViewGroup header_layout = (ViewGroup) view.findViewById(spoilerHeaderId);

        if (header != null) {
            TextView head = form(tree.getContents(header), context);
            head.setTextColor(spoilerLabelColor);
            header_layout.addView(head);
        } else
            escape("Спойлер", header_layout);

        // Отключаем перемещение по ссылкам в заголовке спойлера
        for (int i = 0; i < header_layout.getChildCount(); i++) {
            View ind = header_layout.getChildAt(i);
            if (ind instanceof TextView) {
                TextView txt = (TextView) ind;
                txt.setMovementMethod(null);
            }
        }

        view.findViewById(spoilerHeaderId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View non_used) {

                if (view.findViewById(spoilerBodyId).getVisibility() == View.GONE)
                    view.findViewById(spoilerBodyId).setVisibility(View.VISIBLE);
                else
                    view.findViewById(spoilerBodyId).setVisibility(View.GONE);

                view.getParent().requestLayout();

                if (((ViewGroup) view.findViewById(spoilerBodyId)).getChildCount() == 0)
                    escape(tree.getContents(body), (ViewGroup) view.findViewById(spoilerBodyId));
            }
        });


        return view;
    }


    private static class SpanIgnoringTextView extends TextView {

        public SpanIgnoringTextView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            final boolean b = super.onTouchEvent(event);
            if (Build.VERSION.SDK_INT >= 11 && isTextSelectable())
                return b;
            return getSelectionStart() != -1 && b;
        }
    }
}
