package com.falsepattern.triangulator.mixin.mixins.client;

import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements ITessellatorMixin, ToggleableTessellator {
    @Shadow
    private int drawMode;

    @Shadow private int rawBufferIndex;
    @Shadow private int[] rawBuffer;
    @Shadow private int vertexCount;

    private boolean hackedQuadRendering = false;
    private boolean drawingTris = false;
    private boolean alternativeTriangulation = false;
    private boolean quadTriangulationTemporarilySuspended = false;
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
        if (value == GL11.GL_QUADS) {
            value = GL11.GL_TRIANGLES;
            hackedQuadRendering = true;
        }
        if (value == GL11.GL_TRIANGLES) {
            drawingTris = true;
        }
        drawMode = value;
    }

    @Inject(method = "addVertex",
            at = @At(value = "RETURN"),
            require = 1)
    private void hackVertex(CallbackInfo ci) {
        if (!hackedQuadRendering || quadTriangulationTemporarilySuspended) return;
        quadVerticesPutIntoBuffer++;
        if (quadVerticesPutIntoBuffer == 4) {
            quadVerticesPutIntoBuffer = 0;
            //Current vertex layout: ABCD
            if (alternativeTriangulation) {
                //Target vertex layout: ABD DBC
                System.arraycopy(rawBuffer, rawBufferIndex - 16, rawBuffer, rawBufferIndex + 8, 8);
                System.arraycopy(rawBuffer, rawBufferIndex - 24, rawBuffer, rawBufferIndex, 8);
                System.arraycopy(rawBuffer, rawBufferIndex - 8, rawBuffer, rawBufferIndex - 16, 8);
                alternativeTriangulation = false;
            } else {
                //Target vertex layout: ABC DAC
                System.arraycopy(rawBuffer, rawBufferIndex - 32, rawBuffer, rawBufferIndex, 8);
                System.arraycopy(rawBuffer, rawBufferIndex - 16, rawBuffer, rawBufferIndex + 8, 8);
            }
            vertexCount += 2;
            rawBufferIndex += 16;
        }
    }

    @ModifyArg(method = "getVertexState",
               at = @At(value = "INVOKE",
                       target = "Ljava/util/PriorityQueue;<init>(ILjava/util/Comparator;)V",
                       remap = false),
               index = 1,
               require = 1)
    private Comparator<?> hackQuadComparator(Comparator<?> comparator) {
        if (drawingTris) {
            ((IQuadComparatorMixin)comparator).enableTriMode();
        }
        return comparator;
    }

    @ModifyConstant(method = "getVertexState",
                    constant = @Constant(intValue = 32),
                    require = 1)
    private int hackQuadCounting(int constant) {
        return constant - 8;
    }

    @Override
    public void setAlternativeTriangulation() {
        alternativeTriangulation = true;
    }

    @Override
    public void suspendQuadTriangulation() {
        quadTriangulationTemporarilySuspended = true;
    }

    @Override
    public void resumeQuadTriangulation() {
        quadTriangulationTemporarilySuspended = false;
    }
}
