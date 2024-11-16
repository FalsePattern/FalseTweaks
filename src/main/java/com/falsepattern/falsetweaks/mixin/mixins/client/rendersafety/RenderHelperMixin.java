package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

@Mixin(RenderHelper.class)
public abstract class RenderHelperMixin {
    @Inject(method = "enableStandardItemLighting",
            at = @At("RETURN"),
            require = 1)
    private static void clearLight(CallbackInfo ci) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    }
}
