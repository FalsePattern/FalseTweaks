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

//keep in sync with TessellatorVanillaMixin
package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator.foamfix;

import com.falsepattern.falsetweaks.mixin.helper.ITessellatorMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.Tessellator;

import java.util.Comparator;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(Tessellator.class)
public abstract class TessellatorFoamFixMixin implements ITessellatorMixin {
    @ModifyArg(method = {"getVertexState_foamfix_old"},
               at = @At(value = "INVOKE",
                        target = "Ljava/util/PriorityQueue;<init>(ILjava/util/Comparator;)V",
                        remap = false),
               index = 1,
               remap = false,
               require = 1)
    private Comparator<?> hackQuadComparator_MIXIN(Comparator<?> comparator) {
        return hackQuadComparator(comparator);
    }

    //Intvalue 72 is for optifine compat
    @ModifyConstant(method = "getVertexState_foamfix_old",
                    constant = {@Constant(intValue = 32), @Constant(intValue = 72)},
                    require = 1)
    private int hackQuadCounting_MIXIN(int constant) {
        return hackQuadCounting(constant);
    }
}
