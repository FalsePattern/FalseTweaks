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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.automagy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tuhljin.automagy.renderers.RenderBlockGlowOverlay;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

@Mixin(value = RenderBlockGlowOverlay.class,
       remap = false)
public abstract class RenderBlockGlowOverlayMixin implements ISimpleBlockRenderingHandler {
    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"),
              require = 1)
    private void skipGLPushMatrix() {
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"),
              require = 1)
    private void skipGLEnable(int cap) {
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"),
              require = 1)
    private void skipSetBlendFunction(int sfactor, int dfactor) {
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"),
              require = 1)
    private void skipGLDisable(int cap) {
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"),
              require = 1)
    private void skipGLPopMatrix() {
    }
}
