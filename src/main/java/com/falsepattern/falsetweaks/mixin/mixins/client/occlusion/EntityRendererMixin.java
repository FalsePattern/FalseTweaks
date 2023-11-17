package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;

@Mixin(value = EntityRenderer.class)
public class EntityRendererMixin {

    /**
     * @reason RenderGlobalMixin#performCullingUpdates needs to know the chunk update deadline and the partial tick time
     */
    @Inject(method = "renderWorld",
            at = @At("HEAD"),
            require = 1)
    private void getRendererUpdateDeadline(float partialTickTime, long chunkUpdateDeadline, CallbackInfo ci) {
        OcclusionHelpers.chunkUpdateDeadline = chunkUpdateDeadline;
        OcclusionHelpers.partialTickTime = partialTickTime;
    }

}
