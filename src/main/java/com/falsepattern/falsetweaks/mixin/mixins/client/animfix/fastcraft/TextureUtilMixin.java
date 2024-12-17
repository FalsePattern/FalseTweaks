/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.animfix.fastcraft;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.texture.TextureUtil;

//Evil black magic class #4
//Revert fastcraft ASM changes
@Mixin(value = TextureUtil.class,
       priority = 1200)
public abstract class TextureUtilMixin {
    @Shadow
    private static int func_147943_a(int p_147943_0_, int p_147943_1_, int p_147943_2_, int p_147943_3_, boolean p_147943_4_) {
        return 0;
    }

    @Dynamic
    @Redirect(method = "generateMipmapData",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;p(IIIIZ)I",
                       remap = false),
              require = 0,
              expect = 0)
    private static int disableGenerateMipmapDataTweak(int a, int b, int c, int d, boolean e) {
        return func_147943_a(a, b, c, d, e);
    }

    @Dynamic
    @Redirect(method = "uploadTextureMipmap",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;i([[IIIIIZZ)Z",
                       remap = false),
              require = 0,
              expect = 0)
    private static boolean disableUploadTextureMipmapTweak(int[][] a, int b, int c, int d, int e, boolean f, boolean g) {
        return false;
    }
}
