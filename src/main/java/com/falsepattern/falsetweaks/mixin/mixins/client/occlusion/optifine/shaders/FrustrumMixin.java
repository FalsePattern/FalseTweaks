package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine.shaders;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustrum;

@Mixin(Frustrum.class)
public abstract class FrustrumMixin {
    @Dynamic
    @Redirect(method = "<init>(Lnet/minecraft/client/renderer/culling/ClippingHelper;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;getInstance()Lnet/minecraft/client/renderer/culling/ClippingHelper;"),
              require = 1)
    private ClippingHelper noClippingHelperForShadow() {
        return null;
    }
}
