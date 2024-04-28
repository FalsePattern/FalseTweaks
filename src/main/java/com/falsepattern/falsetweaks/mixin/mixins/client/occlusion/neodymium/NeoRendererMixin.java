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
package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.neodymium;

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionCompat;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.occlusion.WorldRendererOcclusion;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import makamys.neodymium.renderer.Mesh;
import makamys.neodymium.renderer.NeoRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.WorldRenderer;

@Mixin(value = NeoRenderer.class,
       remap = false)
public abstract class NeoRendererMixin {
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
    @Dynamic
    private boolean isRendererVisible(WorldRenderer wr, boolean shadowPass) {
        if (shadowPass) {
            return ((WorldRendererOcclusion)wr).ft$isVisibleShadows();
        } else {
            return wr.isVisible;
        }
    }
}
