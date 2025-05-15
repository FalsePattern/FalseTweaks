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

package com.falsepattern.falsetweaks.modules.cc;

import com.falsepattern.falsetweaks.api.dynlights.FTDynamicLights;
import com.falsepattern.falsetweaks.modules.dynlights.ArrayCache;
import lombok.val;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public class ChunkCacheFT extends ChunkCache {
    private int posX;
    private int posY;
    private int posZ;
    private int[] combinedLights;
    private Block[] blocks;
    private static final ArrayCache<int[]> cacheCombinedLights = new ArrayCache<>(Integer.TYPE, 16);
    private static final ArrayCache<Block[]> cacheBlocks = new ArrayCache<>(Block.class, 16);
    private static final int ARRAY_SIZE = 8000;

    public ChunkCacheFT(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int subIn) {
        super(world, xMin, yMin, zMin, xMax, yMax, zMax, subIn);
        this.posX = xMin - subIn;
        this.posY = yMin - subIn;
        this.posZ = zMin - subIn;
    }

    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        if (this.combinedLights == null) {
            return this.getLightBrightnessForSkyBlocksRaw(x, y, z, lightValue);
        } else {
            int index = this.getPositionIndex(x, y, z);
            if (index >= 0 && index < this.combinedLights.length) {
                int light = this.combinedLights[index];
                if (light == -1) {
                    light = this.getLightBrightnessForSkyBlocksRaw(x, y, z, lightValue);
                    this.combinedLights[index] = light;
                }

                return light;
            } else {
                return this.getLightBrightnessForSkyBlocksRaw(x, y, z, lightValue);
            }
        }
    }

    private int getLightBrightnessForSkyBlocksRaw(int x, int y, int z, int lightValue) {
        int light = super.getLightBrightnessForSkyBlocks(x, y, z, lightValue);
        val dl = FTDynamicLights.frontend();
        if (dl.enabled() && !this.getBlock(x, y, z).isOpaqueCube()) {
            light = dl.forWorldMesh().getCombinedLight(x, y, z, light);
        }

        return light;
    }

    public Block getBlock(int x, int y, int z) {
        if (this.blocks == null) {
            return super.getBlock(x, y, z);
        } else {
            int index = this.getPositionIndex(x, y, z);
            if (index >= 0 && index < this.blocks.length) {
                Block block = this.blocks[index];
                if (block == null) {
                    block = super.getBlock(x, y, z);
                    this.blocks[index] = block;
                }

                return block;
            } else {
                return super.getBlock(x, y, z);
            }
        }
    }

    private int getPositionIndex(int x, int y, int z) {
        int i = x - this.posX;
        int j = y - this.posY;
        int k = z - this.posZ;
        return i >= 0 && j >= 0 && k >= 0 && i < 20 && j < 20 && k < 20 ? i * 400 + k * 20 + j : -1;
    }

    public void renderStart() {
        if (this.combinedLights == null) {
            this.combinedLights = cacheCombinedLights.allocate(ARRAY_SIZE);
        }

        Arrays.fill(this.combinedLights, -1);
        if (this.blocks == null) {
            this.blocks = cacheBlocks.allocate(ARRAY_SIZE);
        }

        Arrays.fill(this.blocks, null);
    }

    public void renderFinish() {
        cacheCombinedLights.free(this.combinedLights);
        this.combinedLights = null;
        cacheBlocks.free(this.blocks);
        this.blocks = null;
    }
}
