package com.falsepattern.triangulator.mixin.mixins.client.optifine;

import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tessellator.class)
public abstract class TessellatorVanillaMixin implements ITessellatorMixin {
    @Inject(method = "addVertex",
            at = @At(value = "RETURN"),
            require = 1)
    private void hackVertex(CallbackInfo ci) {
        triangulate();
    }
}
