package com.falsepattern.triangulator.mixin.mixins.client.redstonepaste;

import com.falsepattern.triangulator.api.ToggleableTessellator;
import fyber.redstonepastemod.client.RedstonePasteHighlighter;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RedstonePasteHighlighter.class,
       remap = false)
public abstract class RedstonePasteHighlighterMixin {
    @Inject(method = "drawLineLoop",
            at = @At("HEAD"),
            require = 1)
    private void turnOffTriangulator(CallbackInfo ci) {
        ((ToggleableTessellator) Tessellator.instance).disableTriangulator();
    }

    @Inject(method = "drawLineLoop",
            at = @At("RETURN"),
            require = 1)
    private void turnOnTriangulator(CallbackInfo ci) {
        ((ToggleableTessellator) Tessellator.instance).enableTriangulator();
    }
}
