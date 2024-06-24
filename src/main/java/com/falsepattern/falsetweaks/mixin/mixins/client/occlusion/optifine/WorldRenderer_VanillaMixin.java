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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.AxisAlignedBB;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderer_VanillaMixin {
    @Redirect(method = "setPosition",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V",
                       remap = false),
              require = 1)
    private void noNewList(int list, int mode) {

    }

    @Redirect(method = "setPosition",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/AxisAlignedBB;getBoundingBox(DDDDDD)Lnet/minecraft/util/AxisAlignedBB;"),
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V",
                                        remap = false)),
              require = 1)
    private AxisAlignedBB noCreateAABB(double p_72330_0_, double p_72330_2_, double p_72330_4_, double p_72330_6_, double p_72330_8_, double p_72330_10_) {
        return null;
    }

    @Redirect(method = "setPosition",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderAABB(Lnet/minecraft/util/AxisAlignedBB;)V"),
              require = 1)
    private void noAABB(AxisAlignedBB axisAlignedBB) {

    }

    @Redirect(method = "setPosition",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glEndList()V",
                       remap = false),
              require = 1)
    private void noEndList() {

    }
}
