package com.falsepattern.animfix.mixin.mixins.client.fastcraft;

import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Evil black magic class #3
//Revert fastcraft ASM changes
@SuppressWarnings({"UnresolvedMixinReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
@Mixin(TextureMap.class)
public abstract class TextureMapMixin {
    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;m(Lnet/minecraft/client/renderer/texture/Stitcher;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
                       remap = false))
    private void disableAddSpriteTweak(Stitcher stitcher, TextureAtlasSprite sprite) {
        stitcher.addSprite(sprite);
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;g(Lnet/minecraft/client/renderer/texture/Stitcher;Lnet/minecraft/client/renderer/texture/TextureMap;)V",
                       remap = false))
    private void disableDoStitchTweak(Stitcher stitcher, TextureMap map) {
        stitcher.doStitch();
    }

    @Redirect(method = "tick",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;h(Lnet/minecraft/client/renderer/texture/TextureMap;)V",
                       remap = false))
    private void disableUpdateAnimationsTweak(TextureMap map) {
        map.updateAnimations();
    }

    @Redirect(method = "setTextureEntry",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;l(Lnet/minecraft/client/renderer/texture/TextureMap;Ljava/lang/String;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
                       remap = false),
              remap = false)
    private void disableSetTextureEntryTweak(TextureMap map, String str, TextureAtlasSprite sprite) {
    }
}
