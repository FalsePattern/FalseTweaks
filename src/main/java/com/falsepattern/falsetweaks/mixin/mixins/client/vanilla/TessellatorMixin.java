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

package com.falsepattern.falsetweaks.mixin.mixins.client.vanilla;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.ToggleableTessellatorManager;
import com.falsepattern.falsetweaks.api.ToggleableTessellator;
import com.falsepattern.falsetweaks.mixin.helper.IQuadComparatorMixin;
import com.falsepattern.falsetweaks.mixin.helper.ITessellatorMixin;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

import java.util.Comparator;

@Mixin(Tessellator.class)
@Accessors(fluent = true,
           chain = false)
public abstract class TessellatorMixin implements ITessellatorMixin, ToggleableTessellator {
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
        if (!isTriangulatorDisabled() &&
            value == GL11.GL_QUADS) {
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

    /**
     * @author SirFell
     * <p>
     * Fixes <a href="https://github.com/MinecraftForge/MinecraftForge/issues/981">MinecraftForge#981</a> . Crash on <a href="https://github.com/MinecraftForge/MinecraftForge/issues/981#issuecomment-57375939">bad moder rendering"(©LexManos)</a> of transparent/translucent blocks when they draw nothing.
     */
    @Inject(method = "getVertexState",
            at = @At("HEAD"),
            cancellable = true)
    public void getVertexStateNatural0Safe(float x, float y, float z, CallbackInfoReturnable<TesselatorVertexState> cir) {
        if (this.rawBufferIndex <= 0) {
            cir.setReturnValue(null);
            cir.cancel();
        }
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
            Share.log.error(new RuntimeException(
                    "Someone suspended triangulation while the tessellator had a partially rendered quad! Stacktrace: "));
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

    private void fixAOTriangles() {
        if (quadTriangulationTemporarilySuspended) {
            return;
        }
        quadVerticesPutIntoBuffer++;
        if (quadVerticesPutIntoBuffer == 4) {
            int vertexSize = shaderOn() ? 18 : 8;
            quadVerticesPutIntoBuffer = 0;
            //Current vertex layout: ABCD
            if (alternativeTriangulation) {
                //Target vertex layout: ABC DAC
                System.arraycopy(rawBuffer, rawBufferIndex - (4 * vertexSize), rawBuffer, rawBufferIndex, vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - (2 * vertexSize), rawBuffer, rawBufferIndex + vertexSize,
                                 vertexSize);
                alternativeTriangulation = false;
            } else {
                //Target vertex layout: ABD DBC
                System.arraycopy(rawBuffer, rawBufferIndex - (3 * vertexSize), rawBuffer, rawBufferIndex,
                                 2 * vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - vertexSize, rawBuffer, rawBufferIndex - (2 * vertexSize),
                                 vertexSize);
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
                int vertexSize = shaderOn() ? 18 : 8;
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
        return ToggleableTessellatorManager.INSTANCE.isTriangulatorDisabled() || forceQuadRendering == 0;
    }

    @Override
    public boolean shaderOn() {
        return shaderOn;
    }

    @Override
    public void shaderOn(boolean state) {
        shaderOn = state;
    }

    @Override
    public Comparator<?> hackQuadComparator(Comparator<?> comparator) {
        if (drawingTris) {
            IQuadComparatorMixin comp = (IQuadComparatorMixin) comparator;
            comp.enableTriMode();
            if (shaderOn) {
                comp.enableShaderMode();
            }
        }
        return comparator;
    }

    @Override
    public int hackQuadCounting(int constant) {
        return drawingTris ? (constant / 4) * 3 : constant;
    }
}
