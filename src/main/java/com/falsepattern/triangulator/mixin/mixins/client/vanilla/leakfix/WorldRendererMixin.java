package com.falsepattern.triangulator.mixin.mixins.client.vanilla.leakfix;

import com.falsepattern.triangulator.mixin.helper.LeakFix;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow private int glRenderList;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;setPosition(III)V"),
              require = 1)
    private void resetRenderListBefore(WorldRenderer thiz, int x, int y, int z) {
        if (LeakFix.ENABLED) {
            glRenderList = -1;
        }
        thiz.setPosition(x, y, z);
    }

    @Inject(method = "setPosition",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/WorldRenderer;setDontDraw()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void genLists(int p_78913_1_, int p_78913_2_, int p_78913_3_, CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (glRenderList >= 0) {
                GLAllocation.deleteDisplayLists(glRenderList);
            }
            glRenderList = GLAllocation.generateDisplayLists(3);
        }
    }

    @Inject(method = "setDontDraw",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearLists(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (glRenderList < 0) {
                return;
            }
            GLAllocation.deleteDisplayLists(glRenderList);
            glRenderList = -1;
        }
    }
}
