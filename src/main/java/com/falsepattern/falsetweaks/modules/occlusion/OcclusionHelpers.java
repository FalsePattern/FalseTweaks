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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.modules.debug.DebugLogging;
import com.falsepattern.falsetweaks.modules.occlusion.util.IntStack;

import net.minecraft.util.EnumFacing;

public class OcclusionHelpers {
    public static final EnumFacing[] FACING_VALUES = EnumFacing.values();
    public static final boolean DEBUG_ALWAYS_RUN_OCCLUSION = Boolean.parseBoolean(System.getProperty(Tags.MOD_ID + ".debug.alwaysRunOcclusion", "false"));
    public static final boolean DEBUG_PRINT_QUEUE_ITERATIONS = Boolean.parseBoolean(System.getProperty(Tags.MOD_ID + ".debug.printQueueIterations", "false"));
    /**
     * Update up to 1 chunk per frame when the framerate is uncapped, vanilla-style.
     */
    public static final boolean DEBUG_LAZY_CHUNK_UPDATES = Boolean.parseBoolean(System.getProperty(Tags.MOD_ID + ".debug.lazyChunkUpdates", "false"));
    /**
     * Disable speeding up chunk updates when the camera is static.
     */
    public static final boolean DEBUG_NO_UPDATE_ACCELERATION = Boolean.parseBoolean(System.getProperty(Tags.MOD_ID + ".debug.noUpdateAcceleration", "false"));
    public static OcclusionWorker worker;
    public static OcclusionRenderer renderer;
    public static long chunkUpdateDeadline;
    public static float partialTickTime;
    public static IntStack deferredAreas = new IntStack(6 * 1024);

    public static void init() {
        worker = new OcclusionWorker();
    }

    public static synchronized void updateArea(int x, int y, int z, int x2, int y2, int z2) {
        DebugLogging.debugLog(() -> String.format("Marking Area from[x=%d,y=%d,z=%d] to[x=%d,y=%d,z=%d] for update (deferred)", x, y, z, x2, y2, z2));

        // backwards so it's more logical to extract
        deferredAreas.add(z2);
        deferredAreas.add(y2);
        deferredAreas.add(x2);
        deferredAreas.add(z);
        deferredAreas.add(y);
        deferredAreas.add(x);
    }

    public static synchronized void processUpdate(OcclusionRenderer render) {
        if (deferredAreas.isEmpty()) {
            return; // guard against multiple instances (no compatibility with mods that do this to us)
        }

        int x = deferredAreas.pop(), y = deferredAreas.pop(), z = deferredAreas.pop();
        int x2 = deferredAreas.pop(), y2 = deferredAreas.pop(), z2 = deferredAreas.pop();
        render.internalMarkBlockUpdate(x, y, z, x2, y2, z2);
    }
}
