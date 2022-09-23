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

//Keep in sync with TessellatorVanillaMixin
package com.falsepattern.falsetweaks.mixin.mixins.client.optifine;

import com.falsepattern.falsetweaks.mixin.helper.ITessellatorMixin;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorOptiFineMixin implements ITessellatorMixin {

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
        if (hackedQuadRendering()) {
            shaderOn(true);
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
