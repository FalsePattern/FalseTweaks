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

import com.falsepattern.falsetweaks.modules.threadexec.FTWorker;
import com.falsepattern.falsetweaks.modules.threadexec.ThreadedTask;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.profiler.Profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OcclusionWorker {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final WRComparator sortComp = new WRComparator(false);
    public volatile boolean dirty = false;
    private WorldClient theWorld;
    private FutureTask<WorldRenderer[]> sortingTask;
    private int sortCount;
    private long lastUpdateTime = 0;
    private boolean markedRendererChanged;

    @AllArgsConstructor
    private static class WorkResults {
        public final WorldRenderer[] sortResults;
        public boolean changed;
        public final int count;
        public List<WorldRenderer> toPush;
    }
    private volatile WorkResults sortResults;
    private volatile EntityLivingBase sortView;

    private static final int SORT_WAIT = 0;
    private static final int SORT_SIGNALED = 1;
    private static final int SORT_WORKING = 2;
    private static final int SORT_DONE = 3;
    private final AtomicInteger sortState = new AtomicInteger();
    private final PreSortWorker preSortWorker = new PreSortWorker();
    private class PreSortWorker implements ThreadedTask {
        @Override
        public boolean alive() {
            return true;
        }

        @Override
        public boolean lazy() {
            return true;
        }

        @Override
        public boolean doWork(Profiler profiler) {
            if (!sortState.compareAndSet(SORT_SIGNALED, SORT_WORKING))
                return false;

            val view = sortView;
            if (view == null)
                return false;
            val render = getRender();
            if (render == null)
                return false;

            val swr = render.sortedWorldRenderers;
            if (swr == null)
                return false;

            val rwr = render.worldRenderers;
            if (rwr == null)
                return false;

            boolean sortedWorldRenderersChanged = false;

            profiler.startSection("presort_init");
            try {
                val newSortedWorldRenderers = new WorldRenderer[swr.length];

                int count = 0;

                val toPush = new ArrayList<WorldRenderer>();

                profiler.endStartSection("presort_mark");
                for (int i = 0; i < rwr.length; ++i) {
                    WorldRenderer wr = rwr[i];
                    if (wr == null) {
                        continue;
                    }
                    val ci = ((WorldRendererOcclusion) wr).ft$getCullInfo();
                    if (ci == null) {
                        continue;
                    }
                    ci.isFrustumCheckPending = true;
                    count = markRendererThreaded(wr, ci, view, newSortedWorldRenderers, count, toPush);
                    sortedWorldRenderersChanged |= markedRendererChanged;
                }
                profiler.endStartSection("presort_clear");
                Arrays.fill(newSortedWorldRenderers, count, newSortedWorldRenderers.length, null);

                profiler.endStartSection("presort_finish");
                sortResults = new WorkResults(newSortedWorldRenderers, sortedWorldRenderersChanged, count, toPush);

                sortState.set(SORT_DONE);
                return true;
            } finally {
                profiler.endSection();
            }
        }
    }

    public OcclusionWorker() {
        FTWorker.addTask(preSortWorker);
    }

    public void setWorld(RenderGlobal rg, WorldClient world) {
        theWorld = world;
        reset();
    }

    public void reset() {
        if (sortState.get() == SORT_WAIT) {
            sortResults = null;
            sortView = null;
        } else {
            while (!sortState.compareAndSet(SORT_DONE, SORT_WAIT)) {
                Thread.yield();
            }
            sortResults = null;
            sortView = null;
        }
        dirty = true;
    }

    private RenderGlobal getRender() {
        return getExtendedRender().getRenderGlobal();
    }

    private OcclusionRenderer getExtendedRender() {
        return OcclusionHelpers.renderer;
    }

    public void run(boolean immediate) {
        if (getRender() == null) {
            return;
        }
        EntityLivingBase view = mc.renderViewEntity;
        if (theWorld == null || view == null) {
            return;
        }

        sortView = view;

        theWorld.theProfiler.startSection("mark");

        if (sortState.compareAndSet(SORT_WAIT, SORT_SIGNALED)) {
            theWorld.theProfiler.endSection();
            return;
        }

        val results = sortResults;

        if (results == null) {
            theWorld.theProfiler.endSection();
            return;
        }
        boolean workFinishedThisFrame = sortState.compareAndSet(SORT_DONE, SORT_WAIT);

        val sortedWorldRenderersChanged = results.changed;
        val count = results.count;
        val newSortedWorldRenderers = results.sortResults;
        if (results.toPush != null) {
            val er = getExtendedRender();
            for (val push: results.toPush) {
                er.pushWorkerRenderer(push);
            }
            results.toPush = null;
        }

        sortState.set(SORT_SIGNALED);

        theWorld.theProfiler.endStartSection("sort");

        RenderGlobal render = getRender();

        if (!sortedWorldRenderersChanged && sortingTask != null && sortingTask.isDone()) {
            if (sortingTask.isCancelled()) {
                sortingTask = null;
            } else {
                WorldRenderer[] sortingOutput = null;
                try {
                    sortingOutput = sortingTask.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
                val sortCount = this.sortCount;
                if (sortingOutput != null && sortCount == count) {
                    int copied = 0;
                    for (int i = 0; i < sortCount; i++) {
                        val rend = sortingOutput[i];
                        if (rend.isInitialized && !((WorldRendererOcclusion) rend).ft$skipRenderPass()) {
                            render.sortedWorldRenderers[copied++] = rend;
                        }
                    }
                    render.renderersLoaded = copied;
                }
                sortingTask = null;
            }
        }

        val camX = (float) render.prevRenderSortX;
        val camY = (float) render.prevRenderSortY;
        val camZ = (float) render.prevRenderSortZ;
        boolean moved = sortComp.posX != camX || sortComp.posY != camY || sortComp.posZ != camZ;
        long currentTime = System.currentTimeMillis();
        if (sortedWorldRenderersChanged || moved || (currentTime - lastUpdateTime) > 100) {
            results.changed = false;
            lastUpdateTime = currentTime;
            val sorter = new InterruptableSorter<>(sortComp);
            sortComp.posX = camX;
            sortComp.posY = camY;
            sortComp.posZ = camZ;
            if (sortingTask != null) {
                sortingTask.cancel(true);
            }
            sortCount = count;
            int finalCount = count;
            sortingTask = new FutureTask<>(() -> {
                try {
                    sorter.interruptableSort(newSortedWorldRenderers, 0, finalCount - 1);
                    return newSortedWorldRenderers;
                } catch (Throwable ignored) {
                }
                return null;
            });
            FTWorker.doSubmit(sortingTask);
        }

        dirty = false;
        theWorld.theProfiler.endSection();
    }

    private int markRendererThreaded(WorldRenderer rend, CullInfo info, EntityLivingBase view, WorldRenderer[] sortedWorldRenderers, int renderersLoaded, List<WorldRenderer> toPush) {
        val newID = renderersLoaded++;
        markedRendererChanged = info.prevSortIndex != newID;
        info.prevSortIndex = newID;

        sortedWorldRenderers[newID] = rend;
        if (rend.needsUpdate || !rend.isInitialized) {
            rend.needsUpdate = true;
            if (!rend.isInitialized || rend.distanceToEntitySquared(view) <= 1128.0F) {
                val wro = ((WorldRendererOcclusion)rend);
                if (!wro.ft$isInUpdateList()) {
                    toPush.add(rend);
                } else {
                    wro.ft$currentPriority(getExtendedRender().determinePriority(rend));
                }
            }
        }
        return renderersLoaded;
    }

    public static class CullInfo {
        /**
         * The index of the world renderer in RenderGlobal#worldRenderers. Not stored as a reference because I
         * found that having it slows things down significantly.
         */
        public int wrIdx;
        public int prevSortIndex = -1;
        /**
         * All the directions we have stepped in to reach this subchunk.
         */
        public volatile boolean isFrustumCheckPending;
        public volatile boolean isFrustumStateUpdated;
    }
}
