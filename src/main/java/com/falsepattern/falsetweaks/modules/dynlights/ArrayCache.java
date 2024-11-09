package com.falsepattern.falsetweaks.modules.dynlights;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayCache<T> {
    private final Class<?> elementClass;
    private final int maxCacheSize;
    private final AbstractObjectList<T> cache;
    private final ReentrantLock MUTEX = new ReentrantLock();

    public ArrayCache(Class<?> elementClass, int maxCacheSize) {
        this.elementClass = elementClass;
        this.maxCacheSize = maxCacheSize;
        cache = new ObjectArrayList<>(maxCacheSize);
    }

    public T allocate(int size) {
        T arr;
        while (!MUTEX.tryLock()) {
            Thread.yield();
        }
        try {
            if (this.cache.isEmpty()) {
                arr = null;
            } else {
            arr = this.cache.pop();
            }
        } finally {
            MUTEX.unlock();
        }
        if (arr == null || Array.getLength(arr) < size) {
            //noinspection unchecked
            arr = (T) Array.newInstance(this.elementClass, size);
        }

        return arr;
    }

    public void free(T arr) {
        if (arr != null) {
            Class<?> cls = arr.getClass();
            if (cls.getComponentType() != this.elementClass) {
                throw new IllegalArgumentException("Wrong component type");
            }
            while (!MUTEX.tryLock()) {
                Thread.yield();
            }
            try {
                if (this.cache.size() < this.maxCacheSize) {
                    this.cache.push(arr);
                }
            } finally {
                MUTEX.unlock();
            }
        }
    }
}
