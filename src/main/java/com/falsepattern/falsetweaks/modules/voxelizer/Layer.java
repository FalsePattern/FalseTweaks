/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.voxelizer;

import com.falsepattern.falsetweaks.modules.voxelizer.interfaces.ITextureAtlasSpriteMixin;
import com.falsepattern.lib.util.MathUtil;
import lombok.RequiredArgsConstructor;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@RequiredArgsConstructor
public class Layer {
    public final TextureAtlasSprite texture;
    public final float thickness;

    private ITextureAtlasSpriteMixin tex() {
        return (ITextureAtlasSpriteMixin) texture;
    }

    public int fetchAlpha(int x, int y, int W, int H) {
        x = xToReal(x, W);
        y = yToReal(y, H);
        if (tex().useAnisotropicFiltering()) {
            x += 8;
            y += 8;
        }
        try {
            return tex().getFrameAlphaData(tex().frameCounter(), x, y);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(texture.getIconName());
            throw e;
        }
    }

    private int xToReal(int x, int W) {
        return x / (W / tex().getRealWidth());
    }

    private int yToReal(int y, int H) {
        return y / (H / tex().getRealHeight());
    }

    public float fetchU(float x, float W) {
        return fetch(x, W, texture.getMinU(), texture.getMaxU());
    }

    public float fetchV(float y, float H) {
        return fetch(y, H, texture.getMinV(), texture.getMaxV());
    }

    private float fetch(float a, float A, float min, float max) {
        return (float) MathUtil.clampedLerp(min, max, a / A);
    }

    public String textureIdentity() {
        return texture.getIconName() + '|' + tex().frameCounter();
    }
}
