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

package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.debug.Debug;
import com.falsepattern.falsetweaks.modules.debug.DebugLogging;
import com.falsepattern.falsetweaks.modules.occlusion.*;
import com.falsepattern.falsetweaks.modules.threadexec.FTWorker;
import com.falsepattern.falsetweaks.modules.threadexec.ThreadedTask;
import com.falsepattern.falsetweaks.modules.triangulator.ToggleableTessellatorManager;
import com.google.common.base.Preconditions;
import com.gtnewhorizon.gtnhlib.api.CapturingTesselator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mega.fluidlogged.api.FLBlockAccess;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.falsepattern.falsetweaks.modules.occlusion.OcclusionRenderer.*;
import static com.falsepattern.falsetweaks.modules.threadedupdates.MainThreadContainer.MAIN_THREAD;

public class ThreadedChunkUpdateHelper implements IRenderGlobalListener {
    public static final boolean AGGRESSIVE_NEODYMIUM_THREADING = ThreadingConfig.AGGRESSIVE_NEODYMIUM_THREADING();
    public static final RenderBlocksStack renderBlocksStack = new RenderBlocksStack();
    private static final int BIT_NextPass = 0b1;
    private static final int BIT_RenderedSomething = 0b10;
    private static final int BIT_StartedTessellator = 0b100;
    public static ThreadedChunkUpdateHelper instance;
    /**
     * Used within the scope of WorldRenderer#updateRenderer (on the main thread).
     */
    public static WorldRenderer lastWorldRenderer;
    /**
     * Finished tasks ready for consumption
     */
    public BlockingDeque<WorldRenderer> finishedTasks = new LinkedBlockingDeque<>();

    public FastThreadLocal.FixedValue<Tessellator> threadTessellator = new FastThreadLocal.FixedValue<>(Tessellator::new);

    IRendererUpdateOrderProvider rendererUpdateOrderProvider = new IRendererUpdateOrderProvider() {
        /** The renderers updated during the batch */
        private final DoubleBuffered<List<WorldRenderer>> worldRenderersToUpdateListInternal = new DoubleBuffered<>(ArrayList::new);
        private final DoubleBuffered<List<WorldRenderer>> updatedRenderers = new DoubleBuffered<>(ArrayList::new);
        private final DoubleBuffered<Set<WorldRenderer>> updatedRenderersSet = new DoubleBuffered<>(HashSet::new);

        private WorldRenderer nextRenderer;

        @Override
        public void prepare(List<WorldRenderer> worldRenderersToUpdateList, int updateLimit) {
            val front = worldRenderersToUpdateListInternal.front();
            val back = worldRenderersToUpdateListInternal.back();
            worldRenderersToUpdateListInternal.flip();
            back.clear();
            val size = front.size();
            for (int i = 0; i < size; i++) {
                val wr = front.get(i);
                val task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
                if (!task.inFinishedQueue) {
                    DebugLogging.debugLog(() -> "Fresh " + wr + " added to back buffer");
                    back.add(wr);
                } else {
                    DebugLogging.debugLog(() -> "Finished " + wr + " ignored");
                    ((WorldRendererOcclusion) wr).ft$setInUpdateList(false);
                }
            }
            front.clear();
            back.addAll(worldRenderersToUpdateList);
            worldRenderersToUpdateList.clear();
            preRendererUpdates(back, updateLimit);
        }

        @Override
        public boolean hasNext() {
            WorldRenderer wr;

            while ((wr = finishedTasks.poll()) != null) {
                UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
                task.inFinishedQueue = false;
                if (task.cancelled || (!wr.needsUpdate && !((WorldRendererOcclusion) wr).ft$needsSort())) {
                    if (AGGRESSIVE_NEODYMIUM_THREADING) {
                        NeodymiumCompat.safeDiscardTask(task);
                    }
                    task.clear();
                } else {
                    nextRenderer = wr;
                    return true;
                }
            }
            return false;
        }

        @Override
        public WorldRenderer next() {
            Preconditions.checkNotNull(nextRenderer);
            WorldRenderer wr = nextRenderer;
            nextRenderer = null;
            updatedRenderers.front().add(wr);
            updatedRenderersSet.front().add(wr);

            DebugLogging.debugLog(() -> "Consuming renderer " + wr + " " + worldRendererUpdateTaskToString(wr));

            return wr;
        }

        @Override
        public Future<List<WorldRenderer>> cleanup() {
            val updatedRenderers = this.updatedRenderers.front();
            this.updatedRenderers.flip();
            val updatedRenderersSet = this.updatedRenderersSet.front();
            this.updatedRenderersSet.flip();
            val worldRenderersToUpdateList = worldRenderersToUpdateListInternal.front();

            nextRenderer = null;
            return FTWorker.submit(() -> {
                for (int i = 0, updatedRenderersSize = updatedRenderers.size(); i < updatedRenderersSize; ++i) {
                    WorldRenderer wr = updatedRenderers.get(i);
                    val task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
                    if (AGGRESSIVE_NEODYMIUM_THREADING) {
                        NeodymiumCompat.safeDiscardTask(task);
                    }
                    task.clear();
                }
                updatedRenderers.clear();
                for (int i = 0, size = worldRenderersToUpdateList.size(); i < size; ++i) {
                    val wr = worldRenderersToUpdateList.get(i);
                    if (!updatedRenderersSet.contains(wr)) {
                        updatedRenderers.add(wr);
                    }
                }
                worldRenderersToUpdateList.clear();
                worldRenderersToUpdateList.addAll(updatedRenderers);
                updatedRenderers.clear();
                updatedRenderersSet.clear();
                return worldRenderersToUpdateList;
            });
        }
    };
    /**
     * Tasks not yet started
     */
    private AtomicReference<PendingTaskUpdate> taskQueueUnsorted = null;
    private AtomicBoolean run = null;
    private WorkerThread[] currentThreads = null;
    private WorkSorterThread workSorter = null;

