package ru.ponyhawks.android.text.changers;

import android.text.Editable;
import android.text.SpannableStringBuilder;

import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 06:21 on 30/09/15
 *
 * @author cab404
 */
public class ShrunkFormattingPrism implements TextPrism {

    public static final List<String> ALIGNS = Arrays.asList("center", "left", "right");

    @Override
    public Editable affect(Editable input) {
        final HTMLTree tree = new HTMLTree(input.toString());

        /* starting with spoilers */

        List<Tag> backReplace = new LinkedList<>();

        for (Tag tag : tree) {
            if ("span".equals(tag.name)) {
                if ("spoiler".equals(tag.get("class"))) {
                    Tag closing = tree.get(tree.getClosingTag(tag));

                    final HTMLTree spoiledTree = tree.getTree(tag);

                    final Tag title = spoiledTree.xPathFirstTag("span&class=spoiler-title");
                    final Tag content = spoiledTree.xPathFirstTag("span&class=spoiler-body");

                    if (title == null || !title.isOpening()) continue;
                    if (content == null || !content.isOpening()) continue;

                    final Tag titleClose = spoiledTree.get(spoiledTree.getClosingTag(title));
                    final Tag contentClose = spoiledTree.get(spoiledTree.getClosingTag(content));

                    backReplace.add(title);
                    backReplace.add(titleClose);
                    backReplace.add(contentClose);

                    content.name = "end-title";
                    content.type = Tag.Type.STANDALONE;
                    content.props.clear();
                    tag.props.clear();
                    tag.name = closing.name = "spoiler";
                }

                if ("spoiler-gray".equals(tag.get("class")))
                    tag.name = "ls";

                final String align = tag.get("align");
                if (ALIGNS.contains(align)) {
                    Tag closing = tree.get(tree.getClosingTag(tag));
                    tag.name = align;
                    tag.props.clear();
                    closing.name = align;
                }

            }
            if ("strong".equals(tag.name))
                tag.name = "b";
            if ("blockquote".equals(tag.name))
                tag.name = "quote";

        }

        for (Tag tag : backReplace) {
            tag.name = "REDACTED";
            tag.props.clear();
            tag.type = Tag.Type.STANDALONE;
        }

        return new SpannableStringBuilder(tree.toString(false).replace("<REDACTED/>", ""));
    }

    @Override
    public Editable purify(Editable input) {
        String formatted = new HTMLTree(input.toString()).toString(false);

        formatted = formatted
                .replace("<spoiler>", "<span class=\"spoiler\"><span class=\"spoiler-title\" onclick=\"return true;\">")
                .replace("</spoiler>", "</span></span>")

                .replace("<end-title/>", "</span><span class=\"spoiler-body\">")
                .replace("<quote>", "<blockquote>")
                .replace("</quote>", "</blockquote>")
                .replace("<b>", "<strong>")
                .replace("</b>", "</strong>")

                .replace("<left>", "<span align=\"left\">")
                .replace("<right>", "<span align=\"right\">")
                .replace("<center>", "<span align=\"center\">")

                .replace("<ls>", "<span class=\"spoiler-gray\">")
                .replace("</ls>", "</span>")
                .replace("</left>", "</span>")
                .replace("</right>", "</span>")
                .replace("</center>", "</span>")
        ;

        return new SpannableStringBuilder(formatted);
    }
}
