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

package com.falsepattern.falsetweaks.mixin.mixins.client.vertexapi;

import com.falsepattern.falsetweaks.modules.vertexapi.VertexInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin {
    @ModifyConstant(method = "addVertex",
                    constant = @Constant(intValue = 32),
                    require = 1)
    private int extendAddVertexCap(int constant) {
        return VertexInfo.recomputeVertexInfo(constant >>> 2, 4) + 128;
    }

    @ModifyConstant(method = "addVertex",
                    constant = @Constant(intValue = 8),
                    require = 1)
    private int extendAddVertexStep(int constant) {
        return VertexInfo.recomputeVertexInfo(constant, 1);
    }

    @ModifyConstant(method = "draw",
                    constant = @Constant(intValue = 32),
                    require = 5,
                    expect = 5,
                    // OptiFine
                    allow = 6)   // Vanilla
    private int extendDrawStride(int constant) {
        return VertexInfo.recomputeVertexInfo(constant >>> 2, 4);
    }

    @ModifyConstant(method = "draw",
                    constant = @Constant(intValue = 8),
                    require = 0,
                    expect = 0,
                    // OptiFine
                    allow = 2)   // Vanilla
    private int extendDrawOffset(int constant) {
        return VertexInfo.recomputeVertexInfo(constant, 1);
    }
}
