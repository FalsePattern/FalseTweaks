package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

@Mixin(ItemRenderer.class)
public abstract class OverlayCrashFix_ItemRendererMixin {
    @Shadow protected abstract void renderInsideOfBlock(float partialTickTime, IIcon blockTextureIndex);

    @Redirect(method = "renderOverlays",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/ItemRenderer;renderInsideOfBlock(FLnet/minecraft/util/IIcon;)V"),
              require = 0)
    private void guardRenderInsideOfBlock(ItemRenderer instance, float partialTickTime, IIcon blockTextureIndex) {
        if (blockTextureIndex == null) {
            blockTextureIndex = Blocks.stone.getBlockTextureFromSide(2);
            if (blockTextureIndex == null) {
                return;
            }
        }
        ((OverlayCrashFix_ItemRendererMixin)(Object)instance).renderInsideOfBlock(partialTickTime, blockTextureIndex);
    }
}
