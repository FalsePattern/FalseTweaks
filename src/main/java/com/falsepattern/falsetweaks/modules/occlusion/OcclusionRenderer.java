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

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.api.dynlights.FTDynamicLights;
import com.falsepattern.falsetweaks.config.OcclusionConfig;
import com.falsepattern.falsetweaks.modules.debug.Debug;
import com.falsepattern.falsetweaks.modules.debug.DebugLogging;
import com.falsepattern.falsetweaks.modules.occlusion.interfaces.IRenderGlobalMixin;
import com.falsepattern.falsetweaks.modules.occlusion.shader.ShadowPassOcclusionHelper;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import com.falsepattern.falsetweaks.modules.threadexec.FTWorker;
import com.falsepattern.falsetweaks.modules.threadexec.ThreadedTask;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.AGGRESSIVE_NEODYMIUM_THREADING;

public class OcclusionRenderer {

    public static final WRComparator rendererSorter = new WRComparator(true);
    private static final long NANOS_PER_SEC = 1_000_000_000;
    private static final FakeCamera fakeFrustum = new FakeCamera();
    public static double playerX = 0;
    public static double playerY = 0;
    public static double playerZ = 0;
    public boolean occlusionRecheckMain = false;
    public boolean occlusionRecheckShadow = false;
    private final Minecraft mc;
    private final RenderGlobal rg;
    private final SpatialRenderStore renderStore;
    int alphaSortProgress = 0;
    private Thread clientThread;
    private List<WorldRenderer> worldRenderersToUpdateList;
    private int updateListModificationCounter = 0;
    private double prevRenderX, prevRenderY, prevRenderZ;
    private int cameraStaticTime;
    private int renderersNeedUpdate;
    private IRendererUpdateOrderProvider rendererUpdateOrderProvider;
    private List<IRenderGlobalListener> eventListeners;
    /* Make sure other threads can see changes to this */
    private volatile boolean deferNewRenderUpdates;
    private long lastUpdateTime = System.nanoTime();
    private long[] frameTimeRollingAvg = new long[128];
    private int frameTimeRollingAvgNext = 0;
    private int occlusionRecheckCounterRender;
    private int occlusionRecheckCounterShadow;
    private volatile WorldRenderer[] renderersToClip = null;
    private volatile ICamera clipCamera = null;
    private ClipThread clipThread;

    private RecheckThread recheckThread;

    private final QueryStartCallbackRender queryStartCallbackRender = new QueryStartCallbackRender();
    private final QueryStartCallbackShadow queryStartCallbackShadow = new QueryStartCallbackShadow();
    private OcclusionQueryManager occlusionQueryManager = null;

    public static class RecheckThread implements ThreadedTask {
        private final RenderGlobal rg;

        private int counter = 0;

        public RecheckThread(RenderGlobal rg) {
            this.rg = rg;
        }

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
            val renderers = rg.worldRenderers;
            if (renderers == null) {
                return false;
            }
            val world = rg.theWorld;
            if (world == null) {
                return false;
            }
            profiler.startSection("check");
            try {
                for (int i = 0; i < renderers.length; ++i) {
                    val mod15 = (i + counter & 15) == 0;
                    if (!mod15)
                        continue;
                    val wr = renderers[i];
                    if (wr == null)
                        continue;
                    val wro = ((WorldRendererOcclusion) wr);
                    val posX = wr.posX;
                    val posZ = wr.posZ;
                    boolean isNonEmpty = isChunkPresent(world, posX, posZ);
                    int expectedNeighbors = expectedNeighbors();
                    int neighbors = countNeighbors(world, posX, posZ);
                    wro.ft$updateNeighborCheckState(isNonEmpty, expectedNeighbors, neighbors, posX, posZ);
                }
                ++counter;
                return true;
            } finally {
                profiler.endSection();
            }
        }

        private static final EnumFacing[] facings = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
        private int expectedNeighbors() {
            return facings.length;
        }

        private int countNeighbors(World world, int posX, int posZ) {
            int count = 0;
            for (EnumFacing dir : facings) {
                if (isChunkPresent(world, posX + dir.getFrontOffsetX() * 16, posZ + dir.getFrontOffsetZ() * 16)) {
                    count++;
                }
            }
            return count;
        }

