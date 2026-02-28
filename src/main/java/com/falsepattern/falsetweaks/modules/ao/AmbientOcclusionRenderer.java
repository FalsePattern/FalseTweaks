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

package com.falsepattern.falsetweaks.modules.ao;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.config.AOFixConfig;
import com.falsepattern.falsetweaks.modules.triangulator.renderblocks.Facing;
import com.falsepattern.lib.util.MathUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.joml.Vector3ic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import static com.falsepattern.falsetweaks.modules.ao.AOMath.averageAO;
import static com.falsepattern.falsetweaks.modules.ao.AOMath.biLerpAO;
import static com.falsepattern.falsetweaks.modules.ao.AOMath.lerpAO;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.averageBrightness;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.biLerpBrightness;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.lerpBrightness;

@RequiredArgsConstructor
public class AmbientOcclusionRenderer implements AORenderer {
    private static final int SECTION_SIZE = 3 * 3 * 3;
    private static final int AO_OFFSET = 0;
    private static final int LIGHT_OFFSET = AO_OFFSET + SECTION_SIZE;
    private static final int INSET_OFFSET = LIGHT_OFFSET + SECTION_SIZE;
    private static final int TRANSPARENCY_OFFSET = INSET_OFFSET + 6;
    private static final int TOTAL_SIZE = TRANSPARENCY_OFFSET + 1;
    private final int[] data = new int[TOTAL_SIZE];

    private static final int YNEG_I = 0;
    private static final int YPOS_I = 1;
    private static final int ZNEG_I = 2;
    private static final int ZPOS_I = 3;
    private static final int XNEG_I = 4;
    private static final int XPOS_I = 5;

    private float aoTopLeft;
    private float aoTopRight;
    private float aoBottomLeft;
    private float aoBottomRight;

    private int brightnessTopLeft;
    private int brightnessTopRight;
    private int brightnessBottomLeft;
    private int brightnessBottomRight;

