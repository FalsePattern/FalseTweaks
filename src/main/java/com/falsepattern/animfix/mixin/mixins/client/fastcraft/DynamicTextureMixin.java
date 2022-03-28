package com.falsepattern.animfix.mixin.mixins.client.fastcraft;

import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Evil black magic class #2
//Revert fastcraft ASM changes
@SuppressWarnings({"UnresolvedMixinReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
@Mixin(DynamicTexture.class)
public abstract class DynamicTextureMixin {
    @Redirect(method = "updateDynamicTexture",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;j(Lnet/minecraft/client/renderer/texture/DynamicTexture;)V",
                       remap = false))
    private void disableUpdateDynamicTextureTweak(DynamicTexture dt) {

    }
}
