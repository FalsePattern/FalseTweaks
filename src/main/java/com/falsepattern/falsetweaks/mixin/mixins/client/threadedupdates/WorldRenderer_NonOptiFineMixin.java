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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.IRendererUpdateResultHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import static com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.kChunkCache;

@Mixin(value = WorldRenderer.class,
       priority = 1001)
public abstract class WorldRenderer_NonOptiFineMixin implements IRendererUpdateResultHolder {
    @Dynamic
    @Shadow(remap = false)
    private int ft$insertNextPass(int pass) {
        return 0;
    }

    @ModifyConstant(method = "updateRenderer",
                    constant = @Constant(intValue = 0,
                                         ordinal = 1),
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/WorldRenderer;vertexState:Lnet/minecraft/client/shader/TesselatorVertexState;",
                                              shift = At.Shift.AFTER,
                                              ordinal = 0)),
                    require = 1)
    private int insertNextPass(int constant, @Local(index = 17) int pass) {
        return ft$insertNextPass(pass);
    }

    @WrapOperation(method = "updateRenderer",
                   at = @At(value = "NEW",
                            target = "(Lnet/minecraft/world/World;IIIIIII)Lnet/minecraft/world/ChunkCache;"),
                   require = 1)
    private ChunkCache killChunkCache0(World world, int x1, int y1, int z1, int x2, int y2, int z2, int extent, Operation<ChunkCache> original) {
        return kChunkCache(world, x1, y1, z1, x2, y2, z2, extent, original);
    }
}
