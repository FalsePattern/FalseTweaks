package com.falsepattern.animfix.mixins.client;

import com.falsepattern.animfix.AnimationUpdateBatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureUtil.class)
@SideOnly(Side.CLIENT)
public abstract class TextureUtilMixin {
    private static Profiler theProfiler;

    @Inject(method = "uploadTextureSub",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void uploadTextureSub0(int mipMapLevel, int[] texture, int width, int height, int xOffset, int yOffset, boolean magLinear, boolean clamped, boolean minLinear, CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        theProfiler.startSection("uploadTextureSub");
        if (AnimationUpdateBatcher.batcher != null) {
            theProfiler.startSection("copyToBatch");
            if (AnimationUpdateBatcher.batcher.batchUpload(mipMapLevel, texture, width, height, xOffset, yOffset)) {
                ci.cancel();
                theProfiler.endSection();
                theProfiler.endSection();
                return;
            }
            theProfiler.endSection();
        }
        theProfiler.startSection("setup");
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureUtil;copyToBufferPos([III)V"),
            require = 1)
    private static void uploadTextureSub1(CallbackInfo ci) {
        theProfiler.endStartSection("copyToNative");
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glTexSubImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
                     remap = false),
            require = 1)
    private static void uploadTextureSub2(CallbackInfo ci) {
        theProfiler.endStartSection("uploadToGPU");
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "RETURN"),
            require = 1)
    private static void uploadTextureSub3(CallbackInfo ci) {
        theProfiler.endSection();
        theProfiler.endSection();
    }
}
