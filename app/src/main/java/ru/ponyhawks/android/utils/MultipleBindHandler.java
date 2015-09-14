package ru.ponyhawks.android.utils;

import com.cab404.moonlight.framework.ModularBlockParser;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:36 on 14/09/15
 *
 * @author cab404
 */
public class MultipleBindHandler implements ModularBlockParser.ParsedObjectHandler{

    private final ModularBlockParser.ParsedObjectHandler[] handlers;

    public MultipleBindHandler(ModularBlockParser.ParsedObjectHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(Object object, int key) {
        for (ModularBlockParser.ParsedObjectHandler h : handlers)
            h.handle(object, key);
    }
}
