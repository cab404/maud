package ru.ponyhawks.android.utils;

import android.app.Activity;

import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.framework.Request;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.ponyhawks.android.activity.BaseActivity;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 12:25 on 22/09/15
 *
 * @author cab404
 */
public class RequestManager {

    private final Set<Request> requests = new HashSet<>();
    private final Lock lock = new ReentrantLock();
    private final AccessProfile profile;

    public static RequestManager fromActivity(Activity activity) {
        if (activity instanceof BaseActivity)
            return ((BaseActivity) activity).getRequestManager();
        return null;
    }

    public RequestManager(AccessProfile profile) {
        this.profile = profile;
    }

    public <Cls extends Request> RequestBuilder<Cls> manage(Cls request) {
        return new RequestBuilder<>(request);
    }


    public void cancelAll() {
        try {
            lock.lock();
            for (Request req : requests)
                req.cancel();
        } finally {
            lock.unlock();
        }
    }

    public interface RequestCallback<Cls extends Request> {
        void managedBy(RequestManager man);

        void onStart(Cls what);

        void onCancel(Cls what);

        void onSuccess(Cls what);

        void onError(Cls what, Exception e);

        void onFinish(Cls what);
    }

    public static class SimpleRequestCallback<Cls extends Request> implements RequestCallback<Cls> {
        private RequestManager man;

        @Override
        public void managedBy(RequestManager man) {
            this.man = man;
        }

        @Override
        public void onStart(Cls what) {
        }

        @Override
        public void onCancel(Cls what) {
        }

        @Override
        public void onSuccess(Cls what) {
        }

        @Override
        public void onError(Cls what, Exception e) {
        }

        @Override
        public void onFinish(Cls what) {
        }

        public void retry(Cls req) {
            man.manage(req).setCallback(this).start();
        }
    }


    public class RequestBuilder<Cls extends Request> extends Thread {
        private final Cls request;
        private AccessProfile profile = RequestManager.this.profile;

        private RequestCallback<Cls> callback;

        public RequestBuilder(Cls request) {
            this.request = request;
        }

        public RequestBuilder<Cls> setProfile(AccessProfile customProfile) {
            this.profile = customProfile;
            return this;
        }

        public RequestBuilder<Cls> setCallback(RequestCallback<Cls> callback) {
            this.callback = callback;
            return this;
        }

        public void start() {
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            lock.lock();
                            requests.add(request);
                        } finally {
                            lock.unlock();
                        }
                        if (callback != null) {
                            callback.onStart(request);
                        }
                        request.fetch(profile);
                        if (callback != null) {
                            callback.onSuccess(request);
                        }
                    } catch (Exception e) {
                        if (request.isCancelled()) {
                            if (callback != null) {
                                callback.onCancel(request);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError(request, e);
                            }
                        }
                    } finally {
                        if (callback != null) {
                            callback.onFinish(request);
                        }
                        try {
                            lock.lock();
                            requests.remove(request);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }.start();
        }
    }
}
