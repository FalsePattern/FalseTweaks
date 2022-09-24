/*
 * Triangulator
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

package com.falsepattern.falsetweaks.voxelizer;

import lombok.RequiredArgsConstructor;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@RequiredArgsConstructor
public class Layer {
    public final TextureAtlasSprite texture;
    public final float thickness;

    public int fetchAlpha(int x, int y, int W, int H) {
        int wRatio = W / texture.getIconWidth();
        int hRatio = H / texture.getIconHeight();
        x /= wRatio;
        y /= hRatio;
        int argb = texture.getFrameTextureData(texture.frameCounter)[0][y * texture.getIconWidth() + x];
        return (argb >>> 24) & 0xFF;
    }
}
