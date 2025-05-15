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
package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.neodymium;

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionCompat;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.occlusion.WorldRendererOcclusion;
import com.falsepattern.falsetweaks.modules.occlusion.shader.ShadowPassOcclusionHelper;
import lombok.val;
import makamys.neodymium.renderer.ChunkMesh;
import makamys.neodymium.renderer.GPUMemoryManager;
import makamys.neodymium.renderer.NeoRegion;
import makamys.neodymium.renderer.NeoRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.WorldRenderer;

import java.util.List;

@Mixin(value = NeoRenderer.class,
       remap = false)
public abstract class NeoRendererMixin {
    @Shadow private List<GPUMemoryManager> mems;

    @Shadow private List<NeoRegion> loadedRegionsList;

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL30;glBindVertexArray(I)V",
                     shift = At.Shift.AFTER),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lorg/lwjgl/opengl/GL14;glMultiDrawArrays(ILjava/nio/IntBuffer;Ljava/nio/IntBuffer;)V")),
            require = 1)
    private void postRender(int pass, double alpha, CallbackInfoReturnable<Integer> cir) {
        OcclusionHelpers.renderer.runOcclusionCheck(OcclusionCompat.OptiFineCompat.isShadowPass(), pass);
    }

    /**
     * @author FalsePattern
     * @reason Compat
     */
    @Overwrite
    private boolean isRendererVisible(WorldRenderer wr, boolean shadowPass) {
        if (!shadowPass) {
            return wr.isVisible;
        }
        if (!((WorldRendererOcclusion) wr).ft$isVisibleShadows()) {
            return false;
        }
        return ShadowPassOcclusionHelper.isShadowVisible(wr);
    }

    @Inject(method = "initIndexBuffers",
            at = @At("HEAD"),
            require = 1)
    private void initIndexBuffers(boolean shadowPass, CallbackInfo ci) {
        if (!shadowPass)
            return;
        ShadowPassOcclusionHelper.begin();
        int regionsSize = loadedRegionsList.size();
        for (val mem: mems) {
            for (int regionI = 0; regionI < regionsSize; regionI++) {
                val region = loadedRegionsList.get(regionI).getRenderData(mem);
                for (val mesh: region.getSentMeshes()) {
                    val wr = ((ChunkMesh)mesh).wr();
                    if (wr.isVisible && wr.isInFrustum) {
                        ShadowPassOcclusionHelper.addShadowReceiver(wr);
                    }
                }
            }
        }
        ShadowPassOcclusionHelper.end();
    }
}
