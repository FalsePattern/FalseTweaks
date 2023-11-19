/*
 * This file is part of FalseTweaks.
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
import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockRailBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class VoxelRenderHelper {
    private static final TObjectIntMap<String> layers = new TObjectIntHashMap<>();
    private static final ThreadLocal<Matrix4f> threadMatrix = ThreadLocal.withInitial(Matrix4f::new);

    private static final float RAD_90DEG = (float) Math.toRadians(90);
    private static final float RAD_NEG90DEG = (float) Math.toRadians(-90);
    private static final float RAD_45DEG = (float) Math.toRadians(45);

    //Rail directions
    private static final int RAIL_FLAT_NORTH_SOUTH = 0x0;
    private static final int RAIL_FLAT_WEST_EAST = 0x1;
    private static final int RAIL_RAMP_EAST = 0x2;
    private static final int RAIL_RAMP_WEST = 0x3;
    private static final int RAIL_RAMP_NORTH = 0x4;
    private static final int RAIL_RAMP_SOUTH = 0x5;
    private static final int RAIL_CORNER_EAST_SOUTH = 0x6;
    private static final int RAIL_CORNER_WEST_SOUTH = 0x7;
    private static final int RAIL_CORNER_WEST_NORTH = 0x8;
    private static final int RAIL_CORNER_EAST_NORTH = 0x9;

    static {
        if (VoxelizerConfig.FORCED_LAYERS == null) {
            Share.log.error("Overlay config broken.");
        }
        for (val entry : VoxelizerConfig.FORCED_LAYERS) {
            val parts = entry.split("=");
            if (parts.length != 2) {
                Share.log.error(
                        "Invalid forced layer " + entry + " in overlay config! Format should be: texturename=number");
                continue;
            }
            try {
                layers.put(parts[0], Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                Share.log.error("Could not parse layer from " + entry + " in overlay config!", e);
            }
        }
    }

    public static void renderItemVoxelized(TextureAtlasSprite iicon) {
        val mesh = VoxelMesh.getMesh(iicon);
        val name = iicon.getIconName();
        int layer;
        if (layers.containsKey(name)) {
            layer = layers.get(name) * 2;
            if (Data.enchantmentGlintTextureBound) {
                layer++;
            }
        } else {
            layer = Data.getCurrentItemLayer() * 2;
            if (Data.enchantmentGlintTextureBound) {
                layer--;
            }
        }
        if (Data.enchantmentGlintTextureBound) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
        if (ModuleConfig.ITEM_RENDER_LISTS &&
            VoxelRenderListManager.INSTANCE.pre(mesh, layer, Data.enchantmentGlintTextureBound)) {
            return;
        }
        val tess = Compat.tessellator();
        tess.startDrawingQuads();
        mesh.renderToTessellator(tess, layer, Data.enchantmentGlintTextureBound, VoxelType.Solid);
        tess.draw();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        if (!Data.enchantmentGlintTextureBound) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }
        tess.startDrawingQuads();
        mesh.renderToTessellator(tess, layer, Data.enchantmentGlintTextureBound, VoxelType.SemiSolid);
        tess.draw();
        GL11.glPopAttrib();
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            VoxelRenderListManager.INSTANCE.post();
        }
    }

    public static void renderRail(IBlockAccess blockAccess, BlockRailBase rail, int x, int y, int z, int railDirection, IIcon iicon, boolean mirrored) {
        val tess = Compat.tessellator();
        val mesh = VoxelMesh.getMesh((TextureAtlasSprite) iicon);
        tess.setBrightness(rail.getMixedBrightnessForBlock(blockAccess, x, y, z));
        tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        val transform = threadMatrix.get();
        transform.translation(x, y, z);
        transform.translate(0.5f, 0, 0.5f);
        switch (railDirection) {
            case RAIL_FLAT_WEST_EAST:
            case RAIL_RAMP_EAST:
            case RAIL_CORNER_WEST_SOUTH:
                transform.rotateY(RAD_NEG90DEG);
                break;
            case RAIL_RAMP_SOUTH:
            case RAIL_CORNER_WEST_NORTH:
                transform.scale(-1, 1, -1);
                break;
            case RAIL_RAMP_WEST:
            case RAIL_CORNER_EAST_NORTH:
                transform.rotateY(RAD_90DEG);
                break;
        }
        if (!mirrored) {
            transform.scale(-1, 1, -1);
        }
        transform.translate(-0.5f, 0, -0.5f);
        float offset = (float) (0.0625 * VoxelizerConfig.RAIL_THICKNESS);
        switch (railDirection) {
            case RAIL_RAMP_EAST:
            case RAIL_RAMP_WEST:
            case RAIL_RAMP_NORTH:
            case RAIL_RAMP_SOUTH:
                transform.translate(0, offset, 0)
                         .rotateX(RAD_45DEG)
                         .scale(1, MathUtil.SQRT_2, MathUtil.SQRT_2)
                         .translate(0, 0, offset);
                break;
            default:
                transform.rotateX(RAD_90DEG);
                break;
        }
        transform.scale(1, 1, (float) VoxelizerConfig.RAIL_THICKNESS);
        mesh.renderToTessellator(tess, 0, false, true, transform, (face) -> {
            //Notice: corner rails commented out because they have parts of the "wood" also on the edge of the mesh
            switch (face.dir) {
                case Up: {
                    if (face.minY != 0) {
                        break;
                    }
                    switch (railDirection) {
                        case RAIL_FLAT_NORTH_SOUTH:
                            return isBlockRailWithDirection(blockAccess, x, y, z - 1, RAIL_FLAT_NORTH_SOUTH,
                                                            RAIL_RAMP_NORTH, RAIL_CORNER_EAST_SOUTH,
                                                            RAIL_CORNER_WEST_SOUTH);
                        case RAIL_FLAT_WEST_EAST:
                            return isBlockRailWithDirection(blockAccess, x + 1, y, z, RAIL_FLAT_WEST_EAST,
                                                            RAIL_RAMP_EAST, RAIL_CORNER_WEST_SOUTH,
                                                            RAIL_CORNER_WEST_NORTH);
                    }
                    break;
                }
                case Down: {
                    if (face.maxY != mesh.ySize() - 1) {
                        break;
                    }
                    switch (railDirection) {
                        case RAIL_FLAT_NORTH_SOUTH:
                            //                        case RAIL_CORNER_EAST_SOUTH:
                            return isBlockRailWithDirection(blockAccess, x, y, z + 1, RAIL_FLAT_NORTH_SOUTH,
                                                            RAIL_RAMP_SOUTH, RAIL_CORNER_EAST_NORTH,
                                                            RAIL_CORNER_WEST_NORTH);
                        case RAIL_FLAT_WEST_EAST:
                            //                        case RAIL_CORNER_WEST_SOUTH:
                            return isBlockRailWithDirection(blockAccess, x - 1, y, z, RAIL_FLAT_WEST_EAST,
                                                            RAIL_RAMP_WEST, RAIL_CORNER_EAST_SOUTH,
                                                            RAIL_CORNER_EAST_NORTH);
                        //                        case RAIL_CORNER_WEST_NORTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x, y, z - 1, RAIL_FLAT_NORTH_SOUTH, RAIL_RAMP_NORTH, RAIL_CORNER_EAST_SOUTH, RAIL_CORNER_WEST_SOUTH);
                        //                        case RAIL_CORNER_EAST_NORTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x + 1, y, z, RAIL_FLAT_WEST_EAST, RAIL_RAMP_EAST, RAIL_CORNER_WEST_SOUTH, RAIL_CORNER_WEST_NORTH);
                    }
                    break;
                }
                case Right: {
                    if (face.maxX != mesh.xSize() - 1) {
                        break;
                    }
                    switch (railDirection) {
                        //                        case RAIL_CORNER_WEST_NORTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x - 1, y, z, RAIL_FLAT_WEST_EAST, RAIL_RAMP_WEST, RAIL_CORNER_EAST_SOUTH, RAIL_CORNER_EAST_NORTH);
                        //                        case RAIL_CORNER_WEST_SOUTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x, y, z + 1, RAIL_FLAT_NORTH_SOUTH, RAIL_RAMP_SOUTH, RAIL_CORNER_EAST_NORTH, RAIL_CORNER_WEST_NORTH);
                        //                        case RAIL_CORNER_EAST_SOUTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x + 1, y, z, RAIL_FLAT_WEST_EAST, RAIL_RAMP_EAST, RAIL_CORNER_WEST_SOUTH, RAIL_CORNER_WEST_NORTH);
                        //                        case RAIL_CORNER_EAST_NORTH:
                        //                            return isBlockRailWithMetadata(blockAccess, x, y, z - 1, RAIL_FLAT_NORTH_SOUTH, RAIL_RAMP_NORTH, RAIL_CORNER_EAST_SOUTH, RAIL_CORNER_WEST_SOUTH);
                    }
                    break;
                }
            }
            return false;
        }, VoxelType.Solid);
    }

    private static boolean isBlockRailWithDirection(IBlockAccess blockAccess, int x, int y, int z, int... expectedRailDirection) {
        val block = blockAccess.getBlock(x, y, z);
        if (!(block instanceof BlockRailBase)) {
            return false;
        }
        //Apply 4 bit mask for EndlessIDs compat
        val railDirection = ((BlockRailBase) block).getBasicRailMetadata(blockAccess, null, x, y, z) & 0xF;
        for (int expected : expectedRailDirection) {
            if (expected == railDirection) {
                return true;
            }
        }
        return false;
    }

    public static void renderRailVanilla(RenderBlocks renderBlocks, BlockRailBase rail, int x, int y, int z) {
        //Apply 4 bit mask for EndlessIDs compat
        int railDirection = renderBlocks.blockAccess.getBlockMetadata(x, y, z) & 0xF;
        IIcon iicon = renderBlocks.getBlockIconFromSideAndMetadata(rail, 0, railDirection);
        if (renderBlocks.hasOverrideBlockTexture()) {
            iicon = renderBlocks.overrideBlockTexture;
        }
        if (rail.isPowered()) {
            railDirection &= 0x7;
        }
        renderRail(renderBlocks.blockAccess, rail, x, y, z, railDirection, iicon, false);
    }
}
