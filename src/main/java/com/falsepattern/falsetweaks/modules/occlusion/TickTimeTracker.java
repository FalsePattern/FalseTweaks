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
package com.falsepattern.falsetweaks.modules.occlusion;

import lombok.val;

import java.util.Arrays;

public class TickTimeTracker {
    private static final int WINDOW_SIZE = 25;
    private static final long[] rollingDelta = new long[WINDOW_SIZE];
    // Sane defaults
    static {
        Arrays.fill(rollingDelta, 10_000_000L);
    }
    private static long averageDeltaNanos = 10_000_000L;
    private static int averageFPS = 100;
    private static int current = 0;
    private static long lastTickNanos = 0;

    private static final long ONE_SECOND_NANOS = 1_000_000_000L;

    public static void tick() {
        val currentNanos = System.nanoTime();
        if (lastTickNanos == 0) {
            lastTickNanos = currentNanos;
            return;
        }
        val delta = currentNanos - lastTickNanos;
        lastTickNanos = currentNanos;
        rollingDelta[current] = delta;
        current = (current + 1) % WINDOW_SIZE;
        long newDelta = 0;
        for (int i = 0; i < WINDOW_SIZE; i++) {
            newDelta += rollingDelta[i];
        }
        newDelta /= WINDOW_SIZE;
        //Just in case
        newDelta = Math.max(1, newDelta);
        averageDeltaNanos = newDelta;
        // 10k fps max is a safe bet.
        averageFPS = (int) Math.min(10000L, ONE_SECOND_NANOS / newDelta);
    }

    public static long averageDeltaNanos() {
        return averageDeltaNanos;
    }

    public static int averageFPS() {
        return averageFPS;
    }
}
