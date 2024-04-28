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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;

@Mixin(RenderGlobal.class)
public abstract class SkyFix_RenderGlobalMixin {
    @ModifyConstant(method = "<init>",
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;")),
                    constant = @Constant(intValue = 256,
                                         ordinal = 0),
                    require = 1)
    private int modifySkySize(int constant) {
        return 2048;
    }

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "FIELD",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;")),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                       ordinal = 0),
              require = 1)
    private void noDrawQuads1(Tessellator instance) {

    }

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "FIELD",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;")),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                       ordinal = 0),
              require = 1)
    private int noDrawQuads2(Tessellator instance) {
        return 0;
    }

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "FIELD",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;")),
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V",
                       ordinal = 0),
              require = 1)
    private void bigDraw1Start(int list, int mode) {
        GL11.glNewList(list, mode);
        Tessellator.instance.startDrawingQuads();
    }

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "FIELD",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;")),
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glEndList()V",
                       ordinal = 0),
              require = 1)
    private void bigDraw1End() {
        Tessellator.instance.draw();
        GL11.glEndList();
    }
}
