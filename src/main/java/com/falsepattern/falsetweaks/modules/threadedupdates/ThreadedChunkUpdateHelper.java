/*
 * This file is part of FalseTweaks.
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

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.occlusion.IRenderGlobalListener;
import com.falsepattern.falsetweaks.modules.occlusion.IRendererUpdateOrderProvider;
import com.falsepattern.falsetweaks.modules.occlusion.IWorldRenderer;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionWorker;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadedChunkUpdateHelper implements IRenderGlobalListener {

    public static ThreadedChunkUpdateHelper instance;

    public static Thread MAIN_THREAD;

    private static final boolean DEBUG_THREADED_UPDATE_FINE_LOG =
            Boolean.parseBoolean(System.getProperty(Tags.MODID + ".debug.enableThreadedUpdateFineLog"));

    /**
     * Used within the scope of WorldRenderer#updateRenderer (on the main thread).
     */
    public static WorldRenderer lastWorldRenderer;

    public static final RenderBlocksStack renderBlocksStack = new RenderBlocksStack();

    /**
     * Tasks not yet started
     */
    public BlockingQueue<WorldRenderer> taskQueue = new LinkedBlockingDeque<>();
    /**
     * Finished tasks ready for consumption
     */
    public BlockingDeque<WorldRenderer> finishedTasks = new LinkedBlockingDeque<>();

    /**
     * Tasks that should be completed immediately on the main thread
     */
    public Queue<WorldRenderer> urgentTaskQueue = new ArrayDeque<>();

    public ThreadLocal<Tessellator> threadTessellator = ThreadLocal.withInitial(Tessellator::new);

    IRendererUpdateOrderProvider rendererUpdateOrderProvider = new IRendererUpdateOrderProvider() {
        /** The renderers updated during the batch */
        private List<WorldRenderer> updatedRenderers = new ArrayList<>();

        private WorldRenderer nextRenderer;

        @Override
        public void prepare(List<WorldRenderer> worldRenderersToUpdateList, int updateLimit) {
            preRendererUpdates(worldRenderersToUpdateList, updateLimit);
        }

        @Override
        public boolean hasNext(List<WorldRenderer> worldRenderersToUpdateList) {
            WorldRenderer wr;

            if (!urgentTaskQueue.isEmpty()) {
                nextRenderer = urgentTaskQueue.poll();
                UpdateTask task = ((IRendererUpdateResultHolder) nextRenderer).ft$getRendererUpdateTask();
                task.cancelled = true;
                return true;
            }

            while ((wr = finishedTasks.poll()) != null) {
                UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
                if (task.cancelled || !wr.needsUpdate) {
                    task.clear();
                } else {
                    nextRenderer = wr;
                    return true;
                }
            }
            return false;
        }

        @Override
        public WorldRenderer next(List<WorldRenderer> worldRenderersToUpdateList) {
            Preconditions.checkNotNull(nextRenderer);
            WorldRenderer wr = nextRenderer;
            nextRenderer = null;
            updatedRenderers.add(wr);

            debugLog("Consuming renderer " + worldRendererToString(wr) + " " + worldRendererUpdateTaskToString(wr));

            return wr;
        }

        @Override
        public void cleanup(List<WorldRenderer> worldRenderersToUpdateList) {
            for (WorldRenderer wr : updatedRenderers) {
                worldRenderersToUpdateList.remove(wr);
                ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask().clear();
            }
            updatedRenderers.clear();
            urgentTaskQueue.clear();
            nextRenderer = null;
        }
    };

    public void init() {
        OcclusionHelpers.renderer.ft$setRendererUpdateOrderProvider(rendererUpdateOrderProvider);
        OcclusionHelpers.renderer.ft$addRenderGlobalListener(this);
        MAIN_THREAD = Thread.currentThread();
        FMLCommonHandler.instance().bus().register(this);
        spawnThreads();
    }

    private AtomicBoolean run = null;
    private Thread[] currentThreads = null;
    private int threadCount = 0;

    private void spawnThreads() {
        int threads = ThreadingConfig.CHUNK_UPDATE_THREADS;

        if (threads == 0)
            threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

        if (threadCount == threads) {
            return;
        }
        if (run != null)
            run.set(false);
        if (currentThreads != null)
            for (val thread: currentThreads)
                thread.interrupt();
        Share.log.info("Creating " + threads + " chunk builder" + (threads > 1 ? "s" : ""));
        String nameBase = "Chunk Update Worker Thread #";
        if (Loader.isModLoaded("lumina")) {
            nameBase = "$LUMI_NO_RELIGHT" + nameBase;
        }
        run = new AtomicBoolean(true);
        currentThreads = new Thread[threads];
        threadCount = threads;
        for (int i = 0; i < threads; i++) {
            val t = new Thread(() -> this.runThread(run), nameBase + i);
            currentThreads[i] = t;
            t.setDaemon(true);
            t.start();
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (!e.modID.equals(Tags.MODID))
            return;
        spawnThreads();
    }

    private void preRendererUpdates(List<WorldRenderer> toUpdateList, int updateLimit) {
        updateWorkQueue(toUpdateList, updateLimit);
        removeCancelledResults();
    }

    private void updateWorkQueue(List<WorldRenderer> toUpdateList, int updateLimit) {
        final int updateQueueSize = Math.min(updateLimit, threadCount * ThreadingConfig.UPDATE_QUEUE_SIZE_PER_THREAD);
        taskQueue.clear();
        main:
        for (int i = 0, updated = 0; updated < updateQueueSize && i < toUpdateList.size(); i++) {
            WorldRenderer wr = toUpdateList.get(i);
            val ci = ((IWorldRenderer)wr).ft$getCullInfo();
            if (ci.visGraph == null || ci.visGraph == OcclusionWorker.DUMMY)
                continue;
            for (val neighbor: ci.neighbors) {
                if (neighbor == null || neighbor.visGraph == null || neighbor.visGraph == OcclusionWorker.DUMMY)
                    continue main;
            }
            updated++;
            UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();

            if (wr.distanceToEntitySquared(Minecraft.getMinecraft().renderViewEntity) < 16 * 16) {
                if (!ThreadingConfig.DISABLE_BLOCKING_CHUNK_UPDATES) {
                    urgentTaskQueue.add(wr);
                } else {
                    task.important = true;
                }
            }

            if (task.isEmpty()) {
                // No update in progress; add to task queue
                debugLog("Adding " + worldRendererToString(wr) + " to task queue");
                task.chunkCache = getChunkCacheSnapshot(wr);
                taskQueue.add(wr);
            }
        }
    }

    private void removeCancelledResults() {
        for (Iterator<WorldRenderer> it = finishedTasks.iterator(); it.hasNext(); ) {
            WorldRenderer wr = it.next();
            UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
            if (task.cancelled) {
                // Discard results and allow re-schedule on worker thread.
                task.clear();
                it.remove();
            }
        }
    }

    @Override
    public void onDirtyRendererChanged(WorldRenderer wr) {
        onWorldRendererDirty(wr);
    }

    public void onWorldRendererDirty(WorldRenderer wr) {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        if (!task.isEmpty()) {
            debugLog("Renderer " + worldRendererToString(wr) + " is dirty, cancelling task");
            task.cancelled = true;
        }
    }

    @SneakyThrows
    private void runThread(AtomicBoolean run) {
        while (run.get()) {
            try {
                WorldRenderer wr = taskQueue.take();
                UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
                task.started = true;
                try {
                    doChunkUpdate(wr);
                } catch (Exception e) {
                    Share.log.error("Failed to update chunk " + worldRendererToString(wr), e);
                    for (UpdateTask.Result r : task.result) {
                        r.clear();
                    }
                    ((ICapturableTessellator) threadTessellator.get()).discard();
                }
                if (!task.important) {
                    finishedTasks.add(wr);
                } else {
                    finishedTasks.addFirst(wr);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Renders certain blocks (as defined in canBlockBeRenderedOffThread) on the worker thread, and saves the
     * tessellation result. WorldRenderer#updateRenderer will skip over these blocks, and use the result that was
     * produced by the worker thread to fill them in.
     */
    public void doChunkUpdate(WorldRenderer wr) {
        debugLog("Starting update of renderer " + worldRendererToString(wr));

        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();

        ChunkCache chunkcache = task.chunkCache;

        Tessellator tess = threadTessellator.get();

        if (chunkcache != null && !chunkcache.extendedLevelsInChunkCache()) {
            RenderBlocks renderblocks = new RenderBlocks(chunkcache);

            for (int pass = 0; pass < 2; pass++) {
                boolean renderedSomething = false;
                boolean startedTessellator = false;

                BlockLoop:
                for (int y = wr.posY; y < wr.posY + 16; ++y) {
                    for (int z = wr.posZ; z < wr.posZ + 16; ++z) {
                        for (int x = wr.posX; x < wr.posX + 16; ++x) {
                            if (task.cancelled) {
                                debugLog("Realized renderer " + worldRendererToString(wr) + " is dirty, aborting update");
                                break BlockLoop;
                            }

                            Block block = chunkcache.getBlock(x, y, z);

                            if (block.getMaterial() != Material.air) {
                                if (!startedTessellator) {
                                    startedTessellator = true;
                                    tess.startDrawingQuads();
                                    tess.setTranslation(-wr.posX, -wr.posY, -wr.posZ);
                                }

                                int k3 = block.getRenderBlockPass();

                                if (!block.canRenderInPass(pass)) {
                                    continue;
                                }

                                renderedSomething |= renderblocks.renderBlockByRenderType(block, x, y, z);
                            }
                        }
                    }
                }

                if (startedTessellator) {
                    task.result[pass].renderedQuads = ((ICapturableTessellator) tess).arch$getUnsortedVertexState();
                    ((ICapturableTessellator) tess).discard();
                }
                task.result[pass].renderedSomething = renderedSomething;
            }
        }
        debugLog("Result of updating " + worldRendererToString(wr) + ": " + worldRendererUpdateTaskToString(wr));
    }

    public static boolean canBlockBeRenderedOffThread(Block block, int pass, int renderType) {
        return renderType < 42 && renderType != 22; // vanilla block
    }

    private ChunkCache getChunkCacheSnapshot(WorldRenderer wr) {
        // TODO This is not thread-safe! Actually make a snapshot here.
        byte pad = 1;
        ChunkCache chunkcache = new ChunkCache(wr.worldObj, wr.posX - pad, wr.posY - pad, wr.posZ - pad,
                wr.posX + 16 + pad, wr.posY + 16 + pad, wr.posZ + 16 + pad, pad);
        return chunkcache;
    }

    public void clear() {
        // TODO: destroy state when chunks are reloaded or server is stopped
    }

    public Tessellator getThreadTessellator() {
        if (Thread.currentThread() == MAIN_THREAD) {
            return Tessellator.instance;
        } else {
            return threadTessellator.get();
        }
    }

    private static String worldRendererToString(WorldRenderer wr) {
        return wr + "(" + wr.posX + ", " + wr.posY + ", " + wr.posZ + ")";
    }

    private static String worldRendererUpdateTaskToString(WorldRenderer wr) {
        UpdateTask task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        return task.result[0].renderedSomething + " (" + (task.result[0].renderedQuads == null ? "null" : task.result[0].renderedQuads.getVertexCount()) + ")/" + task.result[1].renderedSomething + " (" + (task.result[1].renderedQuads == null ? "null" : task.result[1].renderedQuads.getVertexCount()) + ")";
    }

    private static void debugLog(String msg) {
        if (DEBUG_THREADED_UPDATE_FINE_LOG) {
            Share.log.trace(msg);
        }
    }

    public static class UpdateTask {
        public boolean started;
        public boolean cancelled;
        public boolean important;
        public Result[] result = new Result[]{new Result(), new Result()};

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
            important = false;
        }

        public static class Result {
            public boolean renderedSomething;
            public TesselatorVertexState renderedQuads;

            public void clear() {
                renderedSomething = false;
                renderedQuads = null;
            }
        }
    }
}
