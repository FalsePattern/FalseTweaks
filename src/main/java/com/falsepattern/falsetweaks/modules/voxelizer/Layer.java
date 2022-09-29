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
        int argb = tex().getFrameTextureDataSafe(tex().getFrameCounter())[0][y * texture.getIconWidth() + x];
        return (argb >>> 24) & 0xFF;
    }

    private int xToReal(int x, int W) {
        return x / (W / texture.getIconWidth());
    }

    private int yToReal(int y, int H) {
        return y / (H / texture.getIconHeight());
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
        return texture.getIconName() + '|' + tex().getFrameCounter();
    }
}
