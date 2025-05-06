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

package com.falsepattern.falsetweaks.mixin.mixins.client.cc.of;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@Mixin(targets = "ChunkCacheOF",
       remap = false)
public abstract class ChunkCacheOF_NonShader_DevMixin extends ChunkCache {
    public ChunkCacheOF_NonShader_DevMixin(World p_i1964_1_, int p_i1964_2_, int p_i1964_3_, int p_i1964_4_, int p_i1964_5_, int p_i1964_6_, int p_i1964_7_, int p_i1964_8_) {
        super(p_i1964_1_, p_i1964_2_, p_i1964_3_, p_i1964_4_, p_i1964_5_, p_i1964_6_, p_i1964_7_, p_i1964_8_);
    }

    @Dynamic
    @Redirect(method = "getLightBrightnessForSkyBlocks",
              at = @At(value = "INVOKE",
                       target = "LConfig;isDynamicLights()Z",
                       remap = false),
              remap = false,
              expect = 0,
              require = 0)
    private boolean ftDynamicLightsDev() {
        return DynamicLightsDrivers.frontend.enabled();
    }

    @Dynamic
    @Redirect(method = "getLightBrightnessForSkyBlocks",
              at = @At(value = "INVOKE",
                       target = "LDynamicLights;getCombinedLight(IIII)I",
                       remap = false),
              remap = false,
              expect = 0,
              require = 0)
    private int ftCombinedLightsDev(int x, int y, int z, int combinedLight) {
        return DynamicLightsDrivers.frontend.forWorldMesh().getCombinedLight(x, y, z, combinedLight);
    }

    @Dynamic
    @Redirect(method = "getLightBrightnessForSkyBlocks",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/IBlockAccess;getLightBrightnessForSkyBlocks(IIII)I",
                       remap = true),
              remap = false,
              expect = 0,
              require = 0)
    private int brightnessFromSuperDev(IBlockAccess instance, int x, int y, int z, int lightValue) {
        return super.getLightBrightnessForSkyBlocks(x, y, z, lightValue);
    }

    @Dynamic
    @Redirect(method = "getBlock",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;",
                       remap = true),
              remap = false,
              expect = 0,
              require = 0)
    private Block blockFromSuperDev(IBlockAccess instance, int x, int y, int z) {
        return super.getBlock(x, y, z);
    }
}
