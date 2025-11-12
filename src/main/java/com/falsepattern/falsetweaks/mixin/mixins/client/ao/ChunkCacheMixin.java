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

package com.falsepattern.falsetweaks.mixin.mixins.client.ao;

import com.falsepattern.falsetweaks.modules.ao.AOChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;

@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin implements AOChunkCache {
    @Unique
    private boolean ft$useNeighborBrightness = true;
    @Override
    public void ft$setUseNeighborBrightness(boolean value) {
        ft$useNeighborBrightness = value;
    }

    @Redirect(method = "getSkyBlockTypeBrightness",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getUseNeighborBrightness()Z"),
              require = 1)
    private boolean bypassUseNeighborBrightness(Block instance) {
        return ft$useNeighborBrightness && instance.getUseNeighborBrightness();
    }
}
