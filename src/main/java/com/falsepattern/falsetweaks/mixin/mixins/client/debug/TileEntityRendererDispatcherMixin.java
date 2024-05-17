package com.falsepattern.falsetweaks.mixin.mixins.client.debug;

import com.falsepattern.falsetweaks.modules.debug.Debug;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererDispatcherMixin {
    @Inject(method = "renderTileEntity",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    public void renderTileEntity(TileEntity tileEntity, float partialTick, CallbackInfo ci) {
        if (Debug.ENABLED && !Debug.tesrRendering)
            ci.cancel();
    }
}
