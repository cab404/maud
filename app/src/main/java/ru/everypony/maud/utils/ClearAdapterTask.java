package ru.everypony.maud.utils;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.moonlight.framework.ModularBlockParser;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:03 on 22/09/15
 *
 * @author cab404
 */
public class ClearAdapterTask implements ModularBlockParser.ParsedObjectHandler, Runnable {
    private final UniteSyncronization sync;
    private final ChumrollAdapter adapter;
    boolean cleared = false;

    public ClearAdapterTask(ChumrollAdapter adapter, UniteSyncronization sync) {
        this.adapter = adapter;
        this.sync = sync;
    }

    @Override
    public void handle(Object object, int key) {
        if (!cleared)
            sync.post(this);
        cleared = true;
    }

    @Override
    public void run() {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
}