    @Override
    public boolean renderWithAO(RenderBlocks rb, Block block, int x, int y, int z, float r, float g, float b) {
        val renderAllFaces = rb.renderAllFaces;
        val world = rb.blockAccess;
        final boolean ryn, ryp, rzn, rzp, rxn, rxp;
        boolean anyRender = false;
        if (renderAllFaces) {
            anyRender = ryn = ryp = rzn = rzp = rxn = rxp = true;
        } else {
            anyRender |= ryn = shouldSideBeRenderedQuick(world, block, x, y - 1, z, YNEG_I);
            anyRender |= ryp = shouldSideBeRenderedQuick(world, block, x, y + 1, z, YPOS_I);
            anyRender |= rzn = shouldSideBeRenderedQuick(world, block, x, y, z - 1, ZNEG_I);
            anyRender |= rzp = shouldSideBeRenderedQuick(world, block, x, y, z + 1, ZPOS_I);
            anyRender |= rxn = shouldSideBeRenderedQuick(world, block, x - 1, y, z, XNEG_I);
            anyRender |= rxp = shouldSideBeRenderedQuick(world, block, x + 1, y, z, XPOS_I);
        }
        if (!anyRender)
            return false;
        final AOChunkCache aocc;
        if (world instanceof AOChunkCache) {
            aocc = (AOChunkCache) world;
            aocc.ft$setUseNeighborBrightness(false);
        } else {
            aocc = null;
        }
        try {
            rb.enableAO = true;
            Compat.tessellator()
                  .setBrightness(0x00f000f0);
            val inset_yneg = (float) MathUtil.clamp(rb.renderMinY, 0, 1);
            val inset_ypos = (float) MathUtil.clamp(1 - rb.renderMaxY, 0, 1);
            val inset_zneg = (float) MathUtil.clamp(rb.renderMinZ, 0, 1);
            val inset_zpos = (float) MathUtil.clamp(1 - rb.renderMaxZ, 0, 1);
            val inset_xneg = (float) MathUtil.clamp(rb.renderMinX, 0, 1);
            val inset_xpos = (float) MathUtil.clamp(1 - rb.renderMaxX, 0, 1);

            data[INSET_OFFSET + YNEG_I] = Float.floatToRawIntBits(inset_yneg);
            data[INSET_OFFSET + YPOS_I] = Float.floatToRawIntBits(inset_ypos);
            data[INSET_OFFSET + ZNEG_I] = Float.floatToRawIntBits(inset_zneg);
            data[INSET_OFFSET + ZPOS_I] = Float.floatToRawIntBits(inset_zpos);
            data[INSET_OFFSET + XNEG_I] = Float.floatToRawIntBits(inset_xneg);
            data[INSET_OFFSET + XPOS_I] = Float.floatToRawIntBits(inset_xpos);

            int fetchMask = 0;
            if (ryn) { fetchMask |= inset_yneg != 0 ? 0b000_000_000___111_111_111___111_111_111 : 0b000_000_000___000_000_000___111_111_111; }
            if (ryp) { fetchMask |= inset_ypos != 0 ? 0b111_111_111___111_111_111___000_000_000 : 0b111_111_111___000_000_000___000_000_000; }
            if (rzn) { fetchMask |= inset_zneg != 0 ? 0b000_111_111___000_111_111___000_111_111 : 0b000_000_111___000_000_111___000_000_111; }
            if (rzp) { fetchMask |= inset_zpos != 0 ? 0b111_111_000___111_111_000___111_111_000 : 0b111_000_000___111_000_000___111_000_000; }
            if (rxn) { fetchMask |= inset_xneg != 0 ? 0b011_011_011___011_011_011___011_011_011 : 0b001_001_001___001_001_001___001_001_001; }
            if (rxp) { fetchMask |= inset_xpos != 0 ? 0b110_110_110___110_110_110___110_110_110 : 0b100_100_100___100_100_100___100_100_100; }

            fetchLightingData(rb, block, x, y, z, fetchMask);
            val useColor = !(rb.getBlockIcon(block)
                               .getIconName()
                               .equals("grass_top") || rb.hasOverrideBlockTexture());
            if (ryn) {
                renderFace(rb, Facing.YNEG, block, x, y, z, useColor, r, g, b);
            }
            if (ryp) {
                renderFace(rb, Facing.YPOS, block, x, y, z, useColor, r, g, b);
            }
            if (rzn) {
                renderFace(rb, Facing.ZNEG, block, x, y, z, useColor, r, g, b);
            }
            if (rzp) {
                renderFace(rb, Facing.ZPOS, block, x, y, z, useColor, r, g, b);
            }
            if (rxn) {
                renderFace(rb, Facing.XNEG, block, x, y, z, useColor, r, g, b);
            }
            if (rxp) {
                renderFace(rb, Facing.XPOS, block, x, y, z, useColor, r, g, b);
            }
            rb.enableAO = false;
            return true;
        } finally {
            if (aocc != null) {
                aocc.ft$setUseNeighborBrightness(true);
            }
        }
    }

