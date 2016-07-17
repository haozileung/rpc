package com.haozileung.rpc.common.protocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ResponseFuture<T> implements Future<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String id;
    private final ConcurrentMap<String, ResponseFuture<T>> futurePool;
    private final CountDownLatch responseLatch = new CountDownLatch(1);
    private final long time;
    private final TimeUnit unit;
    private volatile T response;

    private volatile boolean cancelled = false;

    public ResponseFuture(String id, ConcurrentMap<String, ResponseFuture<T>> futurePool) {
        this.id = id;
        this.futurePool = futurePool;
        this.time = 10;
        this.unit = TimeUnit.SECONDS;
    }
    public ResponseFuture(String id, ConcurrentMap<String, ResponseFuture<T>> futurePool, long time, TimeUnit unit) {
        this.id = id;
        this.futurePool = futurePool;
        this.time = time;
        this.unit = unit;
    }

    @Override
    public boolean cancel(boolean b) {
        if (this.isDone()) {
            return false;
        } else {
            this.responseLatch.countDown();
            this.cancelled = true;
            this.futurePool.remove(this.id);
            return this.isDone();
        }
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isDone() {
        return this.responseLatch.getCount() == 0;
    }


    public T get() {
        try {
            this.responseLatch.await();
        } catch (InterruptedException e) {
            cancel(true);
            logger.error("read rpc response timeout");
            return null;
        }
        return this.response;
    }

    public T get(long timeout, TimeUnit unit) {
        try {
            this.responseLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            cancel(true);
            logger.error("read rpc response timeout");
            return null;
        }
        return this.response;
    }


    public void commit(T response) {
        this.response = response;
        this.responseLatch.countDown();
        this.futurePool.remove(this.id);
    }
}