/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

//Keep in sync with TessellatorVanillaMixin
package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator.optifine;

import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorOptiFineMixin implements ITriangulatorTessellator {

    @Shadow
    private int drawMode;

    private boolean trollOptifineAddVertex;

    @Inject(method = "addVertex",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearShaderCheck(CallbackInfo ci) {
        shaderOn(false);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = "addVertex",
            at = @At(value = "INVOKE",
                     target = "Lshadersmod/client/ShadersTess;addVertex(Lnet/minecraft/client/renderer/Tessellator;DDD)V"),
            require = 1)
    private void shaderAddVertex(CallbackInfo ci) {
        shaderOn(true);
        if (hackedQuadRendering()) {
            trollOptifineAddVertex = true;
            if (quadTriangulationActive()) {
                drawMode = GL11.GL_QUADS;
            }
        }
    }

    @Inject(method = "draw",
            at = @At(value = "HEAD"),
            require = 1)
    private void stateSwitchingForDrawCallStart(CallbackInfoReturnable<Integer> cir) {
        if (hackedQuadRendering() && trollOptifineAddVertex) {
            drawMode = GL11.GL_TRIANGLES;
        }
    }

    @Inject(method = "draw",
            at = @At(value = "RETURN"),
            require = 1)
    private void stateSwitchingForDrawCallEnd(CallbackInfoReturnable<Integer> cir) {
        if (hackedQuadRendering() && trollOptifineAddVertex) {
            if (quadTriangulationActive()) {
                drawMode = GL11.GL_QUADS;
            }
        }
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @ModifyConstant(method = "addVertex",
                    constant = @Constant(intValue = 4),
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/Tessellator;vertexCount:I")),
                    require = 1)
    private int fixNoExpandAlignment(int constant) {
        return quadTriangulationActive() ? 3 : 4;
    }

    @Inject(method = "addVertex",
            at = @At(value = "RETURN"),
            require = 1)
    private void hackVertex(CallbackInfo ci) {
        if (trollOptifineAddVertex) {
            drawMode = GL11.GL_TRIANGLES;
            trollOptifineAddVertex = false;
        }
        triangulate();
        shaderOn(false);
    }
}
