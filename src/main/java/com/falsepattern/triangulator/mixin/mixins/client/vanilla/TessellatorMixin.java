package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.TriCompat;
import com.falsepattern.triangulator.Triangulator;
import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;

@Mixin(Tessellator.class)
@Accessors(fluent = true, chain = false)
public abstract class TessellatorMixin implements ITessellatorMixin, ToggleableTessellator {
    @Shadow
    private int drawMode;

    @Shadow
    private int[] rawBuffer;
    @Shadow
    private int rawBufferIndex;
    @Shadow
    private int vertexCount;
    @Shadow private int rawBufferSize;

    @Shadow public abstract TesselatorVertexState getVertexState(float p_147564_1_, float p_147564_2_, float p_147564_3_);

    private boolean hackedQuadRendering = false;
    @Getter
    private boolean drawingTris = false;
    @Getter
    @Setter
    private boolean alternativeTriangulation = false;
    private boolean quadTriangulationTemporarilySuspended = false;
    private boolean shaderOn = false;
    private boolean forceQuadRendering = false;
    private int quadVerticesPutIntoBuffer = 0;

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
        if (TriCompat.enableTriangulation() &&value == GL11.GL_QUADS && !forceQuadRendering) {
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
    @Inject(method = "getVertexState", at = @At("HEAD"), cancellable = true)
    public void getVertexStateNatural0Safe(float x, float y, float z, CallbackInfoReturnable<TesselatorVertexState> cir){
        if(this.rawBufferIndex <= 0) {
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
            Triangulator.triLog.error(new RuntimeException("Someone suspended triangulation while the tessellator had a partially rendered quad! Stacktrace: "));
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
        if (quadTriangulationTemporarilySuspended) return;
        quadVerticesPutIntoBuffer++;
        if (quadVerticesPutIntoBuffer == 4) {
            int vertexSize = shaderOn() ? 18 : 8;
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
            if (quadVerticesPutIntoBuffer == 1) {
                int vertexSize = shaderOn() ? 18 : 8;
                rawBufferIndex -= vertexSize;
                vertexCount--;
                System.arraycopy(rawBuffer, rawBufferIndex, rawBuffer, rawBufferIndex + 3 * vertexSize, vertexSize);
            } else if (quadVerticesPutIntoBuffer == 4) {
                rawBufferIndex += shaderOn() ? 18 : 8;
                vertexCount++;
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
        forceQuadRendering = true;
    }

    @Override
    public void enableTriangulator() {
        forceQuadRendering = false;
    }

    @Override
    public boolean isTriangulatorDisabled() {
        return forceQuadRendering;
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
            if (shaderOn)
                comp.enableShaderMode();
        }
        return comparator;
    }

    @Override
    public int hackQuadCounting(int constant) {
        return drawingTris ? (constant / 4) * 3 : constant;
    }
}
