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

package com.falsepattern.falsetweaks.mixin.helper;

import com.falsepattern.falsetweaks.TriCompat;
import com.falsepattern.falsetweaks.config.FTConfig;
import com.falsepattern.falsetweaks.renderlists.VoxelRenderListManager;
import com.falsepattern.falsetweaks.voxelizer.VoxelMesh;
import lombok.val;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class VoxelRenderHelper {
    public static void renderItemVoxelized(TextureAtlasSprite iicon, boolean glint) {
        val mesh = VoxelMesh.getMesh(iicon, true);
        int layer = iicon.getIconName().endsWith("_overlay") ? 2 : 0;
        if (glint) {
            layer++;
        }
        if (FTConfig.ENABLE_ITEM_RENDERLISTS && VoxelRenderListManager.INSTANCE.pre(mesh, layer, glint)) {
            return;
        }
        val tess = TriCompat.tessellator();
        tess.startDrawingQuads();
        mesh.renderToTessellator(tess, layer, glint);
        tess.draw();
        if (FTConfig.ENABLE_ITEM_RENDERLISTS) {
            VoxelRenderListManager.INSTANCE.post();
        }
    }
}
