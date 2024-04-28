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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.neodymium;

import com.falsepattern.falsetweaks.modules.threadedupdates.NeodymiumCompat;
import com.falsepattern.falsetweaks.modules.threadedupdates.NeodymiumWorldRendererThreadingBridge;
import com.google.common.collect.Lists;
import makamys.neodymium.renderer.ChunkMesh;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;

import static com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.AGGRESSIVE_NEODYMIUM_THREADING;

@Mixin(value = WorldRenderer.class,
       priority = 1001)
public abstract class WorldRendererMixin implements NeodymiumWorldRendererThreadingBridge {
    @Dynamic
    @Shadow(remap = false)
    private List<ChunkMesh> nd$chunkMeshes;

    @Override
    public void ft$ensureNeodymiumChunkMeshesArraylistPresent() {
        if (nd$chunkMeshes == null) {
            nd$chunkMeshes = Lists.newArrayList(null, null);
        }
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "HEAD"),
            require = 1)
    private void setLastWorldRendererSingleton(CallbackInfo ci) {
        if (AGGRESSIVE_NEODYMIUM_THREADING) {
            NeodymiumCompat.setSuppressRenderPasses((WorldRenderer) (Object) this, true);
        }
    }

    @Inject(method = "updateRenderer",
            at = @At("RETURN"),
            require = 1)
    private void finishUpdate(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (AGGRESSIVE_NEODYMIUM_THREADING) {
            NeodymiumCompat.setSuppressRenderPasses((WorldRenderer) (Object) this, false);
        }
    }
}
