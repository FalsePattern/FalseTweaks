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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

@Pseudo
@Mixin(targets = "ItemRendererOF",
       remap = false)
@SuppressWarnings("UnresolvedMixinReference")
public abstract class OFItemRendererMixin {
    @Inject(method = "func_78443_a(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            require = 1)
    private void startManagedMode(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        Data.setManagedMode(true);
    }

    @Inject(method = "func_78443_a(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("RETURN"),
            require = 1)
    private void endManagedMode(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        Data.setManagedMode(false);
    }
}
