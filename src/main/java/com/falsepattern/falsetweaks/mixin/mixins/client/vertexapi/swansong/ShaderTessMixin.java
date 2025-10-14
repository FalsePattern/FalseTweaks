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

package com.falsepattern.falsetweaks.mixin.mixins.client.vertexapi.swansong;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.api.triangulator.VertexAPI;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import com.ventooth.swansong.tessellator.ShaderTess;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;

@Mixin(value = ShaderTess.class,
       remap = false)
public abstract class ShaderTessMixin {
    @Shadow
    @Final
    protected Tessellator tess;
    private boolean trollSwansongAddVertex;

    @ModifyConstant(method = "vertexStrideInt",
                    constant = @Constant(),
                    require = 1)
    private static int recompute(int constant) {
        return VertexAPI.recomputeVertexInfo(constant, 1);
    }

    @Inject(method = "addVertex(DDD)V",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearShaderCheck(CallbackInfo ci) {
        val triTess = (ITriangulatorTessellator) tess;
        triTess.shaderOn(Compat.ShaderType.Swansong);
        if (triTess.hackedQuadRendering()) {
            trollSwansongAddVertex = true;
            if (triTess.quadTriangulationActive()) {
                tess.drawMode = GL11.GL_QUADS;
            }
        }
    }

    @Inject(method = "addVertex(DDD)V",
            at = @At(value = "RETURN"),
            require = 1)
    private void hackVertex(CallbackInfo ci) {
        val triTess = (ITriangulatorTessellator) tess;
        if (trollSwansongAddVertex) {
            tess.drawMode = GL11.GL_TRIANGLES;
            trollSwansongAddVertex = false;
        }
        triTess.triangulate();
        triTess.shaderOn(Compat.ShaderType.None);
    }

    @Inject(method = "draw",
            at = @At(value = "HEAD"),
            require = 1)
    private void stateSwitchingForDrawCallStart(CallbackInfoReturnable<Integer> cir) {
        val triTess = (ITriangulatorTessellator) tess;
        if (triTess.hackedQuadRendering() && trollSwansongAddVertex) {
            tess.drawMode = GL11.GL_TRIANGLES;
        }
    }

    @Inject(method = "draw",
            at = @At(value = "RETURN"),
            require = 1)
    private void stateSwitchingForDrawCallEnd(CallbackInfoReturnable<Integer> cir) {
        val triTess = (ITriangulatorTessellator) tess;
        if (triTess.hackedQuadRendering() && trollSwansongAddVertex) {
            if (triTess.quadTriangulationActive()) {
                tess.drawMode = GL11.GL_QUADS;
            }
        }
    }
}
