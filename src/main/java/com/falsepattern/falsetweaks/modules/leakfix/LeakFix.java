/*
 * FalseTweaks
 *
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

package com.falsepattern.falsetweaks.modules.leakfix;

import com.falsepattern.falsetweaks.config.LeakFixConfig;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LeakFix {
    public static final boolean ENABLED = ModuleConfig.MEMORY_LEAK_FIX == LeakFixState.Enable;
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

    public static int getCachedBufferCount() {
        return freshAllocations.size() + reusableAllocations.size();
    }

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void gc() {
        allocs = 0;
        int reusables = reusableAllocations.size();
        for (int i = 0; i < reusables; i++) {
            GL11.glDeleteLists(reusableAllocations.get(i), 3);
        }
        reusableAllocations.clear();
        int currentSize = freshAllocations.size();
        int targetSize = LeakFixConfig.CACHE_SIZE_TARGET;
        int allocationCount = targetSize - currentSize;
        if (allocationCount > 0) {
            int base = GL11.glGenLists(allocationCount * 3);
            for (int i = 0; i < allocationCount; i++) {
                freshAllocations.add(base + i * 3);
            }
        } else if (allocationCount < 0) {
            for (int i = currentSize - 1; i >= targetSize; i--) {
                GL11.glDeleteLists(freshAllocations.removeAt(i), 3);
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
        return GL11.glGenLists(3);
    }

    public static void releaseWorldRendererBuffer(int buffer) {
        activeBufferCount--;
        for (int i = 0; i < 3; i++) {
            GL11.glNewList(buffer + i, GL11.GL_COMPILE);
            GL11.glEndList();
        }
        reusableAllocations.add(buffer);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
        if (ENABLED) {
            if (e.type.equals(RenderGameOverlayEvent.ElementType.DEBUG)) {
                debugText = true;
                return;
            }
            if (!debugText || !(e instanceof RenderGameOverlayEvent.Text) ||
                !e.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
                return;
            }
            debugText = false;
            val txt = (RenderGameOverlayEvent.Text) e;
            txt.right.add(null);
            txt.right.add("[FalseTweaks Leak Fix]");
            int active = LeakFix.getActiveBufferCount();
            int cached = LeakFix.getCachedBufferCount();
            int total = active + cached;
            txt.right.add("Total chunk renderers: " + total);
            txt.right.add("Active chunk renderers: " + active);
            txt.right.add("Cached chunk renderers: " + cached);
            if (Minecraft.getMinecraft().mcProfiler.profilingEnabled) {
                //Verbose info
                txt.right.add("Allocations since last GC cycle: " + allocs);
                txt.right.add("Total allocations: " + totalAllocs);
                txt.right.add(
                        "Total allocation cache hits: " + hits + " (" + (int) ((100f / totalAllocs) * hits) + "%)");
                txt.right.add(
                        "Total allocation cache misses: " + misses + " (" + (int) ((100f / totalAllocs) * misses) +
                        "%)");
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        long time = System.nanoTime();
        float secondsSinceLastGC = (time - lastGC) / 1000000000f;
        int cacheSize = getCachedBufferCount();
        if (secondsSinceLastGC > 5 || (secondsSinceLastGC > 1 && (cacheSize < (LeakFixConfig.CACHE_SIZE_TARGET / 2) ||
                                                                  cacheSize > (LeakFixConfig.CACHE_SIZE_TARGET * 2)))) {
            gc();
            lastGC = time;
        }
    }
}
