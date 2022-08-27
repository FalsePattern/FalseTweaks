/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.animfix;

import lombok.SneakyThrows;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import sun.misc.Unsafe;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;

import cpw.mods.fml.relauncher.ReflectionHelper;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AnimationUpdateBatcher {
    public static TextureMap currentAtlas = null;
    public static AnimationUpdateBatcher batcher = null;
    public static String currentName = null;
    private static final Unsafe unsafe = ReflectionHelper.getPrivateValue(Unsafe.class, null, "theUnsafe");
    private static final int arrayBaseOffset = unsafe.arrayBaseOffset(int[].class);

    private final int mipLevels;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;
    private final int[] offsets;
    private final long[] baseOffsets;
    private final IntBuffer memory;

    private final List<int[][]> queuedTextures1 = new ArrayList<>();
    private final List<int[]> queuedDimensions1 = new ArrayList<>();
    private final List<int[][]> queuedTextures2 = new ArrayList<>();
    private final List<int[]> queuedDimensions2 = new ArrayList<>();

    private final AtomicInteger queuedCount1 = new AtomicInteger(0);
    private final AtomicInteger queuedCount2 = new AtomicInteger(0);
    private final Semaphore batchingSemaphore = new Semaphore(0);
    private final Semaphore uploadingSemaphore = new Semaphore(1);
    private final Thread thread;
    private final AtomicBoolean flipped = new AtomicBoolean(false);

    private final AtomicBoolean running = new AtomicBoolean(true);

    @SneakyThrows
    public AnimationUpdateBatcher(int xOffset, int yOffset, int width, int height, int mipLevel) {
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
        long basePointer = ReflectionHelper.getPrivateValue(Buffer.class, memory, "address");
        baseOffsets = new long[mipLevel + 1];
        for (int i = 0; i <= mipLevel; i++) {
            baseOffsets[i] = basePointer + ((long)offsets[i] << 2);
        }
        thread = new Thread(this::run);
        thread.setName("AnimFix batch thread (" + currentName + ")");
        thread.setDaemon(true);
        thread.start();
    }
    public boolean scheduleUpload(int[][] texture, int width, int height, int xOffset, int yOffset) {
        xOffset -= this.xOffset;
        yOffset -= this.yOffset;
        if (xOffset < 0 || xOffset >= this.width || yOffset < 0 || yOffset >= this.height) {
            return false;
        }
        boolean f = flipped.get();
        int i = (f ? queuedCount2 : queuedCount1).getAndIncrement();
        val dimList = (f ? queuedDimensions2 : queuedDimensions1);
        while (dimList.size() <= i) {
            dimList.add(new int[4]);
        }
        int[] dims = dimList.get(i);
        dims[0] = width;
        dims[1] = height;
        dims[2] = xOffset;
        dims[3] = yOffset;
        (f ? queuedTextures2 : queuedTextures1).add(texture);
        return true;
    }

    public void terminate() {
        running.set(false);
        thread.interrupt();
    }

    @SneakyThrows
    private void run() {
        while (running.get()) {
            try {
                batchingSemaphore.acquire();
            } catch (InterruptedException ignored) {
                continue;
            }
            val f = flipped.get();
            int i = (f ? queuedCount1 : queuedCount2).get() - 1;
            val dfb = f ? queuedDimensions1 : queuedDimensions2;
            val tfb = f ? queuedTextures1 : queuedTextures2;
            for (; i >= 0; i--) {
                batchUpload(dfb.get(i), tfb.remove(i));
            }
            (f ? queuedCount1 : queuedCount2).set(0);
            flipped.set(!flipped.get());
            uploadingSemaphore.release();
        }
    }

    private void batchUpload(int[] dims, int[][] texture) {
        int width = dims[0];
        int height = dims[1];
        int xOffset = dims[2];
        int yOffset = dims[3];
        xOffset <<= 2;
        width <<= 2;
        int w = this.width << 2;
        for (int mipMapLevel = 0; mipMapLevel < texture.length; mipMapLevel++) {
            long base = baseOffsets[mipMapLevel] + (long) yOffset * w + xOffset;
            for (long i = 0; i < height; i++) {
                unsafe.copyMemory(texture[mipMapLevel], arrayBaseOffset + i * width, null, base + i * w, width);
            }
            xOffset >>>= 1;
            yOffset >>>= 1;
            width >>>= 1;
            height >>>= 1;
            w >>>= 1;
        }
    }

    @SneakyThrows
    public void upload() {
        while (!uploadingSemaphore.tryAcquire()) {
            Thread.yield();
        }
        for (int i = 0; i <= mipLevels; i++) {
            memory.position(offsets[i]);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, xOffset >>> i, yOffset >>> i, width >>> i, height >>> i,
                                 GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, memory);
        }
        batchingSemaphore.release();
    }
}
