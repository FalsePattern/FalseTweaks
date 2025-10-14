/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.modules.triangulator.TriComparator;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import com.falsepattern.falsetweaks.modules.vertexapi.VertexInfo;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.util.QuadComparator;

@Mixin(value = Tessellator.class,
       //Hodgepodge compat
       priority = 900)
public abstract class TessellatorMixin_RequireFoamFix implements ITriangulatorTessellator {
    @Dynamic
    @Redirect(method = "getVertexState_foamfix_old",
              at = @At(value = "NEW",
                       target = "([IFFF)Lnet/minecraft/client/util/QuadComparator;"),
              require = 1)
    private QuadComparator hackQuadComparator(int[] vertexData, float x, float y, float z) {
        if (this.drawingTris()) {
            return new TriComparator(vertexData, x, y, z);
        } else {
            return new QuadComparator(vertexData, x, y, z);
        }
    }
}
