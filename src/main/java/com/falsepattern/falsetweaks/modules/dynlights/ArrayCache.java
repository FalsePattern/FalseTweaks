/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.dynlights;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.lang.reflect.Array;
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
