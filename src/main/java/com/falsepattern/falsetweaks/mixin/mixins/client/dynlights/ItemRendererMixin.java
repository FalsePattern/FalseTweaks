package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsWorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow private Minecraft mc;

    @Inject(method = "renderItemInFirstPerson",
            at = @At("HEAD"),
            require = 1)
    private void preRender(float partialTickTime, CallbackInfo ci) {
        ((DynamicLightsWorldClient)this.mc.theWorld).ft$renderItemInFirstPerson(true);
    }
    @Inject(method = "renderItemInFirstPerson",
            at = @At("RETURN"),
            require = 1)
    private void postRender(float partialTickTime, CallbackInfo ci) {
        ((DynamicLightsWorldClient)this.mc.theWorld).ft$renderItemInFirstPerson(false);
    }
}
