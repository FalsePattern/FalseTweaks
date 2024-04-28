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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {

    @Shadow
    private float farPlaneDistance;

    /**
     * @reason RenderGlobalMixin#performCullingUpdates needs to know the chunk update deadline and the partial tick time
     */
    @Inject(method = "renderWorld",
            at = @At("HEAD"),
            require = 1)
    private void getRendererUpdateDeadline(float partialTickTime, long chunkUpdateDeadline, CallbackInfo ci) {
        OcclusionHelpers.chunkUpdateDeadline = chunkUpdateDeadline;
        OcclusionHelpers.partialTickTime = partialTickTime;
    }

    @Redirect(method = "setupCameraTransform",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       ordinal = 0,
                       target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F"),
              require = 1)
    private void reduceFarPlaneDist(EntityRenderer instance, float value) {
        farPlaneDistance = value - 48f;
    }

}
