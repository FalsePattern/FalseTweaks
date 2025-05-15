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

package com.falsepattern.falsetweaks.modules.occlusion.leakfix;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.config.OcclusionConfig;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LeakFix {
    private static final LeakFix INSTANCE = new LeakFix();
    private static final TIntList freshAllocations = new TIntArrayList();
    private static final TIntList reusableAllocations = new TIntArrayList();
    @Getter
    private static int activeBufferCount = 0;
    private static boolean debugText = false;
    private static int allocs = 0;
    private static int totalAllocs = 0;
    private static int hits = 0;
    private static int misses = 0;

    private static long lastGC = 0;

    private static int size() {
        int count = 2;
        if (Compat.neodymiumActive()) {
            count -= 2;
        }
        return count;
    }

    public static int getCachedBufferCount() {
        return freshAllocations.size() + reusableAllocations.size();
    }

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void gc() {
        val size = size();
        if (size == 0) {
            return;
        }
        allocs = 0;
        int reusables = reusableAllocations.size();
        for (int i = 0; i < reusables; i++) {
            GL11.glDeleteLists(reusableAllocations.get(i), size);
        }
        reusableAllocations.clear();
        int currentSize = freshAllocations.size();
        int targetSize = OcclusionConfig.CACHE_SIZE_TARGET;
        int allocationCount = targetSize - currentSize;
        if (allocationCount > 0) {
            int base = GL11.glGenLists(allocationCount * size);
            for (int i = 0; i < allocationCount; i++) {
                freshAllocations.add(base + i * size);
            }
        } else if (allocationCount < 0) {
            for (int i = currentSize - 1; i >= targetSize; i--) {
                GL11.glDeleteLists(freshAllocations.removeAt(i), size);
            }
        }
    }

    public static int allocateWorldRendererBuffer() {
        activeBufferCount++;
        allocs++;
        totalAllocs++;
        int reusables = reusableAllocations.size();
        if (reusables > 0) {
            hits++;
            return reusableAllocations.removeAt(reusables - 1);
        }
        int fresh = freshAllocations.size();
        if (fresh > 0) {
            hits++;
            return freshAllocations.removeAt(fresh - 1);
        }
        misses++;
        val size = size();
        if (size == 0) {
            return -1;
        }
        return GL11.glGenLists(size());
    }

    public static void releaseWorldRendererBuffer(int buffer) {
        activeBufferCount--;
        reusableAllocations.add(buffer);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
        if (e.type.equals(RenderGameOverlayEvent.ElementType.DEBUG)) {
            debugText = true;
            return;
        }
        if (!debugText || !(e instanceof RenderGameOverlayEvent.Text) || !e.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            return;
        }
        debugText = false;
        val txt = (RenderGameOverlayEvent.Text) e;
        txt.right.add(null);
        txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.title"));
        int active = LeakFix.getActiveBufferCount();
        int cached = LeakFix.getCachedBufferCount();
        int total = active + cached;
        if (Compat.neodymiumActive()) {
            if (ThreadedChunkUpdateHelper.AGGRESSIVE_NEODYMIUM_THREADING) {
                txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.ndaggressive"));
            } else {
                txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.ndcompat"));
            }
            return;
        }

        txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.total", total));
        txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.active", active));
        txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.cached", cached));
        if (Minecraft.getMinecraft().mcProfiler.profilingEnabled) {
            //Verbose info
            txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.alloc.gc", allocs));
            txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.alloc.total", totalAllocs));
            txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.alloc.hits", hits, (int) ((100f / totalAllocs) * hits)));
            txt.right.add(I18n.format("gui.falsetweaks.occlusion.debug.alloc.miss", misses, (int) ((100f / totalAllocs) * misses)));
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        long time = System.nanoTime();
        float secondsSinceLastGC = (time - lastGC) / 1000000000f;
        int cacheSize = getCachedBufferCount();
        if (secondsSinceLastGC > 5 || (secondsSinceLastGC > 1 && (cacheSize < (OcclusionConfig.CACHE_SIZE_TARGET / 2) || cacheSize > (OcclusionConfig.CACHE_SIZE_TARGET * 2)))) {
            gc();
            lastGC = time;
        }
    }
}
