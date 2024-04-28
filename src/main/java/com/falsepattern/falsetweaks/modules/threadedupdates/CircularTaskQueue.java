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
package com.falsepattern.falsetweaks.modules.threadedupdates;

import lombok.val;

import net.minecraft.client.renderer.WorldRenderer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CircularTaskQueue {
    private WorldRenderer[] renderers;
    private final AtomicInteger head = new AtomicInteger();
    private final AtomicInteger tail = new AtomicInteger();
    public void setCapacity(int capacity) {
        head.set(0);
        tail.set(0);
        renderers = new WorldRenderer[capacity];
    }

    public WorldRenderer tryTake() {
        int index = head.get();
        if (index == tail.get()) {
            return null;
        }
        val wr = renderers[index];
        if (!head.compareAndSet(index, modularIncrement(index))) {
            return null;
        }
        return wr;
    }

    private int modularIncrement(int i) {
        return (i + 1) % renderers.length;
    }

    private int modularDecrement(int i) {
        int len = renderers.length;
        return (i - 1 + len) % len;
    }

    public void add(WorldRenderer renderer) {
        val index = tail.get();
        if (index == modularDecrement(head.get())) {
            System.err.println("Queue wraparound!");
            do {
                Thread.yield();
            } while (index == modularDecrement(head.get()));
        }
        renderers[index] = renderer;
        tail.set(modularIncrement(index));
    }

    public void clear() {
        head.set(tail.get());
        Arrays.fill(renderers, null);
    }
}
