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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator.foamfix;

import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITessellatorMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.util.QuadComparator;

@Mixin(Tessellator.class)
public abstract class TessellatorVanillaMixin implements ITessellatorMixin {
    @Redirect(method = "getVertexState",
              at = @At(value = "NEW",
                       target = "([IFFF)Lnet/minecraft/client/util/QuadComparator;"),
              require = 1)
    private QuadComparator customComparator(int[] vertices, float playerX, float playerY, float playerZ) {
        return createQuadComparator(vertices, playerX, playerY, playerZ);
    }

    //Intvalue 72 is for optifine compat
    @ModifyConstant(method = "getVertexState",
                    constant = {@Constant(intValue = 32), @Constant(intValue = 72)},
                    require = 1)
    private int hackQuadCounting_MIXIN(int constant) {
        return hackQuadCounting(constant);
    }
}
