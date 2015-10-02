package ru.ponyhawks.android.text.changers;

import android.text.Editable;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:22 on 27/09/15
 *
 * @author cab404
 */
public interface TextPrism {
    Editable affect(Editable input);

    Editable purify(Editable input);
}
