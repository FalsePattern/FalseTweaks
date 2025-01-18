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

import lombok.val;
import lombok.var;

import net.minecraft.profiler.Profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

public class FTWorker {
    private static volatile boolean awake = true;
    private static final Semaphore S = new Semaphore(1);
    private static volatile ThreadedTask[] tasks = null;
    private static final TurboTransferQueue<Runnable> singleRunTasks = new TurboTransferQueue<>(S);
    private static final ExecutorThread theThread = new ExecutorThread();
    private static final Profiler profiler = new Profiler();

    static {
        theThread.start();
        profiler.getProfilingData("__MEGATRACE__:fw_");
    }

    public static boolean isThread(Thread thread) {
        return theThread == thread;
    }

    public static Future<?> submit(Runnable task) {
        return doSubmit(new FutureTask<>(task, null));
    }
    public static <T> Future<T> submit(Callable<T> task) {
        return doSubmit(new FutureTask<>(task));
    }

    public static <T> Future<T> doSubmit(FutureTask<T> future) {
        acquire();
        try {
            singleRunTasks.insert(future);
            if (!awake) {
                theThread.interrupt();
            }
        } finally {
            release();
        }
        return future;
    }

    public static void addTask(ThreadedTask task) {
        acquire();
        try {
            if (tasks == null) {
                tasks = new ThreadedTask[]{task};
            } else {
                val newTasks = new ThreadedTask[tasks.length + 1];
                for (int i = 0; i < tasks.length; i++) {
                    newTasks[i] = tasks[i];
                }
                newTasks[tasks.length] = task;
                tasks = newTasks;
            }
        } finally {
            release();
        }
    }

    public static void removeTask(ThreadedTask task) {
        acquire();
        try {
            if (tasks == null)
                return;
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i] == task) {
                    if (tasks.length == 1) {
                        tasks = null;
                        return;
                    }
                    val newTasks = new ThreadedTask[tasks.length - 1];
                    for (int j = 0; j < i; j++) {
                        newTasks[j] = tasks[j];
                    }
                    for (int j = i + 1; j < tasks.length; j++) {
                        newTasks[j - 1] = tasks[j];
                    }
                    tasks = newTasks;
                    return;
                }
            }
        } finally {
            release();
        }
    }

    private static void acquire() {
        while (!S.tryAcquire())
            Thread.yield();
    }

    private static void release() {
        S.release();
    }


    private static class ExecutorThread extends Thread {
        ExecutorThread() {
            super("FalseTweaks Async Executor");
            setDaemon(true);
        }

        private static boolean doRepeatedTasks() {
            acquire();
            val runningTasks = tasks;
            release();
            if (runningTasks == null || runningTasks.length == 0) {
                return false;
            }
            List<ThreadedTask> dead = null;
            boolean didWork = false;
            for (int i = 0, tasksSize = runningTasks.length; i < tasksSize; i++) {
                val task = runningTasks[i];
                if (!task.alive()) {
                    if (dead == null) {
                        dead = new ArrayList<>();
                    }
                    dead.add(task);
                    continue;
                }
                if (!task.doWork(profiler)) {
                    continue;
                }
                didWork |= !task.lazy();
            }
            if (dead != null) {
                for (val d: dead)
                    removeTask(d);
            }
            return didWork;
        }

        private boolean doOneShotTasks() {
            var task = singleRunTasks.remove();
            boolean didWork = false;
            while (task != null) {
                task.run();
                didWork = true;
                task = singleRunTasks.remove();
            }
            return didWork;
        }

        @Override
        public void run() {
            while (true) {
                boolean acquired = false;
                try {
                    acquire();
                    acquired = true;
                    awake = false;
                    release();
                    acquired = false;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                    acquire();
                    acquired = true;
                    awake = true;
                    release();
                    acquired = false;
                    boolean burstMode = false;
                    boolean didWork;
                    long lastWorkTime = System.nanoTime();
                    do {
                        didWork = false;
                        boolean didBurst = doOneShotTasks();
                        burstMode |= didBurst;
                        didWork |= didBurst;
                        didWork |= doRepeatedTasks();
                        if (didBurst) {
                            lastWorkTime = System.nanoTime();
                        }
                    } while (didWork || (burstMode && System.nanoTime() - lastWorkTime < 1_000_000));
                    awake = false;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (acquired) {
                        release();
                        acquired = false;
                    }
                }
            }
        }
    }
}
