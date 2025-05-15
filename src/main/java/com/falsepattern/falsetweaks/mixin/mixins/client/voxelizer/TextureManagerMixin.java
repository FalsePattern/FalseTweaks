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

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Inject(method = "bindTexture",
            at = @At("HEAD"),
            require = 1)
    private void detectEnchantmentGlint(ResourceLocation p_110577_1_, CallbackInfo ci) {
        boolean bound = false;
        if (p_110577_1_ != null) {
            val resPath = p_110577_1_.getResourcePath();
            bound = "textures/misc/enchanted_item_glint.png".equals(resPath);
        }
        Data.enchantmentGlintTextureBound = bound;
    }
}
