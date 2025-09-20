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
package com.falsepattern.falsetweaks.modules.threading;

import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class FastThreadLocal<S> {
    private static final List<WeakReference<FastThreadLocal<?>>> liveThreadLocals = new ArrayList<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private static final int MAX_LIVE_TURBO_THREADS = 128;
    private static Thread MAIN_THREAD = null;

    protected final ThreadLocal<S> threadLocal;
    protected final Object[] turboLookup = new Object[MAX_LIVE_TURBO_THREADS];
    protected S mainThreadValue = null;

    protected FastThreadLocal(ThreadLocal<S> threadLocal) {
        this.threadLocal = threadLocal;
        liveThreadLocals.add(new WeakReference<>(this));
    }

    public static void setMainThread(Thread mainThread) {
        MAIN_THREAD = mainThread;
    }

    protected boolean isMainThread(Thread thread) {
        return thread == MAIN_THREAD;
    }

    private void doCleanup(TurboThread thread) {
        turboLookup[thread.setIndex] = null;
    }

    // region Turbo Thread
    @NoArgsConstructor
    public static class TurboThread extends Thread {
        private static final BitSet theSet = new BitSet(MAX_LIVE_TURBO_THREADS);
        private boolean started = false;
        private int setIndex;

        @SuppressWarnings("unused")
        public TurboThread(Runnable target) {
            super(target);
        }

        public TurboThread(String name) {
            super(name);
        }

        @SuppressWarnings("unused")
        public TurboThread(Runnable target, String name) {
            super(target, name);
        }

        @Override
        public void run() {
            onStartup();
            try {
                super.run();
            } finally {
                onShutdown();
            }
        }

        protected void onStartup() {
            int i;
            lock.lock();
            try {
                for (i = 0; i < MAX_LIVE_TURBO_THREADS; i++) {
                    if (!theSet.get(i)) {
                        theSet.set(i);
                        setIndex = i;
                        started = true;
                        return;
                    }
                }
                throw new IllegalStateException();
            } finally {
                lock.unlock();
            }
        }

        protected void onShutdown() {
            if (!started) {
                return;
            }
            lock.lock();
            try {
                val iter = liveThreadLocals.iterator();
                while (iter.hasNext()) {
                    val el = iter.next()
                                 .get();
                    if (el == null) {
                        iter.remove();
                        continue;
                    }
                    el.doCleanup(this);
                }
                theSet.clear(setIndex);
            } finally {
                lock.unlock();
            }
        }
    }
    // endregion

    // region Fixed Value
    public static final class FixedValue<S> extends FastThreadLocal<S> {
        private final Supplier<S> initializer;

        public FixedValue(Supplier<S> initializer) {
            super(ThreadLocal.withInitial(initializer));
            this.initializer = initializer;
        }

        public S get() {
            val t = Thread.currentThread();
            if (isMainThread(t)) {
                if (mainThreadValue == null) {
                    mainThreadValue = initializer.get();
                }
                return mainThreadValue;
            } else if (t instanceof TurboThread) {
                val tt = (TurboThread) t;
                //noinspection unchecked
                S value = (S) turboLookup[tt.setIndex];
                if (value == null) {
                    turboLookup[tt.setIndex] = value = initializer.get();
                }
                return value;
            } else {
                return threadLocal.get();
            }
        }
    }
    // endregion

    // region Dynamic Value
    public static final class DynamicValue<S> extends FastThreadLocal<S> {
        public DynamicValue() {
            super(new ThreadLocal<>());
        }

        public void set(S value) {
            val t = Thread.currentThread();
            if (isMainThread(t)) {
                mainThreadValue = value;
            } else if (t instanceof TurboThread) {
                val tt = (TurboThread) t;
                turboLookup[tt.setIndex] = value;
            } else {
                threadLocal.set(value);
            }
        }

        @SuppressWarnings("unchecked")
        public S get() {
            val t = Thread.currentThread();
            if (isMainThread(t)) {
                return mainThreadValue;
            } else if (t instanceof TurboThread) {
                val tt = (TurboThread) t;
                return (S) turboLookup[tt.setIndex];
            } else {
                return threadLocal.get();
            }
        }
    }
    // endregion
}
