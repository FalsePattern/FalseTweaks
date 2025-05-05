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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.api.triangulator.ToggleableTessellator;
import com.falsepattern.falsetweaks.modules.triangulator.ToggleableTessellatorManager;
import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;

import java.util.Arrays;

@Mixin(Tessellator.class)
@Accessors(fluent = true,
           chain = false)
public abstract class TessellatorMixin implements ITriangulatorTessellator, ToggleableTessellator {
    @Shadow
    @Final
    public static Tessellator instance;
    // This field has an odd name because of optifine compat (cAnNoT aLiAs NoN-pRiVaTe MeMbEr -- SpongePowered Mixins)
    @Shadow(aliases = {"rawBufferSize"},
            remap = false)
    public int field_78388_E;
    @Shadow
    private int drawMode;
    @Shadow
    private int[] rawBuffer;
    @Shadow
    private int rawBufferIndex;
    @Shadow
    private int vertexCount;
    private boolean hackedQuadRendering = false;
    @Getter
    private boolean drawingTris = false;
    @Getter
    @Setter
    private boolean alternativeTriangulation = false;
    private boolean quadTriangulationTemporarilySuspended = false;
    private boolean shaderOn = false;
    private int forceQuadRendering = 0;
    private int quadVerticesPutIntoBuffer = 0;
    @Getter
    @Setter
    private int pass;

    @Inject(method = "reset",
            at = @At(value = "HEAD"),
            require = 1)
    private void resetState(CallbackInfo ci) {
        drawingTris = false;
        hackedQuadRendering = false;
        quadTriangulationTemporarilySuspended = false;
        alternativeTriangulation = false;
        quadVerticesPutIntoBuffer = 0;
    }

    @Redirect(method = "startDrawing",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/renderer/Tessellator;drawMode:I"),
              require = 1)
    private void forceDrawingTris(Tessellator instance, int value) {
        if (!isTriangulatorDisabled() && value == GL11.GL_QUADS) {
            hackedQuadRendering = true;
            value = GL11.GL_TRIANGLES;
        } else {
            hackedQuadRendering = false;
        }
        if (value == GL11.GL_TRIANGLES) {
            drawingTris = true;
        }
        drawMode = value;
    }

    @Override
    public boolean hackedQuadRendering() {
        return hackedQuadRendering;
    }

    @Override
    public boolean quadTriangulationActive() {
        return !quadTriangulationTemporarilySuspended;
    }

    @Override
    public void suspendQuadTriangulation() {
        quadTriangulationTemporarilySuspended = true;
        if (quadVerticesPutIntoBuffer != 0) {
            Share.log.error(new RuntimeException("Someone suspended triangulation while the tessellator had a partially rendered quad! Stacktrace: "));
            quadVerticesPutIntoBuffer = 0;
        }
    }

    @Override
    public void triangulate() {
        if (hackedQuadRendering) {
            fixAOTriangles();
        } else if (drawMode == GL11.GL_QUADS) {
            fixAOQuad();
        }
    }

    private void ensureRawBufferHasExtraSpaceFor(int numberOfInts) {
        int newIndex = rawBufferIndex + numberOfInts;
        if (newIndex >= field_78388_E) {
            if (field_78388_E < 0x10000) {
                field_78388_E = 0x10000;
            }
            while (newIndex >= field_78388_E) {
                field_78388_E *= 2;
            }
            rawBuffer = Arrays.copyOf(rawBuffer, field_78388_E);
        }
    }

    private void fixAOTriangles() {
        if (quadTriangulationTemporarilySuspended) {
            return;
        }
        quadVerticesPutIntoBuffer++;
        if (quadVerticesPutIntoBuffer == 4) {
            int vertexSize = VertexInfo.recomputeVertexInfo(shaderOn() ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1);
            ensureRawBufferHasExtraSpaceFor(2 * vertexSize);
            quadVerticesPutIntoBuffer = 0;
            //Current vertex layout: ABCD
            if (alternativeTriangulation) {
                //Target vertex layout: ABD DBC
                System.arraycopy(rawBuffer, rawBufferIndex - (3 * vertexSize), rawBuffer, rawBufferIndex, 2 * vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - vertexSize, rawBuffer, rawBufferIndex - (2 * vertexSize), vertexSize);
                alternativeTriangulation = false;
            } else {
                //Target vertex layout: ABC DAC
                System.arraycopy(rawBuffer, rawBufferIndex - (4 * vertexSize), rawBuffer, rawBufferIndex, vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - (2 * vertexSize), rawBuffer, rawBufferIndex + vertexSize, vertexSize);
            }
            vertexCount += 2;
            rawBufferIndex += 2 * vertexSize;
        }
    }

    private void fixAOQuad() {
        //Current vertex layout: ABCD
        if (alternativeTriangulation) {
            //Target vertex layout: BCDA
            quadVerticesPutIntoBuffer++;
            if (quadVerticesPutIntoBuffer == 4) {
                int vertexSize = VertexInfo.recomputeVertexInfo(shaderOn() ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1);
                ensureRawBufferHasExtraSpaceFor(vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - 4 * vertexSize, rawBuffer, rawBufferIndex, vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - 3 * vertexSize, rawBuffer, rawBufferIndex - 4 * vertexSize, 3 * vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex, rawBuffer, rawBufferIndex - vertexSize, vertexSize);
                quadVerticesPutIntoBuffer = 0;
                alternativeTriangulation = false;
            }
        } else {
            quadVerticesPutIntoBuffer = 0;
        }
    }

    @Override
    public void resumeQuadTriangulation() {
        quadTriangulationTemporarilySuspended = false;
    }

    @Override
    public boolean isQuadTriangulationSuspended() {
        return quadTriangulationTemporarilySuspended;
    }

    @Override
    public void disableTriangulator() {
        ToggleableTessellatorManager.INSTANCE.disableTriangulator();
    }

    @Override
    public void enableTriangulator() {
        ToggleableTessellatorManager.INSTANCE.enableTriangulator();
    }

    @Override
    public void disableTriangulatorLocal() {
        forceQuadRendering++;
    }

    @Override
    public void enableTriangulatorLocal() {
        forceQuadRendering--;
        if (forceQuadRendering < 0) {
            forceQuadRendering = 0;
        }
    }

    @Override
    public boolean isTriangulatorDisabled() {
        return ToggleableTessellatorManager.INSTANCE.isTriangulatorDisabled() || forceQuadRendering != 0;
    }

    @Override
    public boolean shaderOn() {
        return shaderOn;
    }

    @Override
    public void shaderOn(boolean state) {
        shaderOn = state;
    }

    @ModifyConstant(method = "addVertex",
                    constant = @Constant(intValue = 32),
                    require = 1)
    private int extendAddVertexCap(int constant) {
        return VertexInfo.recomputeVertexInfo(constant >>> 2, drawingTris ? 3 : 4);
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
