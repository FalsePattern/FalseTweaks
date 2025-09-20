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

package com.falsepattern.falsetweaks.mixin.mixins.client.cc;

import com.falsepattern.falsetweaks.modules.cc.ChunkCacheFT;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Redirect(method = "updateRenderer",
              at = @At(value = "NEW",
                       target = "(Lnet/minecraft/world/World;IIIIIII)Lnet/minecraft/world/ChunkCache;"),
              require = 1)
    private ChunkCache customChunkCache(World world,
                                        int xMin,
                                        int yMin,
                                        int zMin,
                                        int xMax,
                                        int yMax,
                                        int zMax,
                                        int subIn) {
        return new ChunkCacheFT(world, xMin, yMin, zMin, xMax, yMax, zMax, subIn);
    }

    @Redirect(method = "updateRenderer",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/ChunkCache;extendedLevelsInChunkCache()Z"),
              require = 1)
    private boolean begin(ChunkCache instance, @Share("cc") LocalRef<ChunkCacheFT> cc) {
        if (instance.extendedLevelsInChunkCache()) {
            return true;
        }
        if (instance instanceof ChunkCacheFT) {
            val i = ((ChunkCacheFT) instance);
            cc.set(i);
            i.renderStart();
        }
        return false;
    }

    @Inject(method = "updateRenderer",
            at = @At("RETURN"),
            require = 1)
    private void discard(EntityLivingBase cameraEntity, CallbackInfo ci, @Share("cc") LocalRef<ChunkCacheFT> cc) {
        val chunkCache = cc.get();
        if (chunkCache != null) {
            chunkCache.renderFinish();
        }
    }
}
