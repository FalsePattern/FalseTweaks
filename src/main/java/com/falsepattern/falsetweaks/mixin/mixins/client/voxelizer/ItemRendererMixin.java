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

import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelRenderHelper;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    private ItemStack itemToRender;

    @Inject(method = "renderItemIn2D",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private static void forceVoxelized(Tessellator p_78439_0_, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, int p_78439_5_, int p_78439_6_, float p_78439_7_, CallbackInfo ci) {
        val lastUsed = Data.getLastUsedSprite();
        if (lastUsed != null && Data.isManagedMode() && !VoxelizerConfig.isExcluded(lastUsed.getIconName())) {
            val glint = Data.enchantmentGlintTextureBound;
            VoxelRenderHelper.renderItemVoxelized(lastUsed);
            if (!glint) {
                Data.incrementCurrentItemLayer();
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void startManagedMode1(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type, CallbackInfo ci) {
        if (p_78443_2_ == null) {
            return;
        }
        val item = p_78443_2_.getItem();
        if (item == null) {
            return;
        }
        if (!VoxelizerConfig.isClassExcluded(item.getClass())) {
            Data.setManagedMode(true);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            require = 1)
    private void startManagedMode2(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        if (p_78443_2_ == null) {
            return;
        }
        val item = p_78443_2_.getItem();
        if (item == null) {
            return;
        }
        if (!VoxelizerConfig.isClassExcluded(item.getClass())) {
            Data.setManagedMode(true);
        }
    }

    @Inject(method = "renderItemInFirstPerson",
            at = @At("HEAD"),
            require = 1)
    private void startManagedMode3(float p_78440_1_, CallbackInfo ci) {
        if (itemToRender == null) {
            return;
        }
        val item = itemToRender.getItem();
        if (item == null) {
            return;
        }
        if (!VoxelizerConfig.isClassExcluded(item.getClass())) {
            Data.setManagedMode(true);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void endManagedMode1(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type, CallbackInfo ci) {
        Data.setManagedMode(false);
    }

    @Inject(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("RETURN"),
            require = 1)
    private void endManagedMode2(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        Data.setManagedMode(false);
    }

    @Inject(method = "renderItemInFirstPerson",
            at = @At("RETURN"),
            require = 1)
    private void endManagedMode3(float p_78440_1_, CallbackInfo ci) {
        Data.setManagedMode(false);
    }
}
