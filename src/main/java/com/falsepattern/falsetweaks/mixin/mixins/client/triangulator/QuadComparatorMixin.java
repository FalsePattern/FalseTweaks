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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.IQuadComparatorMixin;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.util.QuadComparator;

@Mixin(QuadComparator.class)
public abstract class QuadComparatorMixin implements IQuadComparatorMixin {
    @Shadow
    private float field_147630_a;

    @Shadow
    private float field_147628_b;

    @Shadow
    private float field_147629_c;

    @Shadow
    private int[] field_147627_d;

    private boolean triMode;

    private boolean shaderMode;

    private static float getCenter(float x, float y, float z, int[] vertexData, int i, int vertexSize) {
        val ax = Float.intBitsToFloat(vertexData[i]) - x;
        val ay = Float.intBitsToFloat(vertexData[i + 1]) - y;
        val az = Float.intBitsToFloat(vertexData[i + 2]) - z;
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]) - x;
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]) - y;
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]) - z;
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]) - x;
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]) - y;
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]) - z;

        val xAvg = (ax + bx + cx) / 3f;
        val yAvg = (ay + by + cy) / 3f;
        val zAvg = (az + bz + cz) / 3f;

        return xAvg * xAvg + yAvg * yAvg + zAvg * zAvg;
    }

    private static int compare(int a, int b, float x, float y, float z, int[] vertexData, int vertexSize) {
        return Float.compare(getCenter(x, y, z, vertexData, b, vertexSize),
                             getCenter(x, y, z, vertexData, a, vertexSize));
    }

    @Inject(method = "compare(Ljava/lang/Integer;Ljava/lang/Integer;)I",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void triCompare(Integer aObj, Integer bObj, CallbackInfoReturnable<Integer> cir) {
        if (!triMode) {
            return;
        }
        cir.setReturnValue(
                compare(aObj, bObj, this.field_147630_a, this.field_147628_b, this.field_147629_c, this.field_147627_d,
                        VertexInfo.recomputeVertexInfo(shaderMode ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1)));
    }

    @ModifyConstant(method = "compare(Ljava/lang/Integer;Ljava/lang/Integer;)I",
                    constant = {@Constant(intValue = 8),
                                @Constant(intValue = 9),
                                @Constant(intValue = 10),
                                @Constant(intValue = 16),
                                @Constant(intValue = 17),
                                @Constant(intValue = 18),
                                @Constant(intValue = 24),
                                @Constant(intValue = 25),
                                @Constant(intValue = 26)},
                    require = 18)
    private int extendOffsets(int constant) {
        return VertexInfo.recomputeVertexInfo(constant / 8, 1) + constant % 8;
    }

    @Override
    public void enableTriMode() {
        triMode = true;
    }

    @Override
    public void enableShaderMode() {
        shaderMode = true;
    }
}
