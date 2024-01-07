package com.falsepattern.falsetweaks.modules.occlusion;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.config.OcclusionConfig;
import com.falsepattern.falsetweaks.modules.occlusion.interfaces.IRenderGlobalMixin;
import lombok.val;
import lombok.var;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OcclusionRenderer {

    private final Minecraft mc;
    private final RenderGlobal rg;
    
    private Thread clientThread;

    private ArrayList<WorldRenderer> worldRenderersToUpdateList;

    private double prevRenderX, prevRenderY, prevRenderZ;
    private int cameraStaticTime;

    private short alphaSortProgress = 0;
    private byte frameCounter, frameTarget;

    private int renderersNeedUpdate;

    private boolean resortUpdateList;
    
    private IRendererUpdateOrderProvider rendererUpdateOrderProvider;
    private List<IRenderGlobalListener> eventListeners;

    /* Make sure other threads can see changes to this */
    private volatile boolean deferNewRenderUpdates;


    private boolean populated = false;
    private int lastPosChunkX;
    private int lastPosChunkY;
    private int lastPosChunkZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    /**
     * Order:
     * [X][Z][Y]
     */
    private WorldRenderer[][][] spatialRendererStore;

    private List<WorldRenderer> visgraphDummyRecheckBackBuffer = new ArrayList<>();
    private List<WorldRenderer> visgraphDummyRecheckFrontBuffer = new ArrayList<>();
    private int visgraphPointer = 0;
    private long lastCheck = 0;
    
    public OcclusionRenderer(RenderGlobal renderGlobal) {
        this.rg = renderGlobal;
        this.mc = renderGlobal.mc;
    }
    
    public RenderGlobal getRenderGlobal() {
        return rg;
    }


    /**
     * If the update list is not queued for a full resort (e.g. when the player moves or renderers have their positions
     * changed), uses binary search to add the renderer in the update queue at the appropriate place. Otherwise,
     * the renderer is just added to the end of the list.
     * @param wr renderer to add to the list
     */
    private void addRendererToUpdateQueue(WorldRenderer wr) {
        for(EnumFacing dir : OcclusionHelpers.FACING_VALUES) {
            Chunk chunk = rg.theWorld.getChunkFromBlockCoords(wr.posX + dir.getFrontOffsetX() * 16, wr.posZ + dir.getFrontOffsetZ() * 16);
            if(chunk != null && chunk instanceof EmptyChunk)
                return; // do not allow rendering chunk without neighbors
        }
        if(!((IWorldRenderer)wr).ft$isInUpdateList()) {
            ((IWorldRenderer)wr).ft$setInUpdateList(true);
            worldRenderersToUpdateList.add(wr);
        }
    }
    
    public void handleOffthreadUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        if(deferNewRenderUpdates || Thread.currentThread() != clientThread) {
            OcclusionHelpers.updateArea(x1, y1, z1, x2, y2, z2);
        } else {
            internalMarkBlockUpdate(x1, y1, z1, x2, y2, z2);
        }
    }
    
    public void internalMarkBlockUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
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
                    WorldRenderer worldrenderer = worldRenderers[k4];

                    if (!worldrenderer.needsUpdate || (worldrenderer.isVisible && !((IWorldRenderer)worldrenderer).ft$isInUpdateList())) {
                        worldrenderer.markDirty();
                        OcclusionCompat.DragonAPICompat.ChangePacketRenderer$onChunkRerender(x1, y1, z1, x2, y2, z2, worldrenderer);
                        OcclusionHelpers.worker.dirty = true;
                    } else {
                        int size = eventListeners.size();
                        for(int m = -1; ++m < size;) {
                            eventListeners.get(m).onDirtyRendererChanged(worldrenderer);
                        }
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
            StringBuilder r = new StringBuilder(
                    3 + 6 + 1 + 6 +
                    5 + 6
                    );
            r.append("C: ").append(rg.renderersLoaded).append('/').append(rg.worldRenderers.length);
            r.append(", N: ").append(rg.worldRenderersToUpdate.size());
            return r.toString();
        } else {
            StringBuilder r = new StringBuilder(
                    3 + 6 + 1 + 6 + 1 + 6
                    + 5 + 6
                    + 5 + 6
                    + 5 + 6
                    + 5 + 6
                    + 5 + 6
                    + 5 + 6
            );
            r.append("C: ").append(rg.renderersBeingRendered).append('/').append(rg.renderersLoaded).append('/').append(rg.worldRenderers.length);
            r.append(". F: ").append(rg.renderersBeingClipped);
            r.append(", O: ").append(rg.renderersBeingOccluded);
            r.append(", E: ").append(rg.renderersSkippingRenderPass);
            r.append(", I: ").append(rg.dummyRenderInt);
            r.append("; U: ").append(renderersNeedUpdate);
            r.append(", N: ").append(rg.worldRenderersToUpdate.size());
            return r.toString();
        }
    }

    public void initBetterLists() {
        worldRenderersToUpdateList = new ArrayList<>();
        /* Make sure any vanilla code modifying the update queue crashes */
        rg.worldRenderersToUpdate = Collections.unmodifiableList(worldRenderersToUpdateList);
        clientThread = Thread.currentThread();
        rendererUpdateOrderProvider = new DefaultRendererUpdateOrderProvider();
        eventListeners = new ArrayList<>();
    }

    public void clearRendererUpdateQueue(List instance) {
        if(instance == rg.worldRenderersToUpdate) {
            for(WorldRenderer wr : worldRenderersToUpdateList) {
                ((IWorldRenderer)wr).ft$setInUpdateList(false);
            }
            worldRenderersToUpdateList.clear();
        } else {
            throw new AssertionError("Transformer applied to the wrong List.clear method");
        }
    }
    
    private static int fixPos(int pos, int amt) {
        int r = Math.floorDiv(pos, 16) % amt;
        if(r < 0) {
            r += amt;
        }
        return r;
    }
    
    public WorldRenderer getRenderer(int x, int y, int z) {
        if ((y - 15) > rg.maxBlockY || y < rg.minBlockY || (x - 15) > rg.maxBlockX || x < rg.minBlockX || (z - 15) > rg.maxBlockZ || z < rg.minBlockZ)
            return null;

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

    private boolean rebuildChunks(EntityLivingBase view, long deadline) {
        int updateLimit = (deadline == 0) ? 5 : Integer.MAX_VALUE;
        int updates = 0;

        boolean spareTime = true;
        deferNewRenderUpdates = true;
        rendererUpdateOrderProvider.prepare(worldRenderersToUpdateList, updateLimit);
        while(updates < updateLimit && rendererUpdateOrderProvider.hasNext(worldRenderersToUpdateList)) {
            WorldRenderer worldrenderer = rendererUpdateOrderProvider.next(worldRenderersToUpdateList);
            
            ((IWorldRenderer)worldrenderer).ft$setInUpdateList(false);

            if (!(worldrenderer.isInFrustum & worldrenderer.isVisible) && !OcclusionHelpers.DEBUG_LAZY_CHUNK_UPDATES) {
                continue;
            }

            boolean e = worldrenderer.isWaitingOnOcclusionQuery;
            worldrenderer.updateRenderer(view);
            worldrenderer.isVisible &= !e;
            worldrenderer.isWaitingOnOcclusionQuery = worldrenderer.skipAllRenderPasses() || (mc.theWorld.getChunkFromBlockCoords(worldrenderer.posX, worldrenderer.posZ) instanceof EmptyChunk);
            // can't add fields, re-use

            if(worldrenderer.distanceToEntitySquared(view) > 272f) {
                updates++;
                if(!worldrenderer.isWaitingOnOcclusionQuery || deadline != 0 || OcclusionHelpers.DEBUG_LAZY_CHUNK_UPDATES) {
                    long t = System.nanoTime();
                    if (t > deadline) {
                        spareTime = false;
                        break;
                    }
                }
            }
        }
        rendererUpdateOrderProvider.cleanup(worldRenderersToUpdateList);
        deferNewRenderUpdates = false;
        return spareTime;
    }

    public void performCullingUpdates(EntityLivingBase view, boolean p_72716_2_) {
        rg.theWorld.theProfiler.startSection("deferred_updates");
        while(OcclusionHelpers.deferredAreas.size() > 0) {
            OcclusionHelpers.processUpdate(this);
        }
        rg.theWorld.theProfiler.endStartSection("rebuild");

        CameraInfo cam = CameraInfo.getInstance();

        boolean cameraMoved = cam.getEyeX() != prevRenderX || cam.getEyeY() != prevRenderY || cam.getEyeZ() != prevRenderZ;

        prevRenderX = cam.getEyeX();
        prevRenderY = cam.getEyeY();
        prevRenderZ = cam.getEyeZ();

        boolean cameraRotated = PreviousActiveRenderInfo.changed();

        if(!cameraRotated && !cameraMoved) {
            cameraStaticTime++;
        } else {
            cameraStaticTime = 0;
        }


        if (!rg.worldRenderersToUpdate.isEmpty()) {
            ++frameCounter;
            boolean doUpdateAcceleration = cameraStaticTime > 2 &&
                                           !OcclusionHelpers.DEBUG_LAZY_CHUNK_UPDATES &&
                                           !OcclusionHelpers.DEBUG_NO_UPDATE_ACCELERATION &&
                                           OcclusionConfig.DYNAMIC_CHUNK_UPDATES_DEADLINE > 0;
            /* If the camera is not moving, assume a deadline of N FPS. */
            rebuildChunks(view, !doUpdateAcceleration ? OcclusionHelpers.chunkUpdateDeadline
                    : mc.entityRenderer.renderEndNanoTime + (1_000_000_000L / OcclusionConfig.DYNAMIC_CHUNK_UPDATES_DEADLINE));
        }

        rg.theWorld.theProfiler.endStartSection("scan");
        int yaw = MathHelper.floor_float(view.rotationYaw + 45) >> 4;
        int pitch = MathHelper.floor_float(view.rotationPitch + 45) >> 4;
        if (OcclusionHelpers.worker.dirty || cameraRotated || OcclusionHelpers.DEBUG_ALWAYS_RUN_OCCLUSION) {
            // Clear the update queue, the graph search will repopulate it in the correct order
            OcclusionHelpers.renderer.clearRendererUpdateQueue(rg.worldRenderersToUpdate);
            OcclusionHelpers.worker.run(true);
            PreviousActiveRenderInfo.update();
        }
        rg.theWorld.theProfiler.endSection();
    }
    
    public void resetLoadedRenderers() {
        if(rg.theWorld != null) {
            rg.renderersLoaded = 0;
        }
    }

    private void repositionSmart(RenderGlobal rg, CameraInfo cam) {
        val prevX = rg.prevChunkSortX;
        val prevY = rg.prevChunkSortY;
        val prevZ = rg.prevChunkSortZ;
        val camX = MathHelper.floor_double(cam.getX());
        val camY = MathHelper.floor_double(cam.getY());
        val camZ = MathHelper.floor_double(cam.getZ());
        val currX = cam.getChunkCoordX();
        val currY = cam.getChunkCoordY();
        val currZ = cam.getChunkCoordZ();
        val deltaX = currX - prevX;
        val deltaZ = currZ - prevZ;
        val xSize = rg.renderChunksWide;
        val ySize = rg.renderChunksTall;
        val zSize = rg.renderChunksDeep;
        if (Math.abs(deltaX) >= 4 || Math.abs(deltaZ) >= 4 || !populated
            || lastPosChunkX != prevX || lastPosChunkZ != prevZ
            || xSize != sizeX || ySize != sizeY || zSize != sizeZ) {
            rg.markRenderersForNewPosition(camX, camY, camZ);
            rebuidSpatialRenderStore(rg, xSize, ySize, zSize, camX, camZ);
            OcclusionHelpers.renderer.updateRendererNeighborsFull();
            OcclusionHelpers.worker.run(true);
        } else {
            if (deltaX != 0) {
                int dRaw = deltaX < 0 ? -1 : 1;
                int dAbs = Math.abs(deltaX);
                for (int i = 0; i < dAbs; i++) {
                    repositionDeltaX(rg, dRaw);
                    OcclusionHelpers.renderer.updateRendererNeighborsXPartial(dRaw);
                }
            }
            if (deltaZ != 0) {
                int dRaw = deltaZ < 0 ? -1 : 1;
                int dAbs = Math.abs(deltaZ);
                for (int i = 0; i < dAbs; i++) {
                    repositionDeltaZ(rg, dRaw);
                    OcclusionHelpers.renderer.updateRendererNeighborsZPartial(dRaw);
                }
            }
            OcclusionHelpers.worker.run(true);
        }

        lastPosChunkX = rg.prevChunkSortX = currX;
        lastPosChunkY = rg.prevChunkSortY = currY;
        lastPosChunkZ = rg.prevChunkSortZ = currZ;
    }

    private void repositionDeltaX(RenderGlobal rg, int deltaX) {
        rotate(spatialRendererStore, deltaX);
        WorldRenderer[][] edgeColumn;
        WorldRenderer[][] prevEdgeColumn;
        int deltaPos;
        if (deltaX > 0) {
            edgeColumn = spatialRendererStore[sizeX - 1];
            prevEdgeColumn = spatialRendererStore[sizeX - 2];
            deltaPos = 16;
        } else {
            edgeColumn = spatialRendererStore[0];
            prevEdgeColumn = spatialRendererStore[1];
            deltaPos = -16;
        }
        for (int z = 0; z < sizeZ; z++) {
            val edgeChunk = edgeColumn[z];
            val prevEdgeChunk = prevEdgeColumn[z];
            updateRelativeChunk(edgeChunk, prevEdgeChunk, deltaPos, 0);
        }
        rg.minBlockX += deltaPos;
        rg.maxBlockX += deltaPos;
    }

    private void repositionDeltaZ(RenderGlobal rg, int deltaZ) {
        int deltaPos = deltaZ * 16;
        for (int x = 0; x < sizeX; x++) {
            val columnX = spatialRendererStore[x];
            rotate(columnX, deltaZ);
            WorldRenderer[] edgeChunk;
            WorldRenderer[] prevEdgeChunk;
            if (deltaZ > 0) {
                edgeChunk = columnX[sizeZ - 1];
                prevEdgeChunk = columnX[sizeZ - 2];
            } else {
                edgeChunk = columnX[0];
                prevEdgeChunk = columnX[1];
            }
            updateRelativeChunk(edgeChunk, prevEdgeChunk, 0, deltaPos);
        }
        rg.minBlockZ += deltaPos;
        rg.maxBlockZ += deltaPos;
    }

    private void updateRelativeChunk(WorldRenderer[] edgeChunk, WorldRenderer[] prevEdgeChunk, int deltaX, int deltaZ) {
        for (int y = 0; y < sizeY; y++) {
            val edgeSubChunk = edgeChunk[y];
            val prevEdgeSubChunk = prevEdgeChunk[y];
            OcclusionHelpers.renderer.setPositionAndMarkInvisible(edgeSubChunk, prevEdgeSubChunk.posX + deltaX, prevEdgeSubChunk.posY, prevEdgeSubChunk.posZ + deltaZ);
        }
    }

    private static <T> void rotate(T[] array, int delta) {
        if (delta < -1 || delta > 1)
            throw new IllegalArgumentException("Can only rotate array by single steps!");

        int grab;
        int drop;
        int start;
        int end;
        if (delta > 0) {
            grab = 0;
            drop = array.length - 1;
            start = 1;
            end = array.length;
        } else {
            grab = array.length - 1;
            drop = 0;
            start = array.length - 2;
            end = -1;
        }
        val toShift = array[grab];
        for (int i = start; i != end; i += delta) {
            array[i - delta] = array[i];
        }
        array[drop] = toShift;
    }

    private void rebuidSpatialRenderStore(RenderGlobal rg, int xSize, int ySize, int zSize, int camX, int camZ) {
        spatialRendererStore = new WorldRenderer[xSize][zSize][ySize];
        val baseX = camX - (xSize + 1) * 8;
        val baseZ = camZ - (zSize + 1) * 8;
        for (int i = 0; i < rg.worldRenderers.length; i++) {
            val wr = rg.worldRenderers[i];
            var wrcX = (wr.posX - baseX) >> 4;
            var wrcY = wr.posY >> 4;
            var wrcZ = (wr.posZ - baseZ) >> 4;

            if (spatialRendererStore[wrcX]
                        [wrcZ]
                        [wrcY] != null) {
                System.err.println("WUT @" + wrcX + "," + wrcY + "," + wrcZ);
            }
            spatialRendererStore[wrcX][wrcZ][wrcY] = wr;

        }
        populated = true;
        sizeX = xSize;
        sizeY = ySize;
        sizeZ = zSize;
    }

    public void resetOcclusionWorker() {
        populated = false;
        updateRendererNeighborsFull();
        if(OcclusionHelpers.worker != null) {
            OcclusionHelpers.worker.dirty = true;
        }
    }

    private void updateRendererNeighborsXPartial(int deltaX) {
        WorldRenderer[][] oldNeighborColumn;
        WorldRenderer[][] column;
        WorldRenderer[][] newNeighborColumn;
        EnumFacing towardsOldNeighbor;
        EnumFacing towardsNewNeighbor;
        if (deltaX > 0) {
            oldNeighborColumn = spatialRendererStore[0];
            column = spatialRendererStore[sizeX - 1];
            newNeighborColumn = spatialRendererStore[sizeX - 2];
            towardsOldNeighbor = EnumFacing.WEST;
            towardsNewNeighbor = EnumFacing.EAST;
        } else {
            oldNeighborColumn = spatialRendererStore[sizeX - 1];
            column = spatialRendererStore[0];
            newNeighborColumn = spatialRendererStore[1];
            towardsOldNeighbor = EnumFacing.EAST;
            towardsNewNeighbor = EnumFacing.WEST;
        }
        for (int z = 0; z < sizeZ; z++) {
            updateNeighborsShift(oldNeighborColumn[z], column[z], newNeighborColumn[z], towardsOldNeighbor, towardsNewNeighbor);
        }
    }

    private void updateRendererNeighborsZPartial(int deltaZ) {
        for (int x = 0; x < sizeX; x++) {
            val xColumn = spatialRendererStore[x];
            WorldRenderer[] oldNeighborChunk;
            WorldRenderer[] chunk;
            WorldRenderer[] newNeighborChunk;
            EnumFacing towardsOldNeighbor;
            EnumFacing towardsNewNeighbor;
            if (deltaZ > 0) {
                oldNeighborChunk = xColumn[0];
                chunk = xColumn[sizeZ - 1];
                newNeighborChunk = xColumn[sizeZ - 2];
                towardsOldNeighbor = EnumFacing.SOUTH;
                towardsNewNeighbor = EnumFacing.NORTH;
            } else {
                oldNeighborChunk = xColumn[sizeZ - 1];
                chunk = xColumn[0];
                newNeighborChunk = xColumn[1];
                towardsOldNeighbor = EnumFacing.NORTH;
                towardsNewNeighbor = EnumFacing.SOUTH;
            }
            updateNeighborsShift(oldNeighborChunk, chunk, newNeighborChunk, towardsOldNeighbor, towardsNewNeighbor);
        }
    }

    private void updateNeighborsShift(WorldRenderer[] oldNeighbor, WorldRenderer[] chunk, WorldRenderer[] newNeighbor, EnumFacing towardsOldNeighbor, EnumFacing towardsNewNeighbor) {
        for (int y = 0; y < sizeY; y++) {
            val oldNeighborCI = ((IWorldRenderer)oldNeighbor[y]).ft$getCullInfo();
            val newNeighborCI = ((IWorldRenderer)newNeighbor[y]).ft$getCullInfo();
            WorldRenderer rend = chunk[y];
            OcclusionWorker.CullInfo ci = ((IWorldRenderer) rend).ft$getCullInfo();
            updateVisGraph(rend, ci);
            if (ci.visGraph == OcclusionWorker.DUMMY)
                visgraphDummyRecheckBackBuffer.add(rend);
            ci.setNeighbor(towardsOldNeighbor, null);
            ci.setNeighbor(towardsNewNeighbor, newNeighborCI);
            oldNeighborCI.setNeighbor(towardsNewNeighbor, null);
            newNeighborCI.setNeighbor(towardsOldNeighbor, ci);
        }
    }

    private void updateRendererNeighborsFull() {
        visgraphDummyRecheckBackBuffer.clear();
        visgraphDummyRecheckFrontBuffer.clear();
        if(rg.worldRenderers == null) return;
        for(int i = 0; i < rg.worldRenderers.length; i++) {
            WorldRenderer rend = rg.worldRenderers[i];
            OcclusionWorker.CullInfo ci = ((IWorldRenderer) rend).ft$getCullInfo();
            ci.wrIdx = i;
            updateVisGraph(rend, ci);
            if (ci.visGraph == OcclusionWorker.DUMMY)
                visgraphDummyRecheckBackBuffer.add(rend);
            updateNeighborsForRendererInefficient(rend, ci);
        }
    }

    private void updateVisGraph(WorldRenderer rend, OcclusionWorker.CullInfo ci) {
        Chunk o = rend.worldObj.getChunkFromBlockCoords(rend.posX, rend.posZ);
        VisGraph oSides = isChunkEmpty(o) ? OcclusionWorker.DUMMY : ((ICulledChunk)o).getVisibility()[rend.posY >> 4];
        ci.visGraph = oSides;
        ci.vis = oSides.getVisibilityArray();
    }

    private void updateNeighborsForRendererInefficient(WorldRenderer rend, OcclusionWorker.CullInfo ci) {
        for(EnumFacing dir : OcclusionHelpers.FACING_VALUES) {
            WorldRenderer neighbor = getRenderer(
                    rend.posX + dir.getFrontOffsetX() * 16,
                    rend.posY + dir.getFrontOffsetY() * 16,
                    rend.posZ + dir.getFrontOffsetZ() * 16
                                                );
            ci.setNeighbor(dir, neighbor == null ? null : ((IWorldRenderer)neighbor).ft$getCullInfo());
        }
    }

    public void pushWorkerRenderer(WorldRenderer wr) {
        if(!(mc.theWorld.getChunkFromBlockCoords(wr.posX, wr.posZ) instanceof EmptyChunk))
            addRendererToUpdateQueue(wr);
    }

    public void markRendererInvisible(WorldRenderer instance) {
        instance.isVisible = false;
        instance.isInFrustum = false;
        instance.markDirty();
    }

    public void setPositionAndMarkInvisible(WorldRenderer wr, int x, int y, int z) {
        wr.setPosition(x, y, z);
        if(((IWorldRenderer)wr).ft$isInUpdateList())
            OcclusionHelpers.worker.dirty = true;
        if(!wr.isInitialized) {
            wr.isWaitingOnOcclusionQuery = false;
            wr.isVisible = false;
        }
    }

    public void runWorkerFull() {
        updateRendererNeighborsFull();
        OcclusionHelpers.worker.run(true);
    }

    private void queryMissingVisgraphs() {
        if (visgraphPointer >= visgraphDummyRecheckFrontBuffer.size()) {
            long newCheck = System.nanoTime();
            long delta = newCheck - lastCheck;
            if (delta < 100_000_000)
                return;
            lastCheck = newCheck;
            val tmp = visgraphDummyRecheckBackBuffer;
            visgraphDummyRecheckFrontBuffer.clear();
            visgraphDummyRecheckBackBuffer = visgraphDummyRecheckFrontBuffer ;
            visgraphDummyRecheckFrontBuffer = tmp;
            visgraphPointer = 0;
        }
        int visgraphEnd = Math.min(visgraphPointer + 1000, visgraphDummyRecheckFrontBuffer.size());
        for (; visgraphPointer < visgraphEnd; visgraphPointer++) {
            val wr = visgraphDummyRecheckFrontBuffer.get(visgraphPointer);
            val ci = ((IWorldRenderer)wr).ft$getCullInfo();
            if (ci.visGraph == OcclusionWorker.DUMMY) {
                updateVisGraph(wr, ci);
                if (ci.visGraph == OcclusionWorker.DUMMY)
                    visgraphDummyRecheckBackBuffer.add(wr);
            }
        }
        if (visgraphPointer >= visgraphDummyRecheckFrontBuffer.size()) {
            visgraphDummyRecheckFrontBuffer.clear();
        }
    }

    public int sortAndRender(EntityLivingBase view, int pass, double tick) {
        OcclusionCompat.OptiFineCompat.updateDynamicLights(rg);
        queryMissingVisgraphs();
        CameraInfo cam = CameraInfo.getInstance();
        cam.update(view, tick);

        rg.theWorld.theProfiler.startSection("sortchunks");

        if (this.mc.gameSettings.renderDistanceChunks != rg.renderDistanceChunks && !(this.mc.currentScreen instanceof GuiVideoSettings))
        {
            rg.loadRenderers();
        }

        WorldRenderer[] sortedWorldRenderers = rg.sortedWorldRenderers;
        if (rg.renderersLoaded > 0) {
            int e = rg.renderersLoaded - 10;
            e &= e >> 31;
            e += 10;
            for (int j = 0; j < e; ++j) {
                rg.worldRenderersCheckIndex = (rg.worldRenderersCheckIndex + 1) % rg.renderersLoaded;
                WorldRenderer rend = sortedWorldRenderers[rg.worldRenderersCheckIndex];

                if (rend.isVisible & (rend.needsUpdate || !rend.isInitialized) & !(this.mc.theWorld.getChunkFromBlockCoords(rend.posX, rend.posZ) instanceof EmptyChunk)) {
                    addRendererToUpdateQueue(rend);
                }
            }
        }

        rg.theWorld.theProfiler.startSection("reposition_chunks");
        if (rg.prevChunkSortX != cam.getChunkCoordX() || rg.prevChunkSortY != cam.getChunkCoordY() || rg.prevChunkSortZ != cam.getChunkCoordZ()) {
            repositionSmart(rg, cam);
            OcclusionHelpers.worker.dirty = true;
        }
        rg.theWorld.theProfiler.endSection();

        rg.theWorld.theProfiler.startSection("alpha_sort");
        if(distanceSquared(cam.getX(), cam.getY(), cam.getZ(), rg.prevRenderSortX, rg.prevRenderSortY, rg.prevRenderSortZ) > 1) {
            rg.prevRenderSortX = cam.getX();
            rg.prevRenderSortY = cam.getY();
            rg.prevRenderSortZ = cam.getZ();

            alphaSortProgress = 0;
        }

        int amt = rg.renderersLoaded < 27 ? rg.renderersLoaded : Math.max(rg.renderersLoaded >> 1, 27);
        if (alphaSortProgress < amt) {
            int amountPerFrame = 1;
            for (int i = 0; i < amountPerFrame && alphaSortProgress < amt; ++i) {
                WorldRenderer r = sortedWorldRenderers[alphaSortProgress++];
                r.updateRendererSort(view);
            }
        }
        rg.theWorld.theProfiler.endSection();

        rg.theWorld.theProfiler.endStartSection("render");
        if (OcclusionCompat.OptiFineCompat.isOptiFineFogOff(this.mc.entityRenderer)) {
            GL11.glDisable(GL11.GL_FOG);
        }
        RenderHelper.disableStandardItemLighting();
        int k = rg.renderSortedRenderers(0, rg.renderersLoaded, pass, tick);
        ((IRenderGlobalMixin)rg).ft$setSortedRendererCount(rg.renderersLoaded);

        rg.theWorld.theProfiler.endSection();
        return k;
    }

    public int sortAndRender(int start, int end, int pass, double tick) {
        boolean shadowPass = OcclusionCompat.OptiFineCompat.isShadowPass();
        val cam = CameraInfo.getInstance();
        double eX, eY, eZ;
        boolean noOcclusion;
        if (shadowPass) {
            noOcclusion = true;
            val entitylivingbase = this.mc.renderViewEntity;
            eX = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * tick;
            eY = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * tick;
            eZ = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * tick;
        } else {
            eX = cam.getEyeX();
            eY = cam.getEyeY();
            eZ = cam.getEyeZ();
            noOcclusion = cam.getEyeY() > 256;
        }

        RenderList[] allRenderLists = rg.allRenderLists;
        for (int i = 0; i < allRenderLists.length; ++i) {
            allRenderLists[i].resetList();
        }

        if (noOcclusion)
            end = rg.worldRenderers.length;

        int loopStart = start;
        int loopEnd = end;
        byte dir = 1;

        if (pass == 1) {
            loopStart = end - 1;
            loopEnd = start - 1;
            dir = -1;
        }

        if (!shadowPass && pass == 0 && mc.gameSettings.showDebugInfo) {

            mc.theWorld.theProfiler.startSection("debug_info");
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
                } else if (rend.isWaitingOnOcclusionQuery) {
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
            mc.theWorld.theProfiler.endSection();
        }

        mc.theWorld.theProfiler.startSection("setup_lists");
        int glListsRendered = 0, allRenderListsLength = 0;
        WorldRenderer[] sortedWorldRenderers = noOcclusion ? rg.worldRenderers : rg.sortedWorldRenderers;

        for (int i = loopStart; i != loopEnd; i += dir) {
            WorldRenderer rend = sortedWorldRenderers[i];

            boolean isVisible = false, isInFrustum = false;
            if (noOcclusion) {
                isVisible = rend.isVisible;
                isInFrustum = rend.isInFrustum;
                rend.isVisible = true;
                rend.isInFrustum = true;
            }

            if ((rend.isVisible && rend.isInFrustum) && !rend.skipRenderPass[pass]) {

                int renderListIndex;

                l: {
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
            if (noOcclusion) {
                rend.isVisible = isVisible;
                rend.isInFrustum = isInFrustum;
            }
        }

        mc.theWorld.theProfiler.endStartSection("call_lists");

        {
            int xSort = MathHelper.floor_double(cam.getX());
            int zSort = MathHelper.floor_double(cam.getZ());
            xSort -= xSort & 1023;
            zSort -= zSort & 1023;
            Arrays.sort(allRenderLists, new RenderDistanceSorter(xSort, zSort));
            rg.renderAllRenderLists(pass, tick);
        }
        mc.theWorld.theProfiler.endSection();

        return glListsRendered;
    }

    public void clipRenderersByFrustum(ICamera p_72729_1_, float p_72729_2_) {
        for (int i = 0; i < rg.worldRenderers.length; ++i) {
            if((i + rg.frustumCheckOffset & 15) == 0) {
                WorldRenderer wr = rg.worldRenderers[i];
                IWorldRenderer iwr = (IWorldRenderer) wr;
                if (wr.isInFrustum && iwr.ft$getCullInfo().isFrustumCheckPending) {
                    wr.updateInFrustum(p_72729_1_);
                    iwr.ft$getCullInfo().isFrustumCheckPending = false;
                    if (!wr.isInFrustum) {
                        OcclusionHelpers.worker.dirtyFrustumRenderers++;
                    }
                }
            }
        }

        ++rg.frustumCheckOffset;

        if(rg.frustumCheckOffset % 15 == 0 && OcclusionHelpers.worker.dirtyFrustumRenderers > 0) {
            OcclusionHelpers.worker.dirty = true;
            OcclusionHelpers.worker.dirtyFrustumRenderers = 0;
        }
    }
    
    public void ft$setRendererUpdateOrderProvider(IRendererUpdateOrderProvider orderProvider) {
        this.rendererUpdateOrderProvider = orderProvider;
    }

    public void ft$addRenderGlobalListener(IRenderGlobalListener listener) {
        this.eventListeners.add(listener);
    }

    private static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2);
    }
    
    private static boolean isChunkEmpty(Chunk chunk) {
        return chunk == null || chunk.isEmpty();
    }
}