    private void renderFace(RenderBlocks rb, Facing facing, Block block, int x, int y, int z, boolean useColor, float r, float g, float b) {
        val inset = MathUtil.clamp(getInset(facing.face) * 2, 0, 1);
        val isInset = inset != 0;

        val data = this.data;

        fetchMixedAO(data, facing, true);
        fetchMixedBrightness(data, facing, true);
        if (isInset) {
            float aoTopLeftOut = this.aoTopLeft;
            float aoTopRightOut = this.aoTopRight;
            float aoBottomLeftOut = this.aoBottomLeft;
            float aoBottomRightOut = this.aoBottomRight;
            fetchMixedAO(data, facing, false);

            this.aoTopLeft = lerpAO(aoTopLeftOut, this.aoTopLeft, inset);
            this.aoTopRight = lerpAO(aoTopRightOut, this.aoTopRight, inset);
            this.aoBottomLeft = lerpAO(aoBottomLeftOut, this.aoBottomLeft, inset);
            this.aoBottomRight = lerpAO(aoBottomRightOut, this.aoBottomRight, inset);
            if (!AOFixConfig.stairAOFix) {
                int brightnessTopLeftOut = this.brightnessTopLeft;
                int brightnessTopRightOut = this.brightnessTopRight;
                int brightnessBottomLeftOut = this.brightnessBottomLeft;
                int brightnessBottomRightOut = this.brightnessBottomRight;
                fetchMixedBrightness(data, facing, false);
                this.brightnessTopLeft = lerpBrightness(brightnessTopLeftOut, this.brightnessTopLeft, inset);
                this.brightnessTopRight = lerpBrightness(brightnessTopRightOut, this.brightnessTopRight, inset);
                this.brightnessBottomLeft = lerpBrightness(brightnessBottomLeftOut, this.brightnessBottomLeft, inset);
                this.brightnessBottomRight = lerpBrightness(brightnessBottomRightOut, this.brightnessBottomRight, inset);
            }
        }

        dispatchRender(rb, facing, block, x, y, z, useColor, r, g, b);
    }

    private void fetchMixedBrightness(int[] data, Facing facing, boolean front) {
        final int x, y, z;
        if (front) {
            x = 1 + facing.front.x();
            y = 1 + facing.front.y();
            z = 1 + facing.front.z();
        } else {
            x = 1;
            y = 1;
            z = 1;
        }

        val brightnessCenter = getBrightness(data, x, y, z);
        val brightnessLeft = getBrightness(data, x, y, z, facing.left);
        val brightnessRight = getBrightness(data, x, y, z, facing.right);
        val brightnessBottom = getBrightness(data, x, y, z, facing.bottom);
        val brightnessTop = getBrightness(data, x, y, z, facing.top);

        int lt = data[TRANSPARENCY_OFFSET];
        val bTransparentLeft = getBrightnessTransparency(lt, x, y, z, facing.left);
        val bTransparentRight = getBrightnessTransparency(lt, x, y, z, facing.right);
        val bTransparentBottom = getBrightnessTransparency(lt, x, y, z, facing.bottom);
        val bTransparentTop = getBrightnessTransparency(lt, x, y, z, facing.top);

        final int brightnessTopLeft;
        if (!bTransparentLeft && !bTransparentTop) {
            brightnessTopLeft = averageBrightness(brightnessLeft, brightnessTop);
        } else {
            brightnessTopLeft = getBrightness(data, x, y, z, facing.topLeft);
        }

        final int brightnessTopRight;
        if (!bTransparentRight && !bTransparentTop) {
            brightnessTopRight = averageBrightness(brightnessRight, brightnessTop);
        } else {
            brightnessTopRight = getBrightness(data, x, y, z, facing.topRight);
        }

        final int brightnessBottomRight;
        if (!bTransparentRight && !bTransparentBottom) {
            brightnessBottomRight = averageBrightness(brightnessRight, brightnessBottom);
        } else {
            brightnessBottomRight = getBrightness(data, x, y, z, facing.bottomRight);
        }

        final int brightnessBottomLeft;
        if (!bTransparentLeft && !bTransparentBottom) {
            brightnessBottomLeft = averageBrightness(brightnessLeft, brightnessBottom);
        } else {
            brightnessBottomLeft = getBrightness(data, x, y, z, facing.bottomLeft);
        }

        val brightnessTopLeftRaw = averageBrightness(brightnessLeft, brightnessTopLeft, brightnessTop, brightnessCenter);
        val brightnessBottomLeftRaw = averageBrightness(brightnessBottomLeft, brightnessLeft, brightnessBottom, brightnessCenter);
        val brightnessBottomRightRaw = averageBrightness(brightnessBottom, brightnessBottomRight, brightnessRight, brightnessCenter);
        val brightnessTopRightRaw = averageBrightness(brightnessTop, brightnessRight, brightnessTopRight, brightnessCenter);

        val insetLeft = getInset(facing.leftFace);
        val insetRight = getInset(facing.rightFace);
        val insetTop = getInset(facing.topFace);
        val insetBottom = getInset(facing.bottomFace);

        this.brightnessTopLeft = biLerpBrightness(brightnessTopLeftRaw, brightnessTopRightRaw, brightnessBottomLeftRaw, brightnessBottomRightRaw, insetLeft, insetTop);
        this.brightnessTopRight = biLerpBrightness(brightnessTopRightRaw, brightnessTopLeftRaw, brightnessBottomRightRaw, brightnessBottomLeftRaw, insetRight, insetTop);
        this.brightnessBottomLeft = biLerpBrightness(brightnessBottomLeftRaw, brightnessBottomRightRaw, brightnessTopLeftRaw, brightnessTopRightRaw, insetLeft, insetBottom);
        this.brightnessBottomRight = biLerpBrightness(brightnessBottomRightRaw, brightnessBottomLeftRaw, brightnessTopRightRaw, brightnessTopLeftRaw, insetRight, insetBottom);
    }

