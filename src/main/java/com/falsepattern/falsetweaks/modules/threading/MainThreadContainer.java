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

/**
 * Separated here to prevent cascading class loading.
 */
public final class MainThreadContainer {
    private static Thread MAIN_THREAD;

    private MainThreadContainer() {
    }

    /**
     * @see com.falsepattern.falsetweaks.asm.CoreLoadingPlugin
     */
    public static void setAsMainThread() {
        if (MAIN_THREAD != null) {
            throw new AssertionError("Main thread set twice");
        }
        MAIN_THREAD = Thread.currentThread();
        //Not using a logger to avoid classloading
        System.out.println("[FalseTweaks MTC] Thread marked as main");
    }

    public static boolean isMainThread() {
        return MAIN_THREAD == Thread.currentThread();
    }

    public static Thread get() {
        return MAIN_THREAD;
    }
}
