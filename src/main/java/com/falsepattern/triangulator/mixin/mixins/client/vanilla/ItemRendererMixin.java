package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.ItemRenderListManager;
import com.falsepattern.triangulator.TriConfig;
import lombok.SneakyThrows;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.ItemRenderer.class)
public abstract class ItemRendererMixin {
    @SneakyThrows
    @Inject(method = "renderItemIn2D",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private static void leFunnyRenderListStart(Tessellator tess, float a, float b, float c, float d, int e, int f, float g, CallbackInfo ci) {
        if (TriConfig.ENABLE_ITEM_RENDERLISTS && ItemRenderListManager.INSTANCE.pre(a, b, c, d, e, f, g))
            ci.cancel();
    }

    @Inject(method = "renderItemIn2D",
            at = @At("RETURN"),
            require = 1)
    private static void leFunnyRenderListEnd(Tessellator tess, float a, float b, float c, float d, int e, int f, float g, CallbackInfo ci) {
        if (TriConfig.ENABLE_ITEM_RENDERLISTS)
            ItemRenderListManager.INSTANCE.post();
    }

    @Redirect(method = "renderItemIn2D",
              slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                                      ordinal = 0),
                             to = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                                      ordinal = 5)),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"),
              require = 5)
    private static int batchDrawCalls1(Tessellator instance) {
        return 0;
    }

    @Redirect(method = "renderItemIn2D",
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                                        ordinal = 0),
                             to = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                                      ordinal = 5)),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V"),
              require = 5)
    private static void batchDrawCalls2(Tessellator instance) {

    }
}