        private boolean isChunkPresent(World world, int posX, int posZ) {
            val chunk = world.getChunkFromBlockCoords(posX, posZ);
            return chunk != null && !(chunk instanceof EmptyChunk);
        }
    }

    public OcclusionRenderer(RenderGlobal renderGlobal) {
        this.rg = renderGlobal;
        this.mc = renderGlobal.mc;
        this.renderStore = new SpatialRenderStore(this);
        recheckThread = new RecheckThread(rg);
        FTWorker.addTask(recheckThread);
    }

    private static int fixPos(int pos, int amt) {
        int r = Math.floorDiv(pos, 16) % amt;
        if (r < 0) {
            r += amt;
        }
        return r;
    }

    private static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2);
    }

    public RenderGlobal getRenderGlobal() {
        return rg;
    }

    /**
     * If the update list is not queued for a full resort (e.g. when the player moves or renderers have their positions
     * changed), uses binary search to add the renderer in the update queue at the appropriate place. Otherwise,
     * the renderer is just added to the end of the list.
     *
     * @param wr renderer to add to the list
     */
    private void addRendererToUpdateQueue(WorldRenderer wr) {
        if (!((WorldRendererOcclusion)wr).ft$hasAllNeighbors())
            return;
        updateRendererPriorityInQueue(wr);
    }

    private void updateRendererPriorityInQueue(WorldRenderer wr) {
        val iwr = ((WorldRendererOcclusion) wr);
        val priority = determinePriority(wr);
        val isInUpdateList = iwr.ft$isInUpdateList();

        iwr.ft$currentPriority(priority);
        if (isInUpdateList) {
            return;
        }
        DebugLogging.debugLog(() -> "Adding " + wr + " to priority queue");

        worldRenderersToUpdateList.add(wr);
        updateListModificationCounter++;
        iwr.ft$setInUpdateList(true);
    }

    /**
     * This only determines the raw priority of the given world renderer, it doesn't actually sort it inside the given
     * priority. That part is done during the worker dispatch.
     * <p>
     * Priorities:
     * 0 - Chunks modified by the player triggered by block place or break events.
     * 1 - 5x5 chunk cube around the player.
     * 2 - Anything within the player frustum.
     * 3 - The rest
     */
    public int determinePriority(WorldRenderer wr) {
        val iwr = ((WorldRendererOcclusion) wr);
        if (iwr.ft$isInUpdateList() && iwr.ft$currentPriority() == 0) {
            return 0;
        }

        val mousePos = mc.objectMouseOver;
        if (mousePos != null && mousePos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            val mouseX = mousePos.blockX;
            val mouseY = mousePos.blockY;
            val mouseZ = mousePos.blockZ;
            val posX = wr.posX;
            val posY = wr.posY;
            val posZ = wr.posZ;
            if (mouseX >= posX && mouseX < posX + 16 &&
                mouseY >= posY && mouseY < posY + 16 &&
                mouseZ >= posZ && mouseZ < posZ + 16) {
                return 0;
            }
        }



        if (wr.isInFrustum) {
            val viewEntity = mc.renderViewEntity;
            if (viewEntity != null) {
                val dist = 2 * 16;
                double dX = Math.abs(viewEntity.posX - wr.posXPlus);
                double dY = Math.abs(viewEntity.posY - wr.posYPlus);
                double dZ = Math.abs(viewEntity.posZ - wr.posZPlus);
                double d = Math.max(dX, Math.max(dY, dZ));
                if (d < dist) {
                    if (!wr.isInitialized)
                        return 5;
                    return 10;
                }
            }
            if (!wr.isInitialized)
                return 15;
            return 20;
        }

        if (!wr.isInitialized)
            return 25;

        return 30;
    }

    public void handleOffthreadUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        if (deferNewRenderUpdates || Thread.currentThread() != clientThread) {
            OcclusionHelpers.updateArea(x1, y1, z1, x2, y2, z2);
        } else {
            internalMarkBlockUpdate(x1, y1, z1, x2, y2, z2);
        }
    }

    public void internalMarkBlockUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        DebugLogging.debugLog(() -> String.format("Marking Area from[x=%d,y=%d,z=%d] to[x=%d,y=%d,z=%d] for update", x1, y1, z1, x2, y2, z2));
        int xStart = MathHelper.bucketInt(x1, 16);
        int yStart = MathHelper.bucketInt(y1, 16);
        int zStart = MathHelper.bucketInt(z1, 16);
        int xEnd = MathHelper.bucketInt(x2, 16);
        int yEnd = MathHelper.bucketInt(y2, 16);
        int zEnd = MathHelper.bucketInt(z2, 16);

        final int width = rg.renderChunksWide;
        final int height = rg.renderChunksTall;
        final int depth = rg.renderChunksDeep;
        final WorldRenderer[] worldRenderers = rg.worldRenderers;

        for (int i = xStart; i <= xEnd; ++i) {
            int x = i % width;
            x += width & (x >> 31);

            for (int j = yStart; j <= yEnd; ++j) {
                int y = j % height;
                y += height & (y >> 31);

                for (int k = zStart; k <= zEnd; ++k) {
                    int z = k % depth;
                    z += depth & (z >> 31);

                    int k4 = (z * height + y) * width + x;
                    val wr = worldRenderers[k4];
                    val wro = (WorldRendererOcclusion) wr;

                    val needsUpdate = wr.needsUpdate;
                    val isVisible = wr.isVisible;
                    val isInitialized = wr.isInitialized;
                    val isInUpdateList = wro.ft$isInUpdateList();
                    val isNonEmpty = wro.ft$isNonEmptyChunk();

                    if (!needsUpdate || (isVisible && !isInUpdateList)) {
                        wr.markDirty();
                        OcclusionCompat.DragonAPICompat.ChangePacketRenderer$onChunkRerender(x1, y1, z1, x2, y2, z2, wr);
                        OcclusionHelpers.worker.dirty = true;
                    } else {
                        int size = eventListeners.size();
                        for (int m = -1; ++m < size; ) {
                            eventListeners.get(m).onDirtyRendererChanged(wr);
                        }
                    }

                    if ((needsUpdate || !isInitialized) && isNonEmpty) {
                        addRendererToUpdateQueue(wr);
                    }
                }
            }
        }
    }

    public boolean skipRenderingIfNotVisible(RenderManager instance, Entity entity, float tick) {
        WorldRenderer rend = getRenderer(entity.posX, entity.posY, entity.posZ);
        if (rend != null && !rend.isVisible) {
            --rg.countEntitiesRendered;
            ++rg.countEntitiesHidden;
            return false;
        }
        return RenderManager.instance.renderEntitySimple(entity, tick);
    }

    public String getDebugInfoRenders() {
        if (Compat.neodymiumActive()) {
            String r = "C: " + rg.renderersLoaded + '/' + rg.worldRenderers.length +
                       ", N: " + (worldRenderersToUpdateList.size() + renderersDispatched);
            return r;
        } else {
            String r = "C: " + rg.renderersBeingRendered + '/' + rg.renderersLoaded + '/' + rg.worldRenderers.length +
                       ". F: " + rg.renderersBeingClipped +
                       ", O: " + rg.renderersBeingOccluded +
                       ", E: " + rg.renderersSkippingRenderPass +
                       ", I: " + rg.dummyRenderInt +
                       "; U: " + renderersNeedUpdate +
                       ", N: " + (rg.worldRenderersToUpdate.size() + renderersDispatched);
            return r;
        }
    }

    public void initBetterLists() {
        worldRenderersToUpdateList = new ArrayList<>();
        updateListModificationCounter++;
        /* Make sure any vanilla code modifying the update queue crashes */
        rg.worldRenderersToUpdate = Collections.unmodifiableList(worldRenderersToUpdateList);
        clientThread = Thread.currentThread();
        eventListeners = new ArrayList<>();
    }

    public void clearRendererUpdateQueue(List instance) {
        if (instance == rg.worldRenderersToUpdate) {
            for (val wr : worldRenderersToUpdateList) {
                ((WorldRendererOcclusion) wr).ft$setInUpdateList(false);
            }
            worldRenderersToUpdateList.clear();
            updateListModificationCounter++;
            if (cleanupTaskFromPreviousFrame != null) {
                try {
                    val taskList = cleanupTaskFromPreviousFrame.get();
                    for (val wr: taskList) {
                        ((WorldRendererOcclusion)wr).ft$setInUpdateList(false);
                    }
                    taskList.clear();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new AssertionError("Transformer applied to the wrong List.clear method");
        }
    }

    public WorldRenderer getRenderer(int x, int y, int z) {
        if ((y - 15) > rg.maxBlockY || y < rg.minBlockY || (x - 15) > rg.maxBlockX || x < rg.minBlockX || (z - 15) > rg.maxBlockZ || z < rg.minBlockZ) {
            return null;
        }

        x = fixPos(x, rg.renderChunksWide);
        y = fixPos(y, rg.renderChunksTall);
        z = fixPos(z, rg.renderChunksDeep);

        return rg.worldRenderers[(z * rg.renderChunksTall + y) * rg.renderChunksWide + x];
    }

    public WorldRenderer getRenderer(double x, double y, double z) {
        int X = MathHelper.floor_double(x);
        int Y = MathHelper.floor_double(y);
        int Z = MathHelper.floor_double(z);
        return getRenderer(X, Y, Z);
    }

    private Future<List<WorldRenderer>> cleanupTaskFromPreviousFrame;
    private int renderersDispatched = 0;

    private boolean rebuildChunks(EntityLivingBase view, boolean fastUpdate) {
        if (Debug.ENABLED && !Debug.chunkRebaking) {
            return false;
        }
        val prof = rg.theWorld.theProfiler;
        long currentFrameTime = System.nanoTime();
        long deltaFrameTime = currentFrameTime - lastUpdateTime;
        lastUpdateTime = currentFrameTime;
        frameTimeRollingAvg[frameTimeRollingAvgNext] = deltaFrameTime;
        frameTimeRollingAvgNext = (frameTimeRollingAvgNext + 1) % frameTimeRollingAvg.length;
        int updatesPerSec = OcclusionConfig.CHUNK_UPDATES_PER_SECOND * (fastUpdate ? 2 : 1);
        long frameTimeSum = 0;
        for (int i = 0; i < frameTimeRollingAvg.length; i++) {
            frameTimeSum += frameTimeRollingAvg[i];
        }
        double frameTimeAvg = (frameTimeSum / (double) NANOS_PER_SEC) / (double) frameTimeRollingAvg.length;
        int updateLimit = (int) (updatesPerSec * frameTimeAvg) + 1;
        int updates = 0;

        boolean spareTime = true;
        rendererSorter.posX = (float) view.posX;
        rendererSorter.posY = (float) view.posY;
        rendererSorter.posZ = (float) view.posZ;

        prof.startSection("cleanup_await");
        renderersDispatched = 0;
        if (cleanupTaskFromPreviousFrame != null) {
            try {
                renderersDispatched = cleanupTaskFromPreviousFrame.get().size();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            cleanupTaskFromPreviousFrame = null;
        }

        deferNewRenderUpdates = true;
        prof.endStartSection("prepare");
        renderersDispatched += worldRenderersToUpdateList.size();
        rendererUpdateOrderProvider.prepare(worldRenderersToUpdateList, updateLimit < 0 ? Integer.MAX_VALUE : 10 * updateLimit);

        prof.endStartSection("work");
        while ((updateLimit < 0 || updates < updateLimit) && rendererUpdateOrderProvider.hasNext()) {
            WorldRenderer worldrenderer = rendererUpdateOrderProvider.next();

            val wro = ((WorldRendererOcclusion) worldrenderer);
            val sortOnly = wro.ft$needsSort() && !worldrenderer.needsUpdate;

            wro.ft$setInUpdateList(false);
            wro.ft$needsSort(false);

            if (sortOnly) {
                worldrenderer.updateRendererSort(view);
            } else {
                if (AGGRESSIVE_NEODYMIUM_THREADING) {
                    val state = worldrenderer.vertexState;
                    worldrenderer.updateRenderer(view);
                    worldrenderer.vertexState = state;
                } else {
                    worldrenderer.updateRenderer(view);
                }

                wro.ft$skipRenderPass(worldrenderer.skipAllRenderPasses() || !wro.ft$isNonEmptyChunk());

                if (!wro.ft$skipRenderPass()) {
                    updates++;
                }
            }
            if (wro.ft$needsRebake()) {
                worldrenderer.markDirty();
                wro.ft$needsRebake(false);
            }
            //                if(!worldrenderer.isWaitingOnOcclusionQuery || deadline != 0 || OcclusionHelpers.DEBUG_LAZY_CHUNK_UPDATES) {
            //                    long t = System.nanoTime();
            //                    if (t > deadline) {
            //                        spareTime = false;
            //                        break;
            //                    }
            //                }
            //            }
        }
        prof.endStartSection("cleanup_start");
        cleanupTaskFromPreviousFrame = rendererUpdateOrderProvider.cleanup();
        prof.endSection();
        deferNewRenderUpdates = false;
        return spareTime;
    }

    public void performCullingUpdates(EntityLivingBase view, boolean p_72716_2_) {
        val prof = rg.theWorld.theProfiler;
        prof.startSection("deferred_updates");
        while (OcclusionHelpers.deferredAreas.size() > 0) {
            OcclusionHelpers.processUpdate(this);
        }
        prof.endStartSection("rebuild");

        CameraInfo cam = CameraInfo.getInstance();

        boolean cameraMoved = cam.getEyeX() != prevRenderX || cam.getEyeY() != prevRenderY || cam.getEyeZ() != prevRenderZ;

        prevRenderX = cam.getEyeX();
        prevRenderY = cam.getEyeY();
        prevRenderZ = cam.getEyeZ();

        boolean cameraRotated = PreviousActiveRenderInfo.changed();

        if (!cameraRotated && !cameraMoved) {
            cameraStaticTime++;
        } else {
            cameraStaticTime = 0;
        }


        boolean doUpdateAcceleration = cameraStaticTime > 2 &&
                                       !OcclusionHelpers.DEBUG_LAZY_CHUNK_UPDATES &&
                                       !OcclusionHelpers.DEBUG_NO_UPDATE_ACCELERATION &&
                                       OcclusionConfig.DYNAMIC_CHUNK_UPDATES;
        /* If the camera is not moving, assume a deadline of N FPS. */
        rebuildChunks(view, doUpdateAcceleration);

        prof.endStartSection("scan");
        int yaw = MathHelper.floor_float(view.rotationYaw + 45) >> 4;
        int pitch = MathHelper.floor_float(view.rotationPitch + 45) >> 4;
        if (OcclusionHelpers.worker.dirty || cameraRotated || OcclusionHelpers.DEBUG_ALWAYS_RUN_OCCLUSION) {
            // Clear the update queue, the graph search will repopulate it in the correct order
//            clearRendererUpdateQueue(rg.worldRenderersToUpdate);
            OcclusionHelpers.worker.run(true);
            PreviousActiveRenderInfo.update();
        }
        prof.endSection();
    }

    private int occlusionListRender = -1;
    private int occlusionListShadow = -1;


    public void resetLoadedRenderers() {
        if (rg.theWorld != null) {
            rg.renderersLoaded = 0;
        }
        buildOcclusionRenderListRender();
        buildOcclusionRenderListShadow();
    }

    private void buildOcclusionRenderListRender() {
        if (occlusionListRender == -1) {
            occlusionListRender = GL11.glGenLists(1);
        }
        float f = 6.0f;
        GL11.glNewList(occlusionListRender, GL11.GL_COMPILE);
        renderAABBTextured(AxisAlignedBB.getBoundingBox(-f, -f, -f, 16 + f, 16 + f, 16 + f));
        GL11.glEndList();
    }
    private void buildOcclusionRenderListShadow() {
        if (occlusionListShadow == -1) {
            occlusionListShadow = GL11.glGenLists(1);
        }
        GL11.glNewList(occlusionListShadow, GL11.GL_COMPILE);
        float f = 6.0f;
        renderAABBTextured(AxisAlignedBB.getBoundingBox(-f, -f, -f, 16 + f, 16 + f, 16 + f));
        GL11.glEndList();
    }

    private static void renderAABBTextured(AxisAlignedBB AABB) {
        Tessellator tessellator = ThreadedChunkUpdateHelper.mainThreadTessellator();
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1, 1, 1, 1);
        tessellator.setBrightness(240);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.minZ, 0, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.minZ, 1, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.minZ, 1, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.minZ, 0, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.maxZ, 0, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.maxZ, 1, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.maxZ, 1, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.maxZ, 0, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.minZ, 0, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.minZ, 1, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.maxZ, 1, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.maxZ, 0, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.maxZ, 0, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.maxZ, 1, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.minZ, 1, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.minZ, 0, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.maxZ, 0, 0);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.maxZ, 1, 0);
        tessellator.addVertexWithUV(AABB.minX, AABB.maxY, AABB.minZ, 1, 1);
        tessellator.addVertexWithUV(AABB.minX, AABB.minY, AABB.minZ, 0, 1);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.minZ, 0, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.minZ, 1, 0);
        tessellator.addVertexWithUV(AABB.maxX, AABB.maxY, AABB.maxZ, 1, 1);
        tessellator.addVertexWithUV(AABB.maxX, AABB.minY, AABB.maxZ, 0, 1);
        tessellator.draw();
    }

    public void resetOcclusionWorker() {
        renderStore.reset(rg);
        if (OcclusionHelpers.worker != null) {
            OcclusionHelpers.worker.reset();
        }
        if (cleanupTaskFromPreviousFrame != null) {
            cleanupTaskFromPreviousFrame.cancel(true);
            cleanupTaskFromPreviousFrame = null;
        }
        if (occlusionQueryManager != null) {
            occlusionQueryManager.close();
        }
        if (rg.worldRenderers != null) {
            occlusionQueryManager = new OcclusionQueryManager(1024);
        }
    }

    public void pushWorkerRenderer(WorldRenderer wr) {
        if (((WorldRendererOcclusion)wr).ft$isNonEmptyChunk()) {
            addRendererToUpdateQueue(wr);
        }
    }

    public void markRendererInvisible(WorldRenderer instance) {
        instance.isInFrustum = false;
        discardOcclusionCheckRender(instance);
        discardOcclusionCheckShadow(instance);
        ((WorldRendererOcclusion)instance).ft$waitingOnShadowOcclusionQuery(false);
        instance.markDirty();
    }

    public void setPositionAndMarkInvisible(WorldRenderer wr, int x, int y, int z) {
        wr.setPosition(x, y, z);
        if (((WorldRendererOcclusion) wr).ft$isInUpdateList()) {
            OcclusionHelpers.worker.dirty = true;
        }
        if (!wr.isInitialized) {
            ((WorldRendererOcclusion) wr).ft$skipRenderPass(false);
        }
    }

    public void runWorkerFull() {
        renderStore.updateRendererNeighborsFull(rg);
        OcclusionHelpers.worker.run(true);
    }

    public int sortAndRender(EntityLivingBase view, int pass, double tick) {
        playerX = view.posX;
        playerY = view.posY;
        playerZ = view.posZ;
        val dl = FTDynamicLights.frontend();
        if (dl.enabled()) {
            dl.update(rg);
        }
        CameraInfo cam = CameraInfo.getInstance();
        cam.update(view, tick);
        val prof = rg.theWorld.theProfiler;

        prof.startSection("sortchunks");

        if (this.mc.gameSettings.renderDistanceChunks != rg.renderDistanceChunks && !(this.mc.currentScreen instanceof GuiVideoSettings)) {
            rg.loadRenderers();
        }

        WorldRenderer[] sortedWorldRenderers = rg.sortedWorldRenderers;
        if (rg.renderersLoaded > 0) {
            int e = rg.renderersLoaded / 100 + 1;
            for (int j = 0; j < e; ++j) {
                rg.worldRenderersCheckIndex = (rg.worldRenderersCheckIndex + 1) % rg.renderersLoaded;
                WorldRenderer rend = sortedWorldRenderers[rg.worldRenderersCheckIndex];

                if ((rend.needsUpdate || !rend.isInitialized) & ((WorldRendererOcclusion)rend).ft$isNonEmptyChunk()) {
                    addRendererToUpdateQueue(rend);
                }
            }
        }

        prof.startSection("reposition_chunks");
        if (rg.prevChunkSortX != cam.getChunkCoordX() || rg.prevChunkSortY != cam.getChunkCoordY() || rg.prevChunkSortZ != cam.getChunkCoordZ()) {
            renderStore.repositionSmart(rg, cam);
            OcclusionHelpers.worker.dirty = true;
        }
        prof.endStartSection("alpha_sort");
        if (distanceSquared(cam.getX(), cam.getY(), cam.getZ(), rg.prevRenderSortX, rg.prevRenderSortY, rg.prevRenderSortZ) > 1) {
            rg.prevRenderSortX = cam.getX();
            rg.prevRenderSortY = cam.getY();
            rg.prevRenderSortZ = cam.getZ();
            alphaSortProgress = 0;
        }

        int amt = rg.renderersLoaded < 27 ? rg.renderersLoaded : Math.max(rg.renderersLoaded >>> 1, 27);
        if ((!Debug.ENABLED || Debug.translucencySorting) && alphaSortProgress < amt) {
            int amountPerFrame = 100;
            for (int i = 0; i < amountPerFrame && alphaSortProgress < amt; ) {
                WorldRenderer r = sortedWorldRenderers[alphaSortProgress++];
                if (!((WorldRendererOcclusion) r).ft$isInUpdateList() && !r.needsUpdate && !r.skipRenderPass[1]) {
                    ((WorldRendererOcclusion) r).ft$needsSort(true);
                    pushWorkerRenderer(r);

                    i++;
                }
            }
        }
        prof.endSection();

        prof.endStartSection("render");
        if (OcclusionCompat.OptiFineCompat.isOptiFineFogOff(this.mc.entityRenderer)) {
            GL11.glDisable(GL11.GL_FOG);
        }
        RenderHelper.disableStandardItemLighting();
        int k = rg.renderSortedRenderers(0, rg.renderersLoaded, pass, tick);
        ((IRenderGlobalMixin) rg).ft$setSortedRendererCount(rg.renderersLoaded);

        prof.endSection();
        return k;
    }

    private boolean prevOcclusionRunRender = true;
    private boolean prevOcclusionRunShadow = true;

    public void runOcclusionCheck(boolean shadow, int pass) {
        if (pass != 0)
            return;
        if (shadow) {
            if (!occlusionRecheckShadow) {
                return;
            }
            occlusionRecheckShadow = false;
        } else {
            if (!occlusionRecheckMain)
                return;
            occlusionRecheckMain = false;
        }
        if (shadow) {
            if (!Debug.ENABLED || Debug.shadowOcclusionChecks) {
                prevOcclusionRunShadow = true;
                runOcclusionCheckShadow(pass);
            } else {
                if (prevOcclusionRunShadow) {
                    for (int i = 0; i < rg.renderersLoaded; ++i) {
                        val wr = rg.sortedWorldRenderers[i];
                        discardOcclusionCheckShadow(wr);
                    }
                }
                prevOcclusionRunShadow = false;
            }
        } else {
            if (!Debug.ENABLED || Debug.occlusionChecks) {
                prevOcclusionRunRender = true;
                runOcclusionCheckRender(pass);
            } else {
                if (prevOcclusionRunRender) {
                    for (int i = 0; i < rg.renderersLoaded; ++i) {
                        val wr = rg.sortedWorldRenderers[i];
                        discardOcclusionCheckRender(wr);
                    }
                }
                prevOcclusionRunRender = false;
            }
        }
    }

    public void runOcclusionCheckShadow(int pass) {
        if (occlusionQueryManager == null)
            return;
        val prof = rg.theWorld.theProfiler;
        val cam = CameraInfo.getInstance();
        prof.startSection("occ_shadow");
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if (!Debug.ENABLED || Debug.shadowOcclusionMask) {
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
        }

        prof.startSection("schedule");

        GL11.glPushMatrix();
        double cx = 0;
        double cy = 0;
        double cz = 0;
        val eyeX = cam.getEyeX();
        val eyeY = cam.getEyeY();
        val eyeZ = cam.getEyeZ();
        GL11.glTranslated(-eyeX, -eyeY, -eyeZ);
        val FPS = TickTimeTracker.averageFPS();

        for (int i = 0; i < rg.renderersLoaded; i++) {
            val wr = rg.sortedWorldRenderers[i];
            val iwr = (WorldRendererOcclusion) wr;
            if (iwr.ft$waitingOnShadowOcclusionQuery())
                continue;
            int recheckInterval;
            if (FPS > 500) {
                recheckInterval = 128;
            } else if (FPS > 300) {
                recheckInterval = 64;
            } else if (FPS > 200) {
                recheckInterval = 32;
            } else if (FPS > 100) {
                recheckInterval = 16;
            } else {
                recheckInterval = 8;
            }
            val recheck = ((i + occlusionRecheckCounterShadow) % recheckInterval) == 0;
            if (!recheck) {
                continue;
            }

            if (!wr.skipAllRenderPasses()) {
                if (occlusionListShadow == -1) {
                    buildOcclusionRenderListShadow();
                }
                double x = wr.posX;
                double y = wr.posY;
                double z = wr.posZ;
                double dx = x - cx;
                double dy = y - cy;
                double dz = z - cz;
                if (dx != 0 || dy != 0 || dz != 0) {
                    GL11.glTranslated(dx, dy, dz);
                    cx += dx;
                    cy += dy;
                    cz += dz;
                }
                queryStartCallbackShadow.iwr = iwr;
                if (!occlusionQueryManager.dispatchNewOcclusionQuery(queryStartCallbackShadow)) {
                    occlusionQueryManager = occlusionQueryManager.grow(1024);
                    if (!occlusionQueryManager.dispatchNewOcclusionQuery(queryStartCallbackShadow))
                        break;
                }
            }
        }
        GL11.glPopMatrix();

        GL11.glPopAttrib();
        occlusionRecheckCounterShadow++;
        prof.endSection();
        prof.endSection();
    }

    public void runOcclusionCheckRender(int pass) {
        if (occlusionQueryManager == null)
            return;
        val prof = rg.theWorld.theProfiler;
        val cam = CameraInfo.getInstance();
        prof.startSection("occ");
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_FOG);
        if (!Debug.ENABLED || Debug.occlusionMask) {
            GL11.glColorMask(false, false, false, false);
        }
        GL11.glDepthMask(false);

        prof.startSection("check");
        occlusionQueryManager.processDispatchedQueries();
        prof.endStartSection("schedule");

        val nearest = Math.min(rg.renderersLoaded, 32);
        for (int i = 0; i < nearest; i++) {
            val wr = rg.sortedWorldRenderers[i];
            discardOcclusionCheckRender(wr);
        }

        GL11.glPushMatrix();
        double cx = 0;
        double cy = 0;
        double cz = 0;
        val eyeX = cam.getEyeX();
        val eyeY = cam.getEyeY();
        val eyeZ = cam.getEyeZ();
        val eyeXF = (float)eyeX;
        val eyeYF = (float)eyeY;
        val eyeZF = (float)eyeZ;
        GL11.glTranslated(-eyeX, -eyeY, -eyeZ);
        val FPS = TickTimeTracker.averageFPS();

        for (int i = 32; i < rg.renderersLoaded; i++) {
            val wr = rg.sortedWorldRenderers[i];
            if (!wr.isInFrustum || wr.isWaitingOnOcclusionQuery)
                continue;
            int recheckInterval;
            if (FPS > 500) {
                val dX = eyeXF - wr.posXPlus;
                val dY = eyeYF - wr.posXPlus;
                val dZ = eyeZF - wr.posXPlus;
                val dist = MathHelper.sqrt_float(dX * dX + dY * dY + dZ * dZ);
                recheckInterval = 16 + (int) (16 * (dist / 128f));
            } else if (FPS > 300) {
                recheckInterval = 16;
            } else if (FPS > 200) {
                recheckInterval = 8;
            } else if (FPS > 100) {
                recheckInterval = 4;
            } else {
                recheckInterval = 2;
            }
            if (wr.isVisible)
                recheckInterval *= 4;
            val recheck = ((i + occlusionRecheckCounterRender) % recheckInterval) == 0;
            if (!recheck) {
                continue;
            }

            if (!wr.skipAllRenderPasses()) {
                if (occlusionListRender == -1) {
                    buildOcclusionRenderListRender();
                }
                double x = wr.posX;
                double y = wr.posY;
                double z = wr.posZ;
                double dx = x - cx;
                double dy = y - cy;
                double dz = z - cz;
                if (dx != 0 || dy != 0 || dz != 0) {
                    GL11.glTranslated(dx, dy, dz);
                    cx += dx;
                    cy += dy;
                    cz += dz;
                }
                queryStartCallbackRender.wr = wr;
                if (!occlusionQueryManager.dispatchNewOcclusionQuery(queryStartCallbackRender)) {
                    occlusionQueryManager = occlusionQueryManager.grow(1024);
                    if (!occlusionQueryManager.dispatchNewOcclusionQuery(queryStartCallbackRender))
                        break;
                }
            }
        }
        GL11.glPopMatrix();
        GL11.glPopAttrib();

        occlusionRecheckCounterRender++;
        prof.endSection();
        prof.endSection();
    }
    private static final ResourceLocation WHITE_TEX = new ResourceLocation("falsetweaks", "white.png");

    private class QueryStartCallbackRender implements OcclusionQueryManager.QueryStartCallback {
        private WorldRenderer wr;
        @Override
        public OcclusionQueryManager.QueryFinishCallback run(OcclusionQueryManager.QueryLauncher launcher) {
            mc.getTextureManager().bindTexture(WHITE_TEX);
            launcher.begin(GL33.GL_ANY_SAMPLES_PASSED);
            GL11.glCallList(occlusionListRender);
            launcher.end(GL33.GL_ANY_SAMPLES_PASSED);
            mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            wr.isWaitingOnOcclusionQuery = true;
            return new QueryFinishCallbackRender(wr);
        }
    }

    private static class QueryFinishCallbackRender implements OcclusionQueryManager.QueryFinishCallback {
        private final WorldRenderer wr;
        private final int canary;

        QueryFinishCallbackRender(WorldRenderer wr) {
            this.wr = wr;
            canary = ((WorldRendererOcclusion)wr).ft$frustumCheckCanaryRender();
        }

        @Override
        public boolean earlyDiscard() {
            return ((WorldRendererOcclusion)wr).ft$frustumCheckCanaryRender() != canary;
        }

        @Override
        public boolean preCheck() {
            return true;
        }

        @Override
        public void run(int queryResult) {
            if (wr.isWaitingOnOcclusionQuery) {
                wr.isWaitingOnOcclusionQuery = false;
                ((WorldRendererOcclusion)wr).ft$bumpFrustumCheckCanaryRender();
                wr.isVisible = queryResult != 0;
            }
        }
    }

    private class QueryStartCallbackShadow implements OcclusionQueryManager.QueryStartCallback {
        private WorldRendererOcclusion iwr;
        @Override
        public OcclusionQueryManager.QueryFinishCallback run(OcclusionQueryManager.QueryLauncher launcher) {
            launcher.begin(GL33.GL_ANY_SAMPLES_PASSED);
            GL11.glCallList(occlusionListShadow);
            launcher.end(GL33.GL_ANY_SAMPLES_PASSED);
            iwr.ft$waitingOnShadowOcclusionQuery(true);
            return new QueryFinishCallbackShadow(iwr);
        }
    }

    private static class QueryFinishCallbackShadow implements OcclusionQueryManager.QueryFinishCallback {
        private final WorldRendererOcclusion iwr;
        private final int canary;

        QueryFinishCallbackShadow(WorldRendererOcclusion iwr) {
            this.iwr = iwr;
            canary = iwr.ft$frustumCheckCanaryShadow();
        }

        @Override
        public boolean earlyDiscard() {
            return iwr.ft$frustumCheckCanaryShadow() != canary;
        }

        @Override
        public boolean preCheck() {
            return true;
        }

        @Override
        public void run(int queryResult) {
            if (iwr.ft$waitingOnShadowOcclusionQuery()) {
                iwr.ft$waitingOnShadowOcclusionQuery(false);
                iwr.ft$bumpFrustumCheckCanaryShadow();
                iwr.ft$isVisibleShadows(queryResult != 0);
            }
        }
    }

