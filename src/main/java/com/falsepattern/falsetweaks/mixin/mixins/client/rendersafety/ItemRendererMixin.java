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

package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.falsepattern.falsetweaks.modules.rendersafety.SafetyUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @WrapOperation(method = "renderItemInFirstPerson",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraftforge/client/IItemRenderer;renderItem(Lnet/minecraftforge/client/IItemRenderer$ItemRenderType;Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V"),
                   require = 0,
                   expect = 1)
    private void wrapRenderItem(IItemRenderer instance, IItemRenderer.ItemRenderType itemRenderType, ItemStack itemStack, Object[] objects, Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_ITEM;
        SafetyUtil.pre(enable);
        original.call(instance, itemRenderType, itemStack, objects);
        SafetyUtil.post(enable);
    }
}
