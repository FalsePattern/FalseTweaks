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

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.MathHelper;

@RequiredArgsConstructor
public class SpatialRenderStore {
    private final OcclusionRenderer rend;
    private boolean populated = false;
    private int lastPosChunkX;
    private int lastPosChunkZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;

    /**
     * Order:
     * [X][Z][Y]
     */
    private WorldRenderer[][][] spatialRendererStore;

    @SuppressWarnings("CommentedOutCode") // Removed checks for performance
    private static <T> void rotate(T[] array, int delta) {
        //        if (delta < -1 || delta > 1)
        //            throw new IllegalArgumentException("Can only rotate array by single steps!");

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

    public void repositionSmart(RenderGlobal rg, CameraInfo cam) {
        val prevX = rg.prevChunkSortX;
        val prevZ = rg.prevChunkSortZ;
        val camX = MathHelper.floor_double(cam.getX());
        val camY = MathHelper.floor_double(cam.getY());
        val camZ = MathHelper.floor_double(cam.getZ());
        val currX = cam.getChunkCoordX();
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
            updateRendererNeighborsFull(rg);
            OcclusionHelpers.worker.run(true);
        } else {
            if (deltaX != 0) {
                int dRaw = deltaX < 0 ? -1 : 1;
                int dAbs = Math.abs(deltaX);
                for (int i = 0; i < dAbs; i++) {
                    repositionDeltaX(rg, dRaw);
                }
            }
            if (deltaZ != 0) {
                int dRaw = deltaZ < 0 ? -1 : 1;
                int dAbs = Math.abs(deltaZ);
                for (int i = 0; i < dAbs; i++) {
                    repositionDeltaZ(rg, dRaw);
                }
            }
            OcclusionHelpers.worker.run(true);
        }

        lastPosChunkX = rg.prevChunkSortX = currX;
        lastPosChunkZ = rg.prevChunkSortZ = currZ;
    }

    public void updateRendererNeighborsFull(RenderGlobal rg) {
        if (rg.worldRenderers == null) {
            return;
        }
        for (int i = 0; i < rg.worldRenderers.length; i++) {
            WorldRenderer rend = rg.worldRenderers[i];
            OcclusionWorker.CullInfo ci = ((WorldRendererOcclusion) rend).ft$getCullInfo();
            ci.wrIdx = i;
        }
    }

    public void reset(RenderGlobal rg) {
        populated = false;
        updateRendererNeighborsFull(rg);
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
            rend.setPositionAndMarkInvisible(edgeSubChunk, prevEdgeSubChunk.posX + deltaX, prevEdgeSubChunk.posY, prevEdgeSubChunk.posZ + deltaZ);
        }
    }

    @SuppressWarnings("CommentedOutCode") // Removed checks for performance
    private void rebuidSpatialRenderStore(RenderGlobal rg, int xSize, int ySize, int zSize, int camX, int camZ) {
        spatialRendererStore = new WorldRenderer[xSize][zSize][ySize];
        val baseX = camX - (xSize + 1) * 8;
        val baseZ = camZ - (zSize + 1) * 8;
        for (int i = 0; i < rg.worldRenderers.length; i++) {
            val wr = rg.worldRenderers[i];
            var wrcX = (wr.posX - baseX) >> 4;
            var wrcY = wr.posY >> 4;
            var wrcZ = (wr.posZ - baseZ) >> 4;

            //if (spatialRendererStore[wrcX]
            //            [wrcZ]
            //            [wrcY] != null) {
            //    System.err.println("WUT @" + wrcX + "," + wrcY + "," + wrcZ);
            //}
            spatialRendererStore[wrcX][wrcZ][wrcY] = wr;

        }
        populated = true;
        sizeX = xSize;
        sizeY = ySize;
        sizeZ = zSize;
    }
}
