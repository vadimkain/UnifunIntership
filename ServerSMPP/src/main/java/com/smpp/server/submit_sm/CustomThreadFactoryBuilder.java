package com.smpp.server.submit_sm;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactoryBuilder {

    private String namePrefix = null;
    private boolean daemon = false;
    private int priority = Thread.NORM_PRIORITY;

    public CustomThreadFactoryBuilder setNamePrefix(String namePrefix) {
        if (namePrefix == null) {
            throw new NullPointerException("Name prefix cannot be null");
        }
        this.namePrefix = namePrefix;
        return this;
    }

    public CustomThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public CustomThreadFactoryBuilder setPriority(int priority) {
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(
                    String.format("Thread priority (%s) must be between %s and %s",
                            priority, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY)
            );
        }
        this.priority = priority;
        return this;
    }

    public ThreadFactory build() {
        return runnable -> {
            Thread thread = new Thread(runnable);
            if (namePrefix != null) {
                thread.setName(namePrefix + "-" + getAndIncrementCount());
            }
            thread.setDaemon(daemon);
            thread.setPriority(priority);
            return thread;
        };
    }

    private final AtomicInteger count = new AtomicInteger(0);

    private String getAndIncrementCount() {
        return String.valueOf(count.getAndIncrement());
    }
}
