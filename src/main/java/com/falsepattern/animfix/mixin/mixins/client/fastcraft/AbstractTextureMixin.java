package com.falsepattern.animfix.mixin.mixins.client.fastcraft;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


//Evil black magic class #1
//Revert fastcraft ASM changes
@SuppressWarnings({"UnresolvedMixinReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
@Mixin(AbstractTexture.class)
public abstract class AbstractTextureMixin {
    @Redirect(method = "deleteGlTexture",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;k(Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",
                       remap = false))
    private void disabledeleteGlTextureTweak(AbstractTexture dt) {

    }
}
