package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.mixin.helper.IWorldRendererMixin;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererOptifineMixin implements IWorldRendererMixin {

    @Shadow private boolean isInitialized;

    @Inject(method = "updateRenderer",
            at = @At(value = "HEAD"),
            require = 1)
    private void prepareRenderList(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (LeakFix.ENABLED && !this.isInitialized) {
            genList();
        }
    }

    @Inject(method = "callOcclusionQueryList",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void dropOcclusionQueryIfUninitialized(CallbackInfo ci) {
        if (LeakFix.ENABLED && !hasRenderList()) {
            ci.cancel();
        }
    }
}
