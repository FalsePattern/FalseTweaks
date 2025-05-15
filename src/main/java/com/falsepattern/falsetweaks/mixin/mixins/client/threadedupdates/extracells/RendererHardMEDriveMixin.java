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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.extracells;

import extracells.render.block.RendererHardMEDrive$;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;


@Mixin(value = RendererHardMEDrive$.class,
       remap = false)
public abstract class RendererHardMEDriveMixin implements ISimpleBlockRenderingHandler {
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
                       target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"),
              require = 1)
    private void skipGLDisable(int cap) {
    }

    // TODO: As we don't bind the correct texture, the inserted drives do not render correctly.
    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
                       remap = true),
              require = 2)
    private void skipBindTexture(TextureManager instance, ResourceLocation p_110577_1_) {
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"),
              require = 1)
    private void skipGLPopMatrix() {
    }

    @Redirect(method = {"renderWorldBlock", "renderXPos", "renderXNeg", "renderZPos", "renderZNeg"},
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                       remap = true),
              require = 9)
    private int skipTessellatorDraw(Tessellator instance) {
        return 0;
    }

    @Redirect(method = {"renderWorldBlock", "renderXPos", "renderXNeg", "renderZPos", "renderZNeg"},
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                       remap = true),
              require = 9)
    private void skipTessellatorStartDrawingQuads(Tessellator instance) {
    }
}
