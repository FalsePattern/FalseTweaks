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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

@Mixin(ItemRenderer.class)
public abstract class OverlayCrashFix_ItemRendererMixin {
    @Shadow protected abstract void renderInsideOfBlock(float partialTickTime, IIcon blockTextureIndex);

    @Redirect(method = "renderOverlays",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/ItemRenderer;renderInsideOfBlock(FLnet/minecraft/util/IIcon;)V"),
              require = 0)
    private void guardRenderInsideOfBlock(ItemRenderer instance, float partialTickTime, IIcon blockTextureIndex) {
        if (blockTextureIndex == null) {
            blockTextureIndex = Blocks.stone.getBlockTextureFromSide(2);
            if (blockTextureIndex == null) {
                return;
            }
        }
        ((OverlayCrashFix_ItemRendererMixin)(Object)instance).renderInsideOfBlock(partialTickTime, blockTextureIndex);
    }
}
