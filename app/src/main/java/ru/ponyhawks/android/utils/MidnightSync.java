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
public class MidnightSync extends UniteSyncronization implements ModularBlockParser.ParsedObjectHandler {

    private final Map<Integer, Binding> bindings;
    private final ChumrollAdapter target;

    public MidnightSync(ChumrollAdapter target) {
        this.bindings = new HashMap<>();
        this.target = target;
    }

    public <A> MidnightSync bind(Integer id, ViewConverter<A> converter) {
        return bind(id, converter, null);
    }

    public <A> MidnightSync bind(Integer id, ViewConverter<A> converter, InsertionRule<A> rule) {
        bindings.put(id, new Binding<>(converter, rule));
        return this;
    }

    public <A> MidnightSync bind(Integer id, Class<? extends ViewConverter<A>> converter) {
        return bind(id, converter, null);
    }

    @SuppressWarnings("unchecked")
    public <A> MidnightSync bind(Integer id, Class<? extends ViewConverter<A>> converter, InsertionRule<A> rule) {
        bindings.put(id, new Binding<>(target.getConverters().getInstance(converter), rule));
        return this;
    }

    @Override
    public void handle(Object object, int key) {
        if (bindings.containsKey(key))
            post(new InsertObject(bindings.get(key), object));
    }

    public <V> void inject(V object, ViewConverter<V> use) {
        inject(object, use, null);
    }


    public <V> void inject(V object, ViewConverter<V> use, InsertionRule<V> rule) {
        post(new InsertObject(new Binding<>(use, rule), object));
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

    private class InsertObject implements Runnable {
        Binding binding;
        Object object;

        public InsertObject(Binding binding, Object object) {
            if (binding == null) throw new RuntimeException("Binding cannot be empty");
            this.binding = binding;
            this.object = object;
        }

        @Override
        public void run() {
            binding.insert(object);
        }
    }


    public interface InsertionRule<V> {
        int indexFor(V object, ViewConverter<V> converter, ChumrollAdapter adapter);
    }

}
