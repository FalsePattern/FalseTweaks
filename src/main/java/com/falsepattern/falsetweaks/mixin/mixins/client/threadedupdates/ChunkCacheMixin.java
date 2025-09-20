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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import lombok.val;
import lombok.var;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin {
    @Shadow
    private World worldObj;

    @Redirect(method = "getTileEntity",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;func_150806_e(III)Lnet/minecraft/tileentity/TileEntity;"),
              require = 1)
    private TileEntity getTileEntityNoCreate(Chunk chunk, int x, int y, int z) {
        ChunkPosition pos = new ChunkPosition(x, y, z);
        var tile = chunk.chunkTileEntityMap.get(pos);
        if (tile == null) {
            val block = chunk.getBlock(x, y, z);
            val meta = chunk.getBlockMetadata(x, y, z);
            if (!block.hasTileEntity(meta)) {
                return null;
            }
            tile = block.createTileEntity(worldObj, meta);
            if (tile != null) {
                tile.setWorldObj(worldObj);
                tile.xCoord = chunk.xPosition * 16 + x;
                tile.yCoord = y;
                tile.zCoord = chunk.zPosition * 16 + z;
                tile.invalidate();
            }
        }
        return tile;
    }
}
