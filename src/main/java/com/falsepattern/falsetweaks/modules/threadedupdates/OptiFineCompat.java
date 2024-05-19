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

import com.falsepattern.falsetweaks.Compat;
import shadersmod.client.Shaders;
import stubpackage.ChunkCacheOF;
import stubpackage.Config;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.io.IOException;

public class OptiFineCompat {
    public static boolean scheduledReload = false;
    private static Boolean HAS_CHUNKCACHE = null;

    public static void resizeNativeBuffers(Tessellator tessellator) {
        if (Compat.optiFineInstalled()) {
            ((ITessellatorOptiFineCompat) tessellator).ft$resizeNativeBuffers();
        }
    }

    public static void popEntity() {
        if (!Compat.optiFineHasShaders() || !Config.isShaders()) {
            return;
        }
        Shaders.popEntity();
    }

    public static ChunkCache createChunkCache(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int subIn) {
        if (HAS_CHUNKCACHE == null) {
            if (Compat.optiFineInstalled()) {
                try {
                    HAS_CHUNKCACHE = Launch.classLoader.getClassBytes("ChunkCacheOF") != null;
                } catch (IOException e) {
                    e.printStackTrace();
                    HAS_CHUNKCACHE = false;
                }
            } else {
                HAS_CHUNKCACHE = false;
            }
        }
        if (HAS_CHUNKCACHE) {
            return WrappedOF.createOFChunkCache(world, xMin, yMin, zMin, xMax, yMax, zMax, subIn);
        } else {
            return new ChunkCache(world, xMin, yMin, zMin, xMax, yMax, zMax, subIn);
        }
    }

    public static class ThreadSafeEntityData {
        public static final FastThreadLocal.FixedValue<ThreadSafeEntityData> TL = new FastThreadLocal.FixedValue<>(ThreadSafeEntityData::new);

        public final int[] entityData = new int[32];
        public int entityDataIndex = 0;
    }

    private static class WrappedOF {
        private static ChunkCache createOFChunkCache(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int subIn) {
            return new ChunkCacheOF(world, xMin, yMin, zMin, xMax, yMax, zMax, subIn);
        }
    }
}
