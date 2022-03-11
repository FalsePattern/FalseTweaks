//Keep in sync with TessellatorVanillaMixin
package com.falsepattern.triangulator.mixin.mixins.client.optifine;

import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Tessellator.class)
public abstract class TessellatorOptiFineMixin implements ITessellatorMixin {

    @Shadow private int drawMode;

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
            if (quadTriangulationActive())
                drawMode = GL11.GL_QUADS;
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
            if (quadTriangulationActive())
                drawMode = GL11.GL_QUADS;
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
