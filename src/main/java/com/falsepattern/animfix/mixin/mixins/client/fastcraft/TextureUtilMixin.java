package com.falsepattern.animfix.mixin.mixins.client.fastcraft;

import net.minecraft.client.renderer.texture.TextureUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Evil black magic class #4
//Revert fastcraft ASM changes
@SuppressWarnings({"UnresolvedMixinReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
@Mixin(TextureUtil.class)
public abstract class TextureUtilMixin {
    @Shadow
    private static int func_147943_a(int p_147943_0_, int p_147943_1_, int p_147943_2_, int p_147943_3_, boolean p_147943_4_) {
        return 0;
    }

    @Redirect(method = "generateMipmapData",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;p(IIIIZ)I",
                       remap = false),
              require = 1)
    private static int disableGenerateMipmapDataTweak(int a, int b, int c, int d, boolean e) {
        return func_147943_a(a, b, c, d, e);
    }

    @Redirect(method = "uploadTextureMipmap",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;i([[IIIIIZZ)Z",
                       remap = false),
              require = 1)
    private static boolean disableUploadTextureMipmapTweak(int[][] a, int b, int c, int d, int e, boolean f, boolean g) {
        return false;
    }
}
