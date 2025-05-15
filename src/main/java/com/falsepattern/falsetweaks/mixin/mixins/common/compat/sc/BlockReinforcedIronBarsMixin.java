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

package com.falsepattern.falsetweaks.mixin.mixins.common.compat.sc;

import net.geforcemods.securitycraft.blocks.reinforced.BlockReinforcedFenceGate;
import net.geforcemods.securitycraft.blocks.reinforced.BlockReinforcedIronBars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;

@Mixin(BlockReinforcedIronBars.class)
public abstract class BlockReinforcedIronBarsMixin extends BlockPane {
    protected BlockReinforcedIronBarsMixin(String p_i45432_1_, String p_i45432_2_, Material p_i45432_3_, boolean p_i45432_4_) {
        super(p_i45432_1_, p_i45432_2_, p_i45432_3_, p_i45432_4_);
    }

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/ObfuscationReflectionHelper;setPrivateValue(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;I)V"),
              require = 0,
              expect = 0)
    private <T, E> void fixPriv(Class<T> classToAccess, T instance, E value, int index) {
        this.stepSound = soundTypeMetal;
    }
}
