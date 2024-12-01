/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

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
