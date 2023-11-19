/*
 * This file is part of FalseTweaks.
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {

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

}
