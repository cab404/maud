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
 * Also a very handy tool for synchronizing Chumroll add calls to main thread
 * <p/>
 * Created at 01:51 on 14/09/15
 *
 * @author cab404
 */
public class UniteSynchronization implements ModularBlockParser.ParsedObjectHandler {

    private final Map<Integer, Binding> bindings;
    private final ChumrollAdapter target;

    public UniteSynchronization(ChumrollAdapter target) {
        this.bindings = new HashMap<>();
        this.target = target;
    }

    public <A> UniteSynchronization bind(Integer id, ViewConverter<A> converter) {
        return bind(id, converter, null);
    }

    public <A> UniteSynchronization bind(Integer id, ViewConverter<A> converter, InsertionRule<A> rule) {
        bindings.put(id, new Binding<>(converter, rule));
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

    public <V> void inject(V object, ViewConverter<V> use) {
        inject(object, use, null);
    }


    public <V> void inject(V object, ViewConverter<V> use, InsertionRule<V> rule) {
        try {
            runnable.lock.lock();
            //noinspection unchecked
            runnable.dataToAdd.add(new VV(new Binding(use, rule), object));
            if (runnable.finished) {
                runnable.finished = false;
                handler.postDelayed(runnable, 100);
            }
        } finally {
            runnable.lock.unlock();
        }
    }

    private class Binding<Clazz> {
        public Binding(ViewConverter<Clazz> converter, InsertionRule<Clazz> rule) {
            this.converter = converter;
            this.rule = rule;
        }

        ViewConverter<Clazz> converter;
        InsertionRule<Clazz> rule;

        @SuppressWarnings("unchecked")
        void insert(Object object) {
            if (rule == null)
                target.add(converter, ((Clazz) object));
            else
                target.add(
                        rule.indexFor((Clazz) object, converter, target),
                        converter,
                        (Clazz) object
                );
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

    public interface InsertionRule<V> {
        int indexFor(V object, ViewConverter<V> converter, ChumrollAdapter adapter);
    }

}
