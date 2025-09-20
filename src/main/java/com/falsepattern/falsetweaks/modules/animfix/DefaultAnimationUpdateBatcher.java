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

package com.falsepattern.falsetweaks.modules.animfix;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.api.animfix.IAnimationUpdateBatcher;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.GLAllocation;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DefaultAnimationUpdateBatcher implements IAnimationUpdateBatcher {
    private final int mipLevels;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;
    private final int[] offsets;
    private final IntBuffer memory;
    private final Semaphore batchingSemaphore = new Semaphore(0);
    private final Semaphore uploadingSemaphore = new Semaphore(0);
    private final Thread thread;
    private volatile Buffer backBuffer = new Buffer();
    private volatile boolean running = true;

    @SneakyThrows
    DefaultAnimationUpdateBatcher(int xOffset, int yOffset, int width, int height, int mipLevel) {
        this.mipLevels = mipLevel;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
        offsets = new int[mipLevel + 1];
        int size = 0;
        for (int i = 0; i <= mipLevel; i++) {
            offsets[i] = size;
            size += (width >>> i) * (height >>> i);
        }
        memory = GLAllocation.createDirectIntBuffer(size);
        thread = new Thread(this::run);
        thread.setName(Tags.MOD_NAME + " texture batching thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public boolean scheduleUpload(int[][] texture, int width, int height, int xOffset, int yOffset) {
        xOffset -= this.xOffset;
        yOffset -= this.yOffset;
        if (xOffset < 0 || xOffset >= this.width || yOffset < 0 || yOffset >= this.height) {
            return false;
        }
        int i = backBuffer.queuedCount++;
        val dimList = backBuffer.queuedDimensions;
        while (dimList.size() <= i) {
            dimList.add(new int[4]);
        }
        int[] dims = dimList.get(i);
        dims[0] = width;
        dims[1] = height;
        dims[2] = xOffset;
        dims[3] = yOffset;
        backBuffer.queuedTextures.add(texture);
        return true;
    }

    @Override
    public void terminate() {
        running = false;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void upload() {
        batchingSemaphore.release();
        while (!uploadingSemaphore.tryAcquire()) {
            Thread.yield();
        }
        for (int i = 0; i <= mipLevels; i++) {
            memory.position(offsets[i]);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,
                                 i,
                                 xOffset >>> i,
                                 yOffset >>> i,
                                 width >>> i,
                                 height >>> i,
                                 GL12.GL_BGRA,
                                 GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                                 memory);
        }
        batchingSemaphore.release();
        while (!uploadingSemaphore.tryAcquire()) {
            Thread.yield();
        }
    }

    @SneakyThrows
    private void run() {
        var frontBuffer = new Buffer();
        var backBuffer = this.backBuffer;
        while (running) {
            uploadingSemaphore.release();
            while (running) {
                try {
                    batchingSemaphore.acquire();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
            if (!running) {
                return;
            }
            {
                val tmp = frontBuffer;
                frontBuffer = backBuffer;
                this.backBuffer = backBuffer = tmp;
            }
            uploadingSemaphore.release();
            while (running) {
                try {
                    batchingSemaphore.acquire();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
            if (!running) {
                return;
            }
            int i = frontBuffer.queuedCount - 1;
            val dfb = frontBuffer.queuedDimensions;
            val tfb = frontBuffer.queuedTextures;
            for (; i >= 0; i--) {
                batchUpload(dfb.get(i), tfb.remove(i));
            }
            frontBuffer.queuedCount = 0;
        }
    }

    private void batchUpload(int[] dims, int[][] texture) {
        int width = dims[0];
        int height = dims[1];
        int xOffset = dims[2];
        int yOffset = dims[3];
        int w = this.width;
        for (int mipMapLevel = 0; mipMapLevel < texture.length; mipMapLevel++) {
            int base = offsets[mipMapLevel] + yOffset * w + xOffset;
            for (int i = 0; i < height; i++) {
                memory.position(base + i * w);
                memory.put(texture[mipMapLevel], i * width, width);
            }
            xOffset >>>= 1;
            yOffset >>>= 1;
            width >>>= 1;
            height >>>= 1;
            w >>>= 1;
        }
    }

    private static class Buffer {
        public final List<int[][]> queuedTextures = new ArrayList<>();
        public final List<int[]> queuedDimensions = new ArrayList<>();
        public int queuedCount = 0;
    }
}
