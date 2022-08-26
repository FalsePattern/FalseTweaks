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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import sun.misc.Unsafe;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;

import cpw.mods.fml.relauncher.ReflectionHelper;

import java.nio.Buffer;
import java.nio.IntBuffer;

public class AnimationUpdateBatcher {
    public static TextureMap currentAtlas = null;
    public static AnimationUpdateBatcher batcher = null;
    private static final Unsafe unsafe = ReflectionHelper.getPrivateValue(Unsafe.class, null, "theUnsafe");
    private static final long arrayBaseOffset = unsafe.arrayBaseOffset(int[].class);
    private final int mipLevels;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;
    private final int[] offsets;
    private final IntBuffer memory;
    private final long basePointer;

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
        basePointer = ReflectionHelper.getPrivateValue(Buffer.class, memory, "address");
    }

    public boolean batchUpload(int[][] texture, int width, int height, int xOffset, int yOffset) {
        xOffset -= this.xOffset;
        yOffset -= this.yOffset;
        if (xOffset < 0 || xOffset >= this.width || yOffset < 0 || yOffset >= this.height) {
            return false;
        }
        int w = this.width;
        for (int mipMapLevel = 0; mipMapLevel < texture.length; mipMapLevel++) {
            long base = basePointer + ((offsets[mipMapLevel] + (long) yOffset * w + xOffset) << 2);
            for (int i = 0; i < height; i++) {
                unsafe.copyMemory(texture[mipMapLevel], arrayBaseOffset + (((long) i * width) << 2), null, base + (((long) i * w)<< 2), (long) width << 2);
            }
            xOffset >>>= 1;
            yOffset >>>= 1;
            width >>>= 1;
            height >>>= 1;
            w >>>= 1;
        }
        return true;
    }

    @SneakyThrows
    public void upload() {
        for (int i = 0; i <= mipLevels; i++) {
            memory.position(offsets[i]);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, xOffset >>> i, yOffset >>> i, width >>> i, height >>> i,
                                 GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, memory);
        }
    }
}
