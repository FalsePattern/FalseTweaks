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

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.renderlists.VoxelRenderListManager;
import com.falsepattern.lib.util.MathUtil;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.val;
import org.joml.Matrix4f;

import net.minecraft.block.BlockRailBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class VoxelRenderHelper {
    private static final TObjectIntMap<String> layers = new TObjectIntHashMap<>();
    private static final ThreadLocal<Matrix4f> threadMatrix = ThreadLocal.withInitial(Matrix4f::new);

    private static final float RAD_90DEG = (float)Math.toRadians(90);
    private static final float RAD_NEG90DEG = (float)Math.toRadians(-90);
    private static final float RAD_45DEG = (float)Math.toRadians(45);

    static {
        if (VoxelizerConfig.FORCED_LAYERS == null) {
            Share.log.error("Overlay config broken.");
        }
        for (val entry: VoxelizerConfig.FORCED_LAYERS) {
            val parts = entry.split("=");
            if (parts.length != 2) {
                Share.log.error("Invalid forced layer " + entry + " in overlay config! Format should be: texturename=number");
                continue;
            }
            try {
                layers.put(parts[0], Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                Share.log.error("Could not parse layer from " + entry + " in overlay config!", e);
            }
        }
    }
    public static void renderItemVoxelized(TextureAtlasSprite iicon, boolean glint) {
        val mesh = VoxelMesh.getMesh(iicon);
        val name = iicon.getIconName();
        int layer = 0;
        if (layers.containsKey(name)) {
            layer = layers.get(name) * 2;
        } else if (name.endsWith("_overlay")) {
            layer = 2;
        }
        if (glint) {
            layer++;
        }
        if (ModuleConfig.ITEM_RENDER_LISTS && VoxelRenderListManager.INSTANCE.pre(mesh, layer, glint)) {
            return;
        }
        val tess = Compat.tessellator();
        tess.startDrawingQuads();
        mesh.renderToTessellator(tess, layer, glint);
        tess.draw();
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            VoxelRenderListManager.INSTANCE.post();
        }
    }
    public static void renderRail(IBlockAccess blockAccess, BlockRailBase rail, int x, int y, int z, int meta, IIcon iicon, boolean mirrored) {
        val tess = Compat.tessellator();
        val mesh = VoxelMesh.getMesh((TextureAtlasSprite) iicon);
        tess.setBrightness(rail.getMixedBrightnessForBlock(blockAccess, x, y, z));
        tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        val transform = threadMatrix.get();
        transform.translation(x, y, z);
        transform.translate(0.5f, 0, 0.5f);
        switch (meta) {
            case 1:
            case 2: case 3: case 7: transform.rotateY(RAD_NEG90DEG); break;
            case 8: transform.scale(-1, 1, -1); break;
            case 9: transform.rotateY(RAD_90DEG);
        }
        if (!mirrored)
            transform.scale(-1, 1, -1);
        transform.translate(-0.5f, 0, -0.5f);
        switch (meta) {
            case 2: case 3: case 4: case 5: transform.translate(0, 0.0625F, 0)
                                                     .rotateX(RAD_45DEG)
                                                     .scale(1, MathUtil.SQRT_2, MathUtil.SQRT_2)
                                                     .translate(0, 0, 0.0625F); break;
            default: transform.rotateX(RAD_90DEG); break;
        }
        mesh.renderToTessellator(tess, 0, false, true, transform);
    }

    public static void renderRailVanilla(RenderBlocks renderBlocks, BlockRailBase rail, int x, int y, int z) {
        int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
        IIcon iicon = renderBlocks.getBlockIconFromSideAndMetadata(rail, 0, meta);
        if (renderBlocks.hasOverrideBlockTexture()) {
            iicon = renderBlocks.overrideBlockTexture;
        }
        if (rail.isPowered())
            meta &= 0x7;
        renderRail(renderBlocks.blockAccess, rail, x, y, z, meta, iicon, false);
    }
}
