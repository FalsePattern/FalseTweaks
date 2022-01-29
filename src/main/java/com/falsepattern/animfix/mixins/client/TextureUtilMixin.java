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
            theProfiler.startSection("batchUpload");
            if (AnimationUpdateBatcher.batcher.batchUpload(mipMapLevel, texture, width, height, xOffset, yOffset)) {
                ci.cancel();
            }
            theProfiler.endSection();
        }
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureUtil;copyToBufferPos([III)V"),
            require = 1)
    private static void uploadTextureSub1(CallbackInfo ci) {
        theProfiler.endStartSection("buffercopy");
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glTexSubImage2D(IIIIIIIILjava/nio/IntBuffer;)V"),
            require = 1)
    private static void uploadTextureSub2(CallbackInfo ci) {
        theProfiler.endStartSection("upload");
    }

    @Inject(method = "uploadTextureSub",
            at = @At(value = "RETURN"),
            require = 1)
    private static void uploadTextureSub3(CallbackInfo ci) {
        theProfiler.endSection();
        theProfiler.endSection();
    }
}
