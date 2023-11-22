package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.proxy.ClientProxy;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;

import java.nio.FloatBuffer;
import java.util.Arrays;

@Mixin(ClippingHelperImpl.class)
public abstract class ClippingHelperImplMixin extends ClippingHelper {

    @Redirect(method = "getInstance",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;init()V"),
              require = 1)
    private static void initOncePerFrame(ClippingHelperImpl instance) {
        if (!ClientProxy.clippingHelperShouldInit)
            return;
        ClientProxy.clippingHelperShouldInit = false;
        instance.init();
    }
}