    private static boolean hasFlag(int flags, int bit) {
        return (flags & bit) != 0;
    }

    private static int doChunkUpdateForRenderPass(WorldRenderer wr, UpdateTask task, ChunkCache chunkcache, Tessellator tess, int pass, RenderBlocks renderblocks) {
        int flags = 0;

        BlockLoop:
        for (int y = wr.posY; y < wr.posY + 16; ++y) {
            for (int z = wr.posZ; z < wr.posZ + 16; ++z) {
                for (int x = wr.posX; x < wr.posX + 16; ++x) {
                    if (task.cancelled) {
                        DebugLogging.debugLog(() -> "Realized renderer " + wr + " is dirty, aborting update");
                        break BlockLoop;
                    }
                    flags = doChunkUpdateForRenderPassBlock(wr, task, chunkcache, tess, pass, renderblocks, x, y, z, flags);
                }
            }
        }

        return flags;
    }

    private static final IntSet alreadyWarnedRenderTypes = new IntArraySet();

    private static class FluidLoggedCompat {
        public static int drawFluidLogged(WorldRenderer wr, UpdateTask task, ChunkCache chunkcache, Tessellator tess, int pass, int flags, RenderBlocks renderblocks, int x, int y, int z) {
            val fluid = ((FLBlockAccess)chunkcache).fl$getFluid(x, y, z);
            if (fluid == null) {
                return flags;
            }
            val fluidBlock = fluid.getBlock();
            if (fluidBlock == null) {
                return flags;
            }

            if (fluidBlock.getRenderBlockPass() > pass) {
                flags |= BIT_NextPass;
            }
            if (!fluidBlock.canRenderInPass(pass)) {
                return flags;
            }

            flags = startTessellator(wr, task, tess, pass, flags);

            try {
                flags |= renderblocks.renderBlockByRenderType(fluidBlock, x, y, z) ? BIT_RenderedSomething : 0;
            } catch (Exception ignored) {}
            return flags;
        }
    }

