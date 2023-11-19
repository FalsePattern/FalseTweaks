/*
 * This file is part of FalseTweaks.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator.optifine;

import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITessellatorMixin;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorVanillaMixin implements ITessellatorMixin {
    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/Math;min(II)I",
                       ordinal = 0),
              require = 1)
    private int snapTo3(int a, int b) {
        val v = Math.min(a, b);
        if (drawingTris()) {
            return v - (v % 3);
        } else {
            return v;
        }
    }
}
