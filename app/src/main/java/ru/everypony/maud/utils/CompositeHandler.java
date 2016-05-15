package ru.everypony.maud.utils;

import com.cab404.moonlight.framework.ModularBlockParser;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:36 on 14/09/15
 *
 * @author cab404
 */
public class CompositeHandler implements ModularBlockParser.ParsedObjectHandler {

    private final ModularBlockParser.ParsedObjectHandler[] handlers;

    public CompositeHandler(ModularBlockParser.ParsedObjectHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(Object object, int key) {
        for (ModularBlockParser.ParsedObjectHandler h : handlers)
            if (h != null)
                h.handle(object, key);
    }
}