    private void fetchMixedAO(int[] data, Facing facing, boolean front) {
        final int x, y, z;
        if (front) {
            x = 1 + facing.front.x();
            y = 1 + facing.front.y();
            z = 1 + facing.front.z();
        } else {
            x = 1;
            y = 1;
            z = 1;
        }

        val aoCenter = getAO(data, x, y, z);
        val aoLeft = getAO(data, x, y, z, facing.left);
        val aoRight = getAO(data, x, y, z, facing.right);
        val aoBottom = getAO(data, x, y, z, facing.bottom);
        val aoTop = getAO(data, x, y, z, facing.top);

        val aoTransparentLeft = aoLeft == 1;
        val aoTransparentRight = aoRight == 1;
        val aoTransparentBottom = aoBottom == 1;
        val aoTransparentTop = aoTop == 1;

        final float lightTopLeft;
        if (!aoTransparentLeft && !aoTransparentTop) {
            lightTopLeft = averageAO(aoLeft, aoTop);
        } else {
            lightTopLeft = getAO(data, x, y, z, facing.topLeft);
        }

        final float lightTopRight;
        if (!aoTransparentRight && !aoTransparentTop) {
            lightTopRight = averageAO(aoRight, aoTop);
        } else {
            lightTopRight = getAO(data, x, y, z, facing.topRight);
        }

        final float lightBottomRight;
        if (!aoTransparentRight && !aoTransparentBottom) {
            lightBottomRight = averageAO(aoRight, aoBottom);
        } else {
            lightBottomRight = getAO(data, x, y, z, facing.bottomRight);
        }

        final float lightBottomLeft;
        if (!aoTransparentLeft && !aoTransparentBottom) {
            lightBottomLeft = averageAO(aoLeft, aoBottom);
        } else {
            lightBottomLeft = getAO(data, x, y, z, facing.bottomLeft);
        }

        val aoTopLeftRaw = averageAO(aoLeft, lightTopLeft, aoCenter, aoTop);
        val aoTopRightRaw = averageAO(aoCenter, aoTop, aoRight, lightTopRight);
        val aoBottomRightRaw = averageAO(aoBottom, aoCenter, lightBottomRight, aoRight);
        val aoBottomLeftRaw = averageAO(lightBottomLeft, aoLeft, aoBottom, aoCenter);

        val insetLeft = getInset(facing.leftFace);
        val insetRight = getInset(facing.rightFace);
        val insetTop = getInset(facing.topFace);
        val insetBottom = getInset(facing.bottomFace);

        this.aoTopLeft = biLerpAO(aoTopLeftRaw, aoTopRightRaw, aoBottomLeftRaw, aoBottomRightRaw, insetLeft, insetTop);
        this.aoTopRight = biLerpAO(aoTopRightRaw, aoTopLeftRaw, aoBottomRightRaw, aoBottomLeftRaw, insetRight, insetTop);
        this.aoBottomLeft = biLerpAO(aoBottomLeftRaw, aoBottomRightRaw, aoTopLeftRaw, aoTopRightRaw, insetLeft, insetBottom);
        this.aoBottomRight = biLerpAO(aoBottomRightRaw, aoBottomLeftRaw, aoTopRightRaw, aoTopLeftRaw, insetRight, insetBottom);
    }