    private static int doChunkUpdateForRenderPassBlock(WorldRenderer wr, UpdateTask task, ChunkCache chunkcache, Tessellator tess, int pass, RenderBlocks renderblocks, int x, int y, int z, int flags) {
        Block block = chunkcache.getBlock(x, y, z);
        val rt = block.getRenderType();

        if (!(AGGRESSIVE_NEODYMIUM_THREADING || renderOffThread(block, rt))) {
            return flags;
        }

        if (block.getMaterial() == Material.air) {
            return flags;
        }

        if (AGGRESSIVE_NEODYMIUM_THREADING && pass == 0 && block.hasTileEntity(chunkcache.getBlockMetadata(x, y, z))) {
            val tileEntity = chunkcache.getTileEntity(x, y, z);
            if (TileEntityRendererDispatcher.instance.hasSpecialRenderer(tileEntity)) {
                task.TESRs.add(tileEntity);
            }
        }

        if (Compat.fluidloggedInstalled()) {
            flags = FluidLoggedCompat.drawFluidLogged(wr, task, chunkcache, tess, pass, flags, renderblocks, x, y, z);
        }

        if (block.getRenderBlockPass() > pass) {
            flags |= BIT_NextPass;
        }

        if (!block.canRenderInPass(pass)) {
            return flags;
        }

        flags = startTessellator(wr, task, tess, pass, flags);

        try {
            flags |= renderblocks.renderBlockByRenderType(block, x, y, z) ? BIT_RenderedSomething : 0;
        } catch (Exception e) {
            synchronized (alreadyWarnedRenderTypes) {
                if (!alreadyWarnedRenderTypes.contains(rt)) {
                    alreadyWarnedRenderTypes.add(rt);
                    Share.log.error("Exception while rendering a block!", e);
                }
            }
        }

        if (block.getRenderType() == 0 && x == playerX && y == playerY && z == playerZ) {
            renderblocks.setRenderFromInside(true);
            renderblocks.setRenderAllFaces(true);
            renderblocks.renderBlockByRenderType(block, x, y, z);
            renderblocks.setRenderFromInside(false);
            renderblocks.setRenderAllFaces(false);
        }
        return flags;
    }

    private static int startTessellator(WorldRenderer wr, UpdateTask task, Tessellator tess, int pass, int flags) {
        if (!hasFlag(flags, BIT_StartedTessellator)) {
            if (ModuleConfig.TRIANGULATOR()) {
                ToggleableTessellatorManager.preRenderBlocks(pass);
            }
            flags |= BIT_StartedTessellator;
            if (AGGRESSIVE_NEODYMIUM_THREADING) {
                NeodymiumCompat.beginRenderPass(task, wr, pass);
            }
            tess.startDrawingQuads();
            tess.setTranslation(-wr.posX, -wr.posY, -wr.posZ);
        }
        return flags;
    }

    public static boolean isMainThread() {
        val thread = Thread.currentThread();
        if (MAIN_THREAD == null)
            return thread.getName().equals("Client thread");
        return thread == MAIN_THREAD;
    }

    private static String worldRendererUpdateTaskToString(WorldRenderer wr) {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        return task.result[0].renderedSomething + "";
        // TODO + " (" + (task.result[0].renderedQuads == null ? "null" : task.result[0].renderedQuads.getVertexCount()) + ")/" + task.result[1].renderedSomething + " (" + (task.result[1].renderedQuads == null ? "null" : task.result[1].renderedQuads.getVertexCount()) + ")";
    }

