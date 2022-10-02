/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelRenderHelper;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderItemInFirstPerson",
            at = @At("HEAD"),
            require = 1)
    private void startManagedMode(float p_78440_1_, CallbackInfo ci) {
        Data.setManagedMode(true);
    }

    @Inject(method = "renderItemInFirstPerson",
            at = @At("RETURN"),
            require = 1)
    private void endManagedMode(float p_78440_1_, CallbackInfo ci) {
        Data.setManagedMode(false);
    }

    @Inject(method = "renderItemIn2D",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private static void forceVoxelized(Tessellator p_78439_0_, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, int p_78439_5_, int p_78439_6_, float p_78439_7_, CallbackInfo ci) {
        val lastUsed = Data.getLastUsedSprite();
        if (lastUsed != null && Data.isManagedMode()) {
            val glint = Data.enchantmentGlintTextureBound;
            VoxelRenderHelper.renderItemVoxelized(lastUsed);
            if (!glint) {
                Data.incrementCurrentItemLayer();
            }
            ci.cancel();
        }
    }
}
