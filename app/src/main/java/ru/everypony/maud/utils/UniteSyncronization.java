package ru.everypony.maud.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages task batching
 * <p/>
 * Created at 23:07 on 21/09/15
 *
 * @author cab404
 */
public class UniteSyncronization {
    private final BatchRunnable runnable = new BatchRunnable();

    public void post(Runnable psto) {
        try {
            runnable.lock.lock();
            runnable.dataToAdd.add(psto);
            if (runnable.finished) runnable.restart();
        } finally {
            runnable.lock.unlock();
        }
    }

    public void setLimit(int taskLimit) {
        try {
            runnable.lock.lock();
            runnable.limit = taskLimit;
        } finally {
            runnable.lock.unlock();
        }
    }

    protected void onCycleFinish() {

    }

    private class BatchRunnable implements Runnable {

        protected Handler handler = new Handler(Looper.getMainLooper());
        protected final ReentrantLock lock = new ReentrantLock();
        protected List<Runnable> dataToAdd = new ArrayList<>();
        private boolean finished = true;
        int limit = 100;
        int delay = 50;


        void restart() {
            finished = false;
            handler.postDelayed(this, delay);
        }

        @Override
        public void run() {
            List<Runnable> processTasks;
            try {
                lock.lock();
                int end = limit;
                if (limit == -1 || dataToAdd.size() < limit) end = dataToAdd.size();

                processTasks = new ArrayList<>(dataToAdd.subList(0, end));
                if (end == dataToAdd.size())
                    dataToAdd.clear();
                else
                    dataToAdd = dataToAdd.subList(end, dataToAdd.size());

                if (dataToAdd.isEmpty())
                    finished = true;
                else
                    restart();

            } finally {
                lock.unlock();
            }

            for (Runnable r : processTasks)
                r.run();

            onCycleFinish();
        }
    }


}
