package com.smpp.client;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionLimits {
    public int i;
    public AtomicInteger counter;

    public ConnectionLimits(int i, int maxSms) {
        this.i = i;
        this.counter = new AtomicInteger();
    }
}
