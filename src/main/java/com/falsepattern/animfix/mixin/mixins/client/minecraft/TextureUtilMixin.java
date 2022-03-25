package com.falsepattern.animfix.mixin.mixins.client.minecraft;

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

    @Inject(method = "uploadTextureMipmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void uploadTextureBatchable(int[][] texture, int width, int height, int xOffset, int yOffset, boolean ignored1, boolean ignored2, CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        if (AnimationUpdateBatcher.batcher != null) {
            theProfiler.startSection("copyToBatch");
            boolean ended = AnimationUpdateBatcher.batcher.batchUpload(texture, width, height, xOffset, yOffset);
            theProfiler.endSection();
            if (ended) {
                ci.cancel();
                return;
            }
        }
        theProfiler.startSection("uploadUnbatched");
    }

    @Inject(method = "uploadTextureMipmap",
            at = @At(value = "RETURN"),
            require = 1)
    private static void uploadUnbatchedEnd(CallbackInfo ci) {
        theProfiler.endSection();
    }
}