    public void init() {
        OcclusionHelpers.renderer.ft$setRendererUpdateOrderProvider(rendererUpdateOrderProvider);
        OcclusionHelpers.renderer.ft$addRenderGlobalListener(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public void spawnThreads(RenderGlobal rg) {
        if (rg.theWorld == null)
            return;
        int threads = ThreadingConfig.CHUNK_UPDATE_THREADS;

        threads = Math.max(1, Math.min(threads, 8));

        if (run != null) {
            run.set(false);
        }
        if (currentThreads != null) {
            for (val thread : currentThreads) {
                thread.interrupt();
            }
            for (val thread : currentThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            currentThreads = null;
        }
        if (workSorter != null) {
            FTWorker.removeTask(workSorter);
            workSorter = null;
        }
        if (taskQueueUnsorted != null) {
            taskQueueUnsorted.set(null);
        }
        Share.log.info("Creating " + threads + " chunk builder" + (threads > 1 ? "s" : ""));
        String nameBase = "Chunk Update Worker #";
        if (Loader.isModLoaded("lumina") || Loader.isModLoaded("lumi")) {
            nameBase = "$LUMI_NO_RELIGHT" + nameBase;
        }
        CircularTaskQueue taskQueue = new CircularTaskQueue(rg.worldRenderers.length);
        taskQueueUnsorted = new AtomicReference<>(null);
        run = new AtomicBoolean(true);
        workSorter = new WorkSorterThread(run, taskQueueUnsorted, taskQueue);
        FTWorker.addTask(workSorter);
        currentThreads = new WorkerThread[threads];
        for (int i = 0; i < threads; i++) {
            val t = new WorkerThread(nameBase + i, run, taskQueue);
            currentThreads[i] = t;
            t.setDaemon(true);
            t.start();
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (!e.modID.equals(Tags.MOD_ID)) {
            return;
        }
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    private void preRendererUpdates(List<WorldRenderer> toUpdateList, int updateLimit) {
        if (taskQueueUnsorted == null)
            return;
        updateWorkQueue(toUpdateList, updateLimit);
        removeCancelledResults();
    }

    private void updateWorkQueue(List<WorldRenderer> toUpdateList, int updateLimit) {
        if (toUpdateList.isEmpty())
            return;
        DebugLogging.debugLog(() -> "Updating " + toUpdateList.size() + " renderers");
        taskQueueUnsorted.getAndSet(new PendingTaskUpdate(new ArrayList<>(toUpdateList), updateLimit));
    }

    private void removeCancelledResults() {
        for (Iterator<WorldRenderer> it = finishedTasks.iterator(); it.hasNext(); ) {
            WorldRenderer wr = it.next();
            UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
            if (task.cancelled) {
                // Discard results and allow re-schedule on worker thread.
                if (AGGRESSIVE_NEODYMIUM_THREADING) {
                    NeodymiumCompat.safeDiscardTask(task);
                }
                task.clear();
                it.remove();
            }
        }
    }

    @Override
    public void onDirtyRendererChanged(WorldRenderer wr) {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        if (!task.isEmpty()) {
            //Do not discard useful work; just re-bake after rendering.
            ((WorldRendererOcclusion)wr).ft$needsRebake(true);
        }
    }

    public void onWorldRendererDirty(WorldRenderer wr) {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        if (!task.isEmpty()) {
            DebugLogging.debugLog(() -> "Renderer " + wr + " is dirty, cancelling task");
            task.cancelled = true;
        }
    }

    public static boolean renderOffThread(Block block, int renderType) {
        boolean isVanilla = renderType < 42 && renderType != 22;
        return isVanilla || ThreadedBlockSafetyRegistry.canBlockRenderOffThread(block);
    }

    /**
     * Renders certain blocks (as defined in canBlockBeRenderedOffThread) on the worker thread, and saves the
     * tessellation result. WorldRenderer#updateRenderer will skip over these blocks, and use the result that was
     * produced by the worker thread to fill them in.
     */
    public void doChunkUpdate(WorldRenderer wr, Profiler profiler) {
        DebugLogging.debugLog(() -> "Starting update of renderer " + wr);

        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        if (AGGRESSIVE_NEODYMIUM_THREADING) {
            NeodymiumCompat.beginThreadedPass(wr, false);
        }

        ChunkCache chunkcache = task.chunkCache;

        Tessellator tess = threadTessellator.get();

        val wro = ((WorldRendererOcclusion) wr);

        boolean pass1Only = !wr.needsUpdate && wro.ft$needsSort();
        if (pass1Only && wr.vertexState != null) {
            profiler.startSection("sort");
            try {
                if (AGGRESSIVE_NEODYMIUM_THREADING) {
                    NeodymiumCompat.beginRenderPass(task, wr, 1);
                }
                tess.setTranslation(-wr.posX, -wr.posY, -wr.posZ);
                tess.setVertexState(wr.vertexState);
                wr.vertexState = tess.getVertexState((float) playerX, (float) playerY, (float) playerZ);
                if (task.cancelled) {
                    ((ICapturableTessellator) tess).discard();
                    if (AGGRESSIVE_NEODYMIUM_THREADING) {
                        NeodymiumCompat.cancelTask(tess, wr);
                    }
                    return;
                }
                if (AGGRESSIVE_NEODYMIUM_THREADING) {
                    tess.setVertexState(wr.vertexState);
                    NeodymiumCompat.beginCapturing(tess, wr, task, 1);
                }
                return;
            } finally {
                profiler.endSection();
            }
        }
        wr.needsUpdate = true;
        wro.ft$needsSort(false);
        profiler.startSection("checkEmpty");
        try {
            if (chunkcache != null && !chunkcache.extendedLevelsInChunkCache()) {
                profiler.endStartSection("renderStart");
                OptiFineCompat.renderStart(chunkcache);
                profiler.endStartSection("initRenderBlocks");
                RenderBlocks renderblocks = new RenderBlocks(chunkcache);

                for (int pass = 0; pass < 2; pass++) {
                    profiler.endStartSection("pass" + pass);
                    ((ICapturableTessellator) tess).discard();
                    ThreadedClientHooks.threadRenderPass.set(pass);
                    profiler.startSection("doChunkUpdate");
                    int flags;
                    try {
                        flags = doChunkUpdateForRenderPass(wr, task, chunkcache, tess, pass, renderblocks);
                    } finally {
                        profiler.endSection();
                    }
                    if (task.cancelled) {
                        ((ICapturableTessellator) tess).discard();
                        if (AGGRESSIVE_NEODYMIUM_THREADING) {
                            NeodymiumCompat.cancelTask(tess, wr);
                        }
                        break;
                    }

                    boolean nextPass = hasFlag(flags, BIT_NextPass);
                    boolean renderedSomething = hasFlag(flags, BIT_RenderedSomething);
                    boolean startedTessellator = hasFlag(flags, BIT_StartedTessellator);

                    if (startedTessellator) {
                        if (AGGRESSIVE_NEODYMIUM_THREADING) {
                            if (pass == 1) {
                                //Sort vertices
                                val verts = tess.getVertexState((float) playerX, (float) playerY, (float) playerZ);
                                if (verts != null) {
                                    wr.vertexState = verts;
                                    tess.setVertexState(verts);
                                }
                            }
                            NeodymiumCompat.beginCapturing(tess, wr, task, pass);
                        } else {
                            TesselatorVertexState vertexState;
                            if (pass == 1) {
                                //Sort vertices
                                profiler.startSection("sort");
                                try {
                                    vertexState = tess.getVertexState((float) playerX, (float) playerY, (float) playerZ);
                                } finally {
                                    profiler.endSection();
                                }
                            } else {
                                vertexState = ((ICapturableTessellator) tess).arch$getUnsortedVertexState();
                            }
                            task.result[pass].renderedQuads(vertexState);
                            ((ICapturableTessellator) tess).discard();
                        }
                        if (ModuleConfig.TRIANGULATOR()) {
                            ToggleableTessellatorManager.postRenderBlocks(pass);
                        }
                    }
                    task.result[pass].renderedSomething = renderedSomething;
                    task.result[pass].startedTessellator = startedTessellator;
                    task.result[pass].nextPass = nextPass;
                }
                ThreadedClientHooks.threadRenderPass.set(-1);
                profiler.endStartSection("renderFinish");
                OptiFineCompat.renderFinish(chunkcache);
            }
        } finally {
            profiler.endSection();
        }
        DebugLogging.debugLog(() -> "Result of updating " + wr + ": " + worldRendererUpdateTaskToString(wr));
    }

    private ChunkCache getChunkCacheSnapshot(WorldRenderer wr) {
        // TODO This is not thread-safe! Actually make a snapshot here.
        byte pad = 1;
        ChunkCache chunkcache =
                OptiFineCompat.createChunkCache(wr.worldObj, wr.posX - pad, wr.posY - pad, wr.posZ - pad, wr.posX + 16 + pad, wr.posY + 16 + pad, wr.posZ + 16 + pad, pad);
        return chunkcache;
    }

    public void clear() {
        // TODO: destroy state when chunks are reloaded or server is stopped
    }

    public static Tessellator mainThreadTessellator() {
        return Tessellator.instance;
    }

    public Tessellator getThreadTessellator() {
        if (isMainThread()) {
            return mainThreadTessellator();
        } else {
            return threadTessellator.get();
        }
    }

    public static class GTNHLibCompat extends ThreadedChunkUpdateHelper {
        @Override
        public Tessellator getThreadTessellator() {
            if (CapturingTesselator.isCapturing()) {
                return CapturingTesselator.getThreadTesselator();
            }
            return super.getThreadTessellator();
        }
    }

    @RequiredArgsConstructor
    private static class PendingTaskUpdate {
        public final List<WorldRenderer> tasks;
        public final int updateLimit;
    }

    public static class UpdateTask {
        public boolean started;
        public boolean cancelled;
        public volatile boolean inFinishedQueue;
        public Result[] result = new Result[]{new Result(), new Result()};
        public List<TileEntity> TESRs = new ArrayList<>();

        public ChunkCache chunkCache;

        public boolean isEmpty() {
            return !started;
        }

        public void clear() {
            started = false;
            chunkCache = null;
            for (Result r : result) {
                r.clear();
            }
            cancelled = false;
            inFinishedQueue = false;
            TESRs.clear();
        }

        public static class Result {
            public boolean nextPass;
            public boolean startedTessellator;
            public boolean renderedSomething;
            public Object resultData;
            public Throwable written;

            public void clear() {
                renderedSomething = false;
                nextPass = false;
                startedTessellator = false;
                if (AGGRESSIVE_NEODYMIUM_THREADING && ThreadingConfig.EXTRA_DEBUG_INFO && resultData != null) {
                    new Throwable(written).printStackTrace();
                    written = null;
                }
                resultData = null;
            }

            public TesselatorVertexState renderedQuads() {
                return (TesselatorVertexState) resultData;
            }

            public void renderedQuads(TesselatorVertexState quads) {
                resultData = quads;
            }
        }
    }

    private class WorkSorterThread implements ThreadedTask {
        private final AtomicBoolean myRun;
        private final WRComparator comp = new WRComparator(true);
        private final InterruptableSorter<WorldRenderer> sorter = new InterruptableSorter<>(comp);
        private final AtomicReference<PendingTaskUpdate> taskQueueUnsorted;
        private final CircularTaskQueue taskQueue;

        public WorkSorterThread(AtomicBoolean run, AtomicReference<PendingTaskUpdate> taskQueueUnsorted, CircularTaskQueue taskQueue) {
            myRun = run;
            this.taskQueueUnsorted = taskQueueUnsorted;
            this.taskQueue = taskQueue;
        }

        @Override
        public boolean alive() {
            return myRun.get();
        }

        @Override
        public boolean lazy() {
            return false;
        }

        @Override
        public boolean doWork(Profiler profiler) {
            if (Debug.ENABLED && !Debug.chunkRebaking) {
                return false;
            }
            if (!myRun.get())
                return false;
            val pending = taskQueueUnsorted.getAndSet(null);
            if (pending == null) {
                return false;
            }
            val view = Minecraft.getMinecraft().renderViewEntity;
            if (view == null) {
                return false;
            }
            comp.posX = (float) view.posX;
            comp.posY = (float) view.posY;
            comp.posZ = (float) view.posZ;
            val tasks = pending.tasks;
            profiler.startSection("work_sort");
            try {
                try {
                    sorter.interruptableSort(tasks);
                } catch (InterruptedException ignored) {
                }

                profiler.endStartSection("work_updateQueue");
                taskQueue.clear();
                for (int i = 0, updated = 0; updated < pending.updateLimit && i < tasks.size(); i++) {
                    WorldRenderer wr = tasks.get(i);
                    updated++;
                    UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();

                    if (task.isEmpty()) {
                        // No update in progress; add to task queue
                        DebugLogging.debugLog(() -> "Adding " + wr + " to task queue");
                        try {
                            task.chunkCache = getChunkCacheSnapshot(wr);
                        } catch (Exception ignored) {
                            updated--;
                            continue;
                        }

                        taskQueue.add(wr);
                    }
                }
                return true;
            } finally {
                profiler.endSection();
            }
        }
    }

    public static <T> T kChunkCache(World world, int x1, int y1, int z1, int x2, int y2, int z2, int extent, Operation<T> original) {
        return AGGRESSIVE_NEODYMIUM_THREADING ? original.call(world, x1, y1, z1, x2 + 16, y2 + 16, z2 + 16, extent) : original.call(world, x1, y1, z1, x2, y2, z2, extent);
    }


    private void runTask(WorldRenderer wr, Profiler profiler) {
        profiler.startSection("update");
        try {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        task.started = true;
        try {
            doChunkUpdate(wr, profiler);
        } catch (Exception e) {
            ThreadedClientHooks.threadRenderPass.set(-1);
            Share.log.debug("Failed to update chunk " + wr, e);
            if (AGGRESSIVE_NEODYMIUM_THREADING) {
                NeodymiumCompat.safeDiscardTask(task);
            }
            task.clear();
        }
        ((ICapturableTessellator) threadTessellator.get()).discard();
        finishedTasks.add(wr);
        task.inFinishedQueue = true;
        } finally {
            profiler.endSection();
        }
    }

    private class WorkerThread extends FastThreadLocal.TurboThread {
        private final AtomicBoolean myRun;
        private final CircularTaskQueue taskQueue;
        private final Profiler profiler;
        public WorkerThread(String name, AtomicBoolean run, CircularTaskQueue taskQueue) {
            super(name);
            this.myRun = run;
            this.taskQueue = taskQueue;
            this.profiler = new Profiler();
            profiler.getProfilingData("__MEGATRACE__:wr_");
        }

        @Override
        public void run() {
            super.onStartup();
            try {
                while (myRun.get()) {
                    if (Debug.ENABLED && !Debug.chunkRebaking) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            if (!myRun.get())
                                return;
                        }
                        continue;
                    }
                    WorldRenderer wr = taskQueue.tryTake();
                    if (wr == null) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {
                            if (!myRun.get())
                                return;
                        }
                        continue;
                    }
                    runTask(wr, profiler);
                }
            } finally {
                super.onShutdown();
            }
        }
    }
}