    private float getInset(Facing.Direction dir) {
        return Float.intBitsToFloat(this.data[INSET_OFFSET + dir.ordinal()]);
    }

    private void dispatchRender(RenderBlocks rb, Facing facing, Block block, int x, int y, int z, boolean useColor, float r, float g, float b) {
        rb.brightnessTopLeft = brightnessTopLeft;
        rb.brightnessBottomLeft = brightnessBottomLeft;
        rb.brightnessBottomRight = brightnessBottomRight;
        rb.brightnessTopRight = brightnessTopRight;
//        rb.brightnessTopLeft = 0x00f000f0;
//        rb.brightnessBottomLeft = 0x00f000f0;
//        rb.brightnessBottomRight = 0x00f000f0;
//        rb.brightnessTopRight = 0x00f000f0;
        // @formatter:off
        if (useColor || facing.face == Facing.Direction.FACE_YPOS) {
            rb.colorRedTopLeft = rb.colorRedBottomLeft = rb.colorRedBottomRight = rb.colorRedTopRight = r * facing.brightness;
            rb.colorGreenTopLeft = rb.colorGreenBottomLeft = rb.colorGreenBottomRight = rb.colorGreenTopRight = g * facing.brightness;
            rb.colorBlueTopLeft = rb.colorBlueBottomLeft = rb.colorBlueBottomRight = rb.colorBlueTopRight = b * facing.brightness;
        } else {
            rb.colorRedTopLeft = rb.colorRedBottomLeft = rb.colorRedBottomRight = rb.colorRedTopRight = facing.brightness;
            rb.colorGreenTopLeft = rb.colorGreenBottomLeft = rb.colorGreenBottomRight = rb.colorGreenTopRight = facing.brightness;
            rb.colorBlueTopLeft = rb.colorBlueBottomLeft = rb.colorBlueBottomRight = rb.colorBlueTopRight = facing.brightness;
        }
        // @formatter:on

        rb.colorRedTopLeft *= aoTopLeft;
        rb.colorGreenTopLeft *= aoTopLeft;
        rb.colorBlueTopLeft *= aoTopLeft;
        rb.colorRedBottomLeft *= aoBottomLeft;
        rb.colorGreenBottomLeft *= aoBottomLeft;
        rb.colorBlueBottomLeft *= aoBottomLeft;
        rb.colorRedBottomRight *= aoBottomRight;
        rb.colorGreenBottomRight *= aoBottomRight;
        rb.colorBlueBottomRight *= aoBottomRight;
        rb.colorRedTopRight *= aoTopRight;
        rb.colorGreenTopRight *= aoTopRight;
        rb.colorBlueTopRight *= aoTopRight;
        IIcon icon = rb.getBlockIcon(block, rb.blockAccess, x, y, z, facing.face.ordinal());
        doRenderFace(rb, block, x, y, z, facing, icon);
        if (facing.face != Facing.Direction.FACE_YNEG && facing.face != Facing.Direction.FACE_YPOS) {
            if (RenderBlocks.fancyGrass &&
                icon.getIconName()
                    .equals("grass_side") &&
                !rb.hasOverrideBlockTexture()) {
                rb.colorRedTopLeft *= r;
                rb.colorRedBottomLeft *= r;
                rb.colorRedBottomRight *= r;
                rb.colorRedTopRight *= r;
                rb.colorGreenTopLeft *= g;
                rb.colorGreenBottomLeft *= g;
                rb.colorGreenBottomRight *= g;
                rb.colorGreenTopRight *= g;
                rb.colorBlueTopLeft *= b;
                rb.colorBlueBottomLeft *= b;
                rb.colorBlueBottomRight *= b;
                rb.colorBlueTopRight *= b;
                val sideIcon = BlockGrass.getIconSideOverlay();
                doRenderFace(rb, block, x, y, z, facing, sideIcon);
            }
        }
    }

