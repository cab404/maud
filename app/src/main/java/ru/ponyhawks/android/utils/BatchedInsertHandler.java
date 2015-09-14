package ru.ponyhawks.android.utils;

import android.os.Handler;
import android.os.Looper;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;
import com.cab404.moonlight.framework.ModularBlockParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Binds Moonlight and Chumroll in a fast way
 * <p/>
 * Created at 01:51 on 14/09/15
 *
 * @author cab404
 */
public class BatchedInsertHandler implements ModularBlockParser.ParsedObjectHandler {

    private final Map<Integer, Binding> bindings;
    private final ChumrollAdapter target;

    public BatchedInsertHandler(ChumrollAdapter target) {
        this.bindings = new HashMap<>();
        this.target = target;
    }

    public <A> BatchedInsertHandler bind(Integer id, ViewConverter<A> converter){
        bindings.put(id, new Binding<>(converter));
        return this;
    }

    Handler handler = new Handler(Looper.getMainLooper());
    BatchRunnable runnable = new BatchRunnable();

    @Override
    public void handle(Object object, int key) {
        if (bindings.containsKey(key))
            try {
                runnable.lock.lock();
                //noinspection unchecked
                runnable.dataToAdd.add(new VV(bindings.get(key), object));
                if (runnable.finished) {
                    runnable.finished = false;
                    handler.postDelayed(runnable, 100);
                }
            } finally {
                runnable.lock.unlock();
            }
    }

    private class Binding<Clazz> {
        public Binding(ViewConverter<Clazz> converter) {
            this.converter = converter;
        }

        ViewConverter<Clazz> converter;

        @SuppressWarnings("unchecked")
        void insert(Object object){
            target.add(converter, ((Clazz) object));
        }
    }

    private class VV {
        Binding binding;
        Object object;

        public VV(Binding binding, Object object) {
            this.binding = binding;
            this.object = object;
        }
    }

    private class BatchRunnable implements Runnable {

        List<VV> dataToAdd = new ArrayList<>();
        boolean finished = true;
        final ReentrantLock lock = new ReentrantLock();

        @Override
        public void run() {
            try {
                lock.lock();
                for (VV a : dataToAdd)
                    a.binding.insert(a.object);
                target.notifyDataSetChanged();
                dataToAdd.clear();
                finished = true;
            } finally {
                lock.unlock();
            }
        }
    }
}
