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

package com.falsepattern.falsetweaks.mixin.mixins.client.mipmapfix;

import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {
    @ModifyVariable(method = "loadSprite",
                    at = @At("HEAD"),
                    index = 1,
                    argsOnly = true,
                    require = 1)
    private BufferedImage[] scaleUpMipMap(BufferedImage[] img) {
        val baseImage = img[0];
        int w = baseImage.getWidth();
        int h = baseImage.getHeight();
        while (w < 16 || h < 16) {
            w *= 2;
            h *= 2;
        }
        if (baseImage.getWidth() != w || baseImage.getHeight() != h) {
            for (int i = 0; i < img.length; i++) {
                if (img[i] == null) {
                    continue;
                }
                val newImage = new BufferedImage(w >>> i, h >>> i, BufferedImage.TYPE_INT_ARGB);
                val gfx = newImage.createGraphics();
                gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                gfx.drawImage(img[i], 0, 0, w >>> i, h >>> i, null);
                img[i] = newImage;
            }
        }
        return img;
    }
}
