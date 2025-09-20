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

import com.falsepattern.falsetweaks.modules.misc.ItemTexturingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.entity.RenderItem;

@Mixin(RenderItem.class)
public class FastItemTexturing_RenderItemMixin {
    @Redirect(method = "doRender(Lnet/minecraft/entity/item/EntityItem;DDDFF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureUtil;func_152777_a(ZZF)V"),
              require = 1)
    private void setupTextureFlags(boolean a, boolean b, float c) {
        ItemTexturingManager.setTexFlag(a, b, c);
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/item/EntityItem;DDDFF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureUtil;func_147945_b()V"),
              require = 1)
    private void resetTextureFlags() {

    }
}
