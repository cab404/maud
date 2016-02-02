package ru.ponyhawks.android.text.changers;

import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 20:09 on 26/09/15
 *
 * @author cab404
 */
public final class SimpleChangers {
    private SimpleChangers() {
    }
    public static final TextChanger SPAN_LEFT =
            new WrapTextChanger(R.drawable.ic_format_align_left, "<left>", "</left>");
    public static final TextChanger SPAN_RIGHT =
            new WrapTextChanger(R.drawable.ic_format_align_right, "<right>", "</right>");
    public static final TextChanger SPAN_CENTER =
            new WrapTextChanger(R.drawable.ic_format_align_center, "<center>", "</center>");
    public static final TextChanger ITALIC =
            new WrapTextChanger(R.drawable.ic_format_italic, "<em>", "</em>");
    public static final TextChanger BOLD =
            new WrapTextChanger(R.drawable.ic_format_bold, "<b>", "</b>");
    public static final TextChanger UNDERLINE =
            new WrapTextChanger(R.drawable.ic_format_underline, "<u>", "</u>");
    public static final TextChanger STRIKETHROUGH =
            new WrapTextChanger(R.drawable.ic_format_strikethrough, "<s>", "</s>");
    public static final TextChanger QUOTE =
            new WrapTextChanger(R.drawable.ic_format_quote, "<quote>", "</quote>");
    public static final TextChanger LITESPOILER =
            new WrapTextChanger(R.drawable.ic_litespoiler, "<ls>", "</ls>");
    public static final TextChanger SPOILER =
            new ParamWrapTextChanger("<spoiler>%<end-title/>$</spoiler>", R.drawable.ic_spoiler, R.string.spoiler_title);
    public static final TextChanger LINK =
            new ParamWrapTextChanger("<a href='%'>$</a>", R.drawable.ic_link, R.string.link);
    public static final TextChanger VIDEO =
            new ParamWrapTextChanger("<video>%</video>", R.drawable.ic_video, R.string.link);
}