    private void doRenderFace(RenderBlocks rb, Block block, int x, int y, int z, Facing facing, IIcon icon) {
        switch (facing) {
            case YNEG:
                rb.renderFaceYNeg(block, x, y, z, icon);
                break;
            case YPOS:
                rb.renderFaceYPos(block, x, y, z, icon);
                break;
            case ZNEG:
                rb.renderFaceZNeg(block, x, y, z, icon);
                break;
            case ZPOS:
                rb.renderFaceZPos(block, x, y, z, icon);
                break;
            case XNEG:
                rb.renderFaceXNeg(block, x, y, z, icon);
                break;
            case XPOS:
                rb.renderFaceXPos(block, x, y, z, icon);
                break;
        }
    }

    private static float getAO(int[] data, int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getAO(data, x, y, z);
    }

    private static float getAO(int[] data, int x, int y, int z) {
        val index = toIndex(x, y, z);
        return Float.intBitsToFloat(data[index]);
    }

    private static int getBrightness(int[] data, int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getBrightness(data, x, y, z);
    }
    private static int getBrightness(int[] data, int x, int y, int z) {
        val index = toIndex(x, y, z);
        return data[LIGHT_OFFSET + index];
    }

    private boolean shouldSideBeRenderedQuick(IBlockAccess world, Block block, int x, int y, int z, int dir) {
        try {
            return block.shouldSideBeRendered(world, x, y, z, dir);
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static boolean getBrightnessTransparency(int lt, int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getBrightnessTransparency(lt, x, y, z);
    }

    private static boolean getBrightnessTransparency(int lt, int x, int y, int z) {
        val index = toIndex(x, y, z);
        return ((lt >>> index) & 1) != 0;
    }

    private void fetchLightingData(RenderBlocks rb, Block block, int x, int y, int z, int fetchMask) {
        val world = rb.blockAccess;
        val data = this.data;
        //Sample surroundings
        int lt = 0;
        for (int yNear = 0; yNear < 3; yNear++) {
            for (int zNear = 0; zNear < 3; zNear++) {
                for (int xNear = 0; xNear < 3; xNear++) {
                    val xOff = x + xNear - 1;
                    val yOff = y + yNear - 1;
                    val zOff = z + zNear - 1;
                    val index = toIndex(xNear, yNear, zNear);
                    if (((fetchMask >>> index) & 1) == 0) {
                        continue;
                    }
                    val nearBlock = xNear == 1 && yNear == 1 && zNear == 1 ? block : world.getBlock(xOff, yOff, zOff);
                    data[AO_OFFSET + index] = Float.floatToRawIntBits(fetchAOAt(world, nearBlock, xOff, yOff, zOff));
                    data[LIGHT_OFFSET + index] = fetchLightAt(world, nearBlock, xOff, yOff, zOff);
                    lt |= (fetchLightTransparencyAt(world, nearBlock, xOff, yOff, zOff) ? 1 : 0) << index;
                }
            }
        }
        data[TRANSPARENCY_OFFSET] = lt;
    }

    private static float fetchAOAt(IBlockAccess world, Block block, int x, int y, int z) {
        return Compat.getAmbientOcclusionLightValue(block, x, y, z, world);
    }

    private static int fetchLightAt(IBlockAccess world, Block block, int x, int y, int z) {
        return world.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(world, x, y, z));
    }

    private static boolean fetchLightTransparencyAt(IBlockAccess world, Block block, int x, int y, int z) {
        val op = block.getLightOpacity(world, x, y, z);
        return op < 15;
    }

    private static int toIndex(int x, int y, int z) {
        return (y * 3 + z) * 3 + x;
    }
}
