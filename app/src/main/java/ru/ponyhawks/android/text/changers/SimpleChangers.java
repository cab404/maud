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
            new WrapTextChanger(R.drawable.ic_format_align_left, "<span align=\"left\">", "</span>");
    public static final TextChanger SPAN_RIGHT =
            new WrapTextChanger(R.drawable.ic_format_align_right, "<span align=\"right\">", "</span>");
    public static final TextChanger SPAN_CENTER =
            new WrapTextChanger(R.drawable.ic_format_align_center, "<span align=\"center\">", "</span>");
    public static final TextChanger ITALIC =
            new WrapTextChanger(R.drawable.ic_format_italic, "<i>", "</i>");
    public static final TextChanger BOLD =
            new WrapTextChanger(R.drawable.ic_format_bold, "<strong>", "</strong>");
    public static final TextChanger UNDERLINE =
            new WrapTextChanger(R.drawable.ic_format_underline, "<u>", "</u>");
    public static final TextChanger STRIKETHROUGH =
            new WrapTextChanger(R.drawable.ic_format_strikethrough, "<s>", "</s>");
    public static final TextChanger QUOTE =
            new WrapTextChanger(R.drawable.ic_format_quote, "<quote>", "</quote>");

}
