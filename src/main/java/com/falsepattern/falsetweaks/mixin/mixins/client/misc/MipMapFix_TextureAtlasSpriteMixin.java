/*
 * FalseTweaks
 *
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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import com.falsepattern.lib.util.MathUtil;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

@Mixin(TextureAtlasSprite.class)
public abstract class MipMapFix_TextureAtlasSpriteMixin {
    @ModifyVariable(method = "loadSprite",
                    at = @At("HEAD"),
                    index = 1,
                    argsOnly = true,
                    require = 1)
    private BufferedImage[] fixMipMap(BufferedImage[] img) {
        val baseImage = img[0];
        int w = baseImage.getWidth();
        int h = baseImage.getHeight();
        int reqW = MathUtil.smallestEncompassingPowerOfTwo(Math.max(w, 16));
        int reqH = MathUtil.smallestEncompassingPowerOfTwo(Math.max(h, 16));
        if (reqW != w || reqH != h) {
            for (int i = 0; i < img.length; i++) {
                if (img[i] == null) {
                    continue;
                }
                val newImage = new BufferedImage(reqW >>> i, reqH >>> i, BufferedImage.TYPE_INT_ARGB);
                val gfx = newImage.createGraphics();
                gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                gfx.drawImage(img[i], 0, 0, reqW >>> i, reqH >>> i, null);
                img[i] = newImage;
            }
        }
        return img;
    }
}