//    private void checkOcclusionQueryResultShadow() {
//        int offset = shadowCheckCounter & 7;
//        for (int i = offset; i < rg.renderersLoaded; i += 8) {
//            val wr = rg.sortedWorldRenderers[i];
//            val iwr = (WorldRendererOcclusion) wr;
//            if (iwr.ft$occlusionInited() && iwr.ft$waitingOnShadowOcclusionQuery()) {
//                val query = iwr.ft$occlusionQueryShadow();
//                if (query == 0 || !GL15.glIsQuery(query)) {
//                    iwr.ft$waitingOnShadowOcclusionQuery(false);
//                    continue;
//                }
//                processOcclusionQueryShadow(query, iwr);
//            }
//        }
//        ++shadowCheckCounter;
//    }

//    private static void processOcclusionQueryShadow(int query, WorldRendererOcclusion iwr) {
//        occlusionResult.clear();
//        occlusionResult.put(0, -1);
//        GL15.glGetQueryObjectu(query, GL44.GL_QUERY_RESULT_NO_WAIT, occlusionResult);
//        val res = occlusionResult.get(0);
//        if (res != -1) {
//            iwr.ft$waitingOnShadowOcclusionQuery(false);
//            iwr.ft$isVisibleShadows(res != 0);
//        }
//    }

    public int sortAndRender(int start, int end, int pass, double tick) {
        boolean shadowPass = OcclusionCompat.OptiFineCompat.isShadowPass();
        if (Debug.ENABLED && !Debug.shadowPass && shadowPass) {
            return 0;
        }
        val cam = CameraInfo.getInstance();
        double eX, eY, eZ;
        if (shadowPass) {
            val entitylivingbase = this.mc.renderViewEntity;
            eX = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * tick;
            eY = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * tick;
            eZ = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * tick;
        } else {
            eX = cam.getEyeX();
            eY = cam.getEyeY();
            eZ = cam.getEyeZ();
        }

        RenderList[] allRenderLists = rg.allRenderLists;
        for (int i = 0; i < allRenderLists.length; ++i) {
            allRenderLists[i].resetList();
        }

        if (shadowPass) {
            end = rg.worldRenderers.length;
        }

        int loopStart = start;
        int loopEnd = end;
        byte dir = 1;

        if (pass == 1) {
            loopStart = end - 1;
            loopEnd = start - 1;
            dir = -1;
        }

        val prof = mc.theWorld.theProfiler;
        if (!shadowPass && pass == 0 && mc.gameSettings.showDebugInfo) {
            prof.startSection("debug_info");
            int renderersNotInitialized = 0, renderersBeingClipped = 0, renderersBeingOccluded = 0;
            int renderersBeingRendered = 0, renderersSkippingRenderPass = 0, renderersNeedUpdate = 0;
            WorldRenderer[] worldRenderers = rg.worldRenderers;
            for (int i = 0, e = worldRenderers.length; i < e; ++i) {
                WorldRenderer rend = worldRenderers[i];
                if (!rend.isInitialized) {
                    ++renderersNotInitialized;
                } else if (!rend.isInFrustum) {
                    ++renderersBeingClipped;
                } else if (!rend.isVisible) {
                    ++renderersBeingOccluded;
                } else if (((WorldRendererOcclusion) rend).ft$skipRenderPass()) {
                    ++renderersSkippingRenderPass;
                } else {
                    ++renderersBeingRendered;
                }
                if (rend.needsUpdate) {
                    ++renderersNeedUpdate;
                }
            }

            rg.dummyRenderInt = renderersNotInitialized;
            rg.renderersBeingClipped = renderersBeingClipped;
            rg.renderersBeingOccluded = renderersBeingOccluded;
            rg.renderersBeingRendered = renderersBeingRendered;
            rg.renderersSkippingRenderPass = renderersSkippingRenderPass;
            this.renderersNeedUpdate = renderersNeedUpdate;
            prof.endSection();
        }

        prof.startSection("setup_lists");
        int glListsRendered = 0, allRenderListsLength = 0;

        if (shadowPass) {
            val renderers = rg.worldRenderers;
            ShadowPassOcclusionHelper.begin();
            for (int i = 0; i < renderers.length; i++) {
                val wr = renderers[i];
                if (wr != null && wr.isVisible && wr.isInFrustum && !wr.skipAllRenderPasses()) {
                    ShadowPassOcclusionHelper.addShadowReceiver(wr);
                }
            }
            ShadowPassOcclusionHelper.end();
        }
        WorldRenderer[] sortedWorldRenderers = shadowPass ? rg.worldRenderers : rg.sortedWorldRenderers;

        for (int i = loopStart; i != loopEnd; i += dir) {
            WorldRenderer rend = sortedWorldRenderers[i];
            val iwr = (WorldRendererOcclusion)rend;

            boolean isVisible = false, isInFrustum = false;
            if (shadowPass) {
                isVisible = rend.isVisible;
                isInFrustum = rend.isInFrustum;
                rend.isVisible = iwr.ft$isVisibleShadows();
                rend.isInFrustum = ShadowPassOcclusionHelper.isShadowVisible(rend);
            }

            if ((rend.isVisible && rend.isInFrustum) && !rend.skipRenderPass[pass]) {

                int renderListIndex;

                l:
                {
                    for (int j = 0; j < allRenderListsLength; ++j) {
                        if (allRenderLists[j].rendersChunk(rend.posXMinus, rend.posYMinus, rend.posZMinus)) {
                            renderListIndex = j;
                            break l;
                        }
                    }
                    renderListIndex = allRenderListsLength++;
                    if (allRenderLists.length <= renderListIndex) {
                        rg.allRenderLists = allRenderLists = Arrays.copyOf(allRenderLists, renderListIndex + 1);
                        allRenderLists[renderListIndex] = new RenderList();
                    }
                    allRenderLists[renderListIndex].setupRenderList(rend.posXMinus, rend.posYMinus, rend.posZMinus, eX, eY, eZ);
                }

                allRenderLists[renderListIndex].addGLRenderList(rend.getGLCallListForPass(pass));
                ++glListsRendered;
            }
            if (shadowPass) {
                rend.isVisible = isVisible;
                rend.isInFrustum = isInFrustum;
            }
        }

        prof.endStartSection("call_lists");

        {
            int xSort = MathHelper.floor_double(cam.getX());
            int zSort = MathHelper.floor_double(cam.getZ());
            xSort -= xSort & 1023;
            zSort -= zSort & 1023;
            Arrays.sort(allRenderLists, new RenderDistanceSorter(xSort, zSort));
            rg.renderAllRenderLists(pass, tick);
        }
        prof.endSection();

        runOcclusionCheck(shadowPass, pass);

        return glListsRendered;
    }

    private void initClipThread(ICamera camera) {
        if (renderersToClip != rg.worldRenderers) {
            renderersToClip = rg.worldRenderers;
        }
        if (clipCamera != camera) {
            clipCamera = camera;
        }
        if (clipThread == null) {
            clipThread = new ClipThread();
            FTWorker.addTask(clipThread);
        }
    }

    private void updatePriority(WorldRenderer rend, WorldRendererOcclusion iwr, int i) {
        val interval_64 = (i + rg.frustumCheckOffset & 63) == 0;
        if (!interval_64) {
            return;
        }
        if (iwr.ft$isInUpdateList()) {
            iwr.ft$currentPriority(determinePriority(rend));
        }
    }

    private static void discardOcclusionCheckRender(WorldRenderer rend) {
        rend.isWaitingOnOcclusionQuery = false;
        rend.isVisible = true;
        ((WorldRendererOcclusion)rend).ft$bumpFrustumCheckCanaryRender();
    }

    private static void discardOcclusionCheckShadow(WorldRenderer rend) {
        val iwr = (WorldRendererOcclusion) rend;
        iwr.ft$waitingOnShadowOcclusionQuery(false);
        iwr.ft$isVisibleShadows(true);
        iwr.ft$bumpFrustumCheckCanaryShadow();
    }

    private void updateFrustumFlag(WorldRenderer rend, WorldRendererOcclusion iwr, int i) {
        if (Debug.ENABLED && !Debug.frustumChecks) {
            rend.isInFrustum = true;
            return;
        }
        val ci = iwr.ft$getCullInfo();
        val interval_16 = (i + rg.frustumCheckOffset & 15) == 0;
        if (rend.isInFrustum && (ci.isFrustumCheckPending || !interval_16)) {
            return;
        }
        val prev = rend.isInFrustum;
        val next = iwr.ft$nextIsInFrustum();
        if (prev == next)
            return;
        discardOcclusionCheckRender(rend);
        fakeFrustum.returnBool = next;
        rend.updateInFrustum(fakeFrustum);
    }

    public void clipRenderersByFrustum(ICamera camera, float p_72729_2_) {
        initClipThread(camera);
        val startOffset = rg.frustumCheckOffset & 1;
        for (int i = startOffset; i < renderersToClip.length; i += 2) {
            val rend = renderersToClip[i];
            if (rend.skipAllRenderPasses()) {
                continue;
            }
            val iwr = ((WorldRendererOcclusion) rend);
            updatePriority(rend, iwr, i);
            updateFrustumFlag(rend, iwr, i);
        }
        ++rg.frustumCheckOffset;
    }

    public void ft$setRendererUpdateOrderProvider(IRendererUpdateOrderProvider orderProvider) {
        this.rendererUpdateOrderProvider = orderProvider;
    }

    public void ft$addRenderGlobalListener(IRenderGlobalListener listener) {
        this.eventListeners.add(listener);
    }

    private static class FakeCamera implements ICamera {
        public boolean returnBool;

        @Override
        public boolean isBoundingBoxInFrustum(AxisAlignedBB p_78546_1_) {
            return returnBool;
        }

        @Override
        public void setPosition(double p_78547_1_, double p_78547_3_, double p_78547_5_) {

        }
    }

    private class ClipThread implements ThreadedTask {
        private int counter = 0;
        private int evenOdd = 0;

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
            if (Debug.ENABLED && !Debug.frustumChecks) {
                return false;
            }
            val renderers = renderersToClip;
            if (renderers == null) {
                return false;
            }
            val camera = clipCamera;
            if (camera == null) {
                return false;
            }
            profiler.startSection("clip");
            try {
                int offset = evenOdd & 1;
                for (int i = offset; i < renderers.length; i += 2) {
                    val mod15 = (i + counter & 15) == 0;
                    WorldRenderer wr = renderers[i];
                    WorldRendererOcclusion iwr = (WorldRendererOcclusion) wr;
                    val ci = iwr.ft$getCullInfo();
                    if (wr.isInFrustum && (!ci.isFrustumCheckPending || mod15)) {
                        continue;
                    }
                    val next = camera.isBoundingBoxInFrustum(wr.rendererBoundingBox);
                    iwr.ft$nextIsInFrustum(next);
                    ci.isFrustumCheckPending = false;
                    ci.isFrustumStateUpdated = true;
                }
                ++evenOdd;
                if (evenOdd >= 2) {
                    evenOdd = 0;
                    ++counter;
                }
                return true;
            } finally {
                profiler.endSection();
            }
        }
    }

}
