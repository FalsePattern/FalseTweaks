package com.falsepattern.falsetweaks.modules.threadexec;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Semaphore;

@RequiredArgsConstructor
class TurboTransferQueue<T> {
    private static final int SIZE = 256;

    private final Semaphore S;
    private final Object[] STORE = new Object[SIZE];

    private volatile int insert = 0;
    private volatile int remove = 0;

    public void insert(T value) {
        int nextIndex = (insert + 1) % SIZE;
        while (nextIndex == remove) {
            S.release();
            Thread.yield();
            while (!S.tryAcquire())
                Thread.yield();
        }
        STORE[insert] = value;
        insert = nextIndex;
    }

    public T remove() {
        if (remove == insert) {
            return null;
        }
        @SuppressWarnings("unchecked") T result = (T) STORE[remove];
        //noinspection NonAtomicOperationOnVolatileField
        remove = (remove + 1) % SIZE;
        return result;
    }

    public boolean isEmpty() {
        return remove == insert;
    }
}
