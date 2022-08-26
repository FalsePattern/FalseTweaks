/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.animfix.mixin.mixins.client.fastcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.texture.TextureUtil;

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
                       remap = false))
    private static int disableGenerateMipmapDataTweak(int a, int b, int c, int d, boolean e) {
        return func_147943_a(a, b, c, d, e);
    }

    @Redirect(method = "uploadTextureMipmap",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;i([[IIIIIZZ)Z",
                       remap = false))
    private static boolean disableUploadTextureMipmapTweak(int[][] a, int b, int c, int d, int e, boolean f, boolean g) {
        return false;
    }
}
