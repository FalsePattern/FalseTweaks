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

import static com.falsepattern.falsetweaks.modules.ao.AOMath.averageAO;
import static com.falsepattern.falsetweaks.modules.ao.AOMath.biLerpAO;
import static com.falsepattern.falsetweaks.modules.ao.AOMath.lerpAO;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.averageBrightness;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.biLerpBrightness;
import static com.falsepattern.falsetweaks.modules.ao.BrightnessMath.lerpBrightness;

@RequiredArgsConstructor
public class AmbientOcclusionRenderer implements AORenderer {
    private static final Facing.Direction[] dirs = Facing.Direction.values();
    private static final int SECTION_SIZE = 3 * 3 * 3;
    private static final int LIGHT_OFFSET = SECTION_SIZE;
    private static final int INSET_OFFSET = LIGHT_OFFSET + SECTION_SIZE;
    private static final int TRANSPARENCY_OFFSET = INSET_OFFSET + dirs.length;
    private static final int TOTAL_SIZE = TRANSPARENCY_OFFSET + 1;
    private final int[] data = new int[TOTAL_SIZE];
    private float r;
    private float g;
    private float b;

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
        final AOChunkCache aocc;
        if (rb.blockAccess instanceof AOChunkCache) {
            aocc = (AOChunkCache) rb.blockAccess;
            aocc.ft$setUseNeighborBrightness(false);
        } else {
            aocc = null;
        }
        try {
            rb.enableAO = true;
            Compat.tessellator()
                  .setBrightness(0x00f000f0);
            fetchLightingData(rb, block, x, y, z);
            val useColor = !(rb.getBlockIcon(block)
                               .getIconName()
                               .equals("grass_top") || rb.hasOverrideBlockTexture());
            this.r = r;
            this.g = g;
            this.b = b;
            boolean drewSomething;
            drewSomething = renderFace(rb, Facing.YNEG, block, x, y, z, useColor);
            drewSomething |= renderFace(rb, Facing.YPOS, block, x, y, z, useColor);
            drewSomething |= renderFace(rb, Facing.ZNEG, block, x, y, z, useColor);
            drewSomething |= renderFace(rb, Facing.ZPOS, block, x, y, z, useColor);
            drewSomething |= renderFace(rb, Facing.XNEG, block, x, y, z, useColor);
            drewSomething |= renderFace(rb, Facing.XPOS, block, x, y, z, useColor);
            rb.enableAO = false;
            return drewSomething;
        } finally {
            if (aocc != null) {
                aocc.ft$setUseNeighborBrightness(true);
            }
        }
    }

    private boolean renderFace(RenderBlocks rb, Facing facing, Block block, int x, int y, int z, boolean useColor) {
        if (!(rb.renderAllFaces || shouldSideBeRenderedQuick(rb, block, x, y, z, facing))) {
            return false;
        }
        val inset = MathUtil.clamp(getInset(facing.face) * 2, 0, 1);
        val isInset = inset != 0;

        fetchMixedAO(facing, true);
        fetchMixedBrightness(facing, true);
        if (isInset) {
            float aoTopLeftOut = this.aoTopLeft;
            float aoTopRightOut = this.aoTopRight;
            float aoBottomLeftOut = this.aoBottomLeft;
            float aoBottomRightOut = this.aoBottomRight;
            fetchMixedAO(facing, false);

            this.aoTopLeft = lerpAO(aoTopLeftOut, this.aoTopLeft, inset);
            this.aoTopRight = lerpAO(aoTopRightOut, this.aoTopRight, inset);
            this.aoBottomLeft = lerpAO(aoBottomLeftOut, this.aoBottomLeft, inset);
            this.aoBottomRight = lerpAO(aoBottomRightOut, this.aoBottomRight, inset);
            if (!AOFixConfig.stairAOFix) {
                int brightnessTopLeftOut = this.brightnessTopLeft;
                int brightnessTopRightOut = this.brightnessTopRight;
                int brightnessBottomLeftOut = this.brightnessBottomLeft;
                int brightnessBottomRightOut = this.brightnessBottomRight;
                fetchMixedBrightness(facing, false);
                this.brightnessTopLeft = lerpBrightness(brightnessTopLeftOut, this.brightnessTopLeft, inset);
                this.brightnessTopRight = lerpBrightness(brightnessTopRightOut, this.brightnessTopRight, inset);
                this.brightnessBottomLeft = lerpBrightness(brightnessBottomLeftOut, this.brightnessBottomLeft, inset);
                this.brightnessBottomRight = lerpBrightness(brightnessBottomRightOut, this.brightnessBottomRight, inset);
            }
        }

        dispatchRender(rb, facing, block, x, y, z, useColor);

        return true;
    }

    private void fetchMixedBrightness(Facing facing, boolean front) {
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

        val brightnessCenter = getBrightness(x, y, z);
        val brightnessLeft = getBrightness(x, y, z, facing.left);
        val brightnessRight = getBrightness(x, y, z, facing.right);
        val brightnessBottom = getBrightness(x, y, z, facing.bottom);
        val brightnessTop = getBrightness(x, y, z, facing.top);

        val bTransparentLeft = getBrightnessTransparency(x, y, z, facing.left);
        val bTransparentRight = getBrightnessTransparency(x, y, z, facing.right);
        val bTransparentBottom = getBrightnessTransparency(x, y, z, facing.bottom);
        val bTransparentTop = getBrightnessTransparency(x, y, z, facing.top);

        final int brightnessTopLeft;
        if (!bTransparentLeft && !bTransparentTop) {
            brightnessTopLeft = averageBrightness(brightnessLeft, brightnessTop);
        } else {
            brightnessTopLeft = getBrightness(x, y, z, facing.topLeft);
        }

        final int brightnessTopRight;
        if (!bTransparentRight && !bTransparentTop) {
            brightnessTopRight = averageBrightness(brightnessRight, brightnessTop);
        } else {
            brightnessTopRight = getBrightness(x, y, z, facing.topRight);
        }

        final int brightnessBottomRight;
        if (!bTransparentRight && !bTransparentBottom) {
            brightnessBottomRight = averageBrightness(brightnessRight, brightnessBottom);
        } else {
            brightnessBottomRight = getBrightness(x, y, z, facing.bottomRight);
        }

        final int brightnessBottomLeft;
        if (!bTransparentLeft && !bTransparentBottom) {
            brightnessBottomLeft = averageBrightness(brightnessLeft, brightnessBottom);
        } else {
            brightnessBottomLeft = getBrightness(x, y, z, facing.bottomLeft);
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

    private void fetchMixedAO(Facing facing, boolean front) {
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

        val aoCenter = getAO(x, y, z);
        val aoLeft = getAO(x, y, z, facing.left);
        val aoRight = getAO(x, y, z, facing.right);
        val aoBottom = getAO(x, y, z, facing.bottom);
        val aoTop = getAO(x, y, z, facing.top);

        val aoTransparentLeft = aoLeft == 1;
        val aoTransparentRight = aoRight == 1;
        val aoTransparentBottom = aoBottom == 1;
        val aoTransparentTop = aoTop == 1;

        final float lightTopLeft;
        if (!aoTransparentLeft && !aoTransparentTop) {
            lightTopLeft = averageAO(aoLeft, aoTop);
        } else {
            lightTopLeft = getAO(x, y, z, facing.topLeft);
        }

        final float lightTopRight;
        if (!aoTransparentRight && !aoTransparentTop) {
            lightTopRight = averageAO(aoRight, aoTop);
        } else {
            lightTopRight = getAO(x, y, z, facing.topRight);
        }

        final float lightBottomRight;
        if (!aoTransparentRight && !aoTransparentBottom) {
            lightBottomRight = averageAO(aoRight, aoBottom);
        } else {
            lightBottomRight = getAO(x, y, z, facing.bottomRight);
        }

        final float lightBottomLeft;
        if (!aoTransparentLeft && !aoTransparentBottom) {
            lightBottomLeft = averageAO(aoLeft, aoBottom);
        } else {
            lightBottomLeft = getAO(x, y, z, facing.bottomLeft);
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

    private void dispatchRender(RenderBlocks rb, Facing facing, Block block, int x, int y, int z, boolean useColor) {
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
                float r = this.r;
                float g = this.g;
                float b = this.b;
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

    private float getAO(int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getAO(x, y, z);
    }

    private float getAO(int x, int y, int z) {
        val index = toIndex(x, y, z);
        return Float.intBitsToFloat(this.data[index]);
    }

    private int getBrightness(int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getBrightness(x, y, z);
    }
    private int getBrightness(int x, int y, int z) {
        val index = toIndex(x, y, z);
        return data[LIGHT_OFFSET + index];
    }


    private boolean getBrightnessTransparency(int x, int y, int z, Vector3ic offset) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        return getBrightnessTransparency(x, y, z);
    }
    private boolean getBrightnessTransparency(int x, int y, int z) {
        val index = toIndex(x, y, z);
        val i = this.data[TRANSPARENCY_OFFSET];
        return ((i >>> index) & 1) != 0;
    }


    private boolean shouldSideBeRenderedQuick(RenderBlocks rb, Block block, int x, int y, int z, Facing facing) {
        try {
            return block.shouldSideBeRendered(rb.blockAccess,
                                              x + facing.front.x(),
                                              y + facing.front.y(),
                                              z + facing.front.z(),
                                              facing.face.ordinal());
        } catch (Throwable ignored) {
            return true;
        }
    }

    private void fetchLightingData(RenderBlocks rb, Block block, int x, int y, int z) {
        //Sample surroundings
        int lt = 0;
        for (int xNear = 0; xNear < 3; xNear++) {
            for (int yNear = 0; yNear < 3; yNear++) {
                for (int zNear = 0; zNear < 3; zNear++) {
                    val xOff = x + xNear - 1;
                    val yOff = y + yNear - 1;
                    val zOff = z + zNear - 1;
                    val index = toIndex(xNear, yNear, zNear);
                    val nearBlock = xNear == 1 && yNear == 1 && zNear == 1 ? block : rb.blockAccess.getBlock(xOff, yOff, zOff);
                    data[index] = Float.floatToRawIntBits(fetchAOAt(rb, nearBlock, xOff, yOff, zOff));
                    data[LIGHT_OFFSET + index] = fetchLightAt(rb, nearBlock, xOff, yOff, zOff);
                    lt |= (fetchLightTransparencyAt(rb, nearBlock, xOff, yOff, zOff) ? 1 : 0) << index;
                }
            }
        }
        data[TRANSPARENCY_OFFSET] = lt;
        for (val dir: dirs) {
            double inset;
            switch (dir) {
                case FACE_YNEG:
                    inset = rb.renderMinY;
                    break;
                case FACE_YPOS:
                    inset = 1 - rb.renderMaxY;
                    break;
                case FACE_ZNEG:
                    inset = rb.renderMinZ;
                    break;
                case FACE_ZPOS:
                    inset = 1 - rb.renderMaxZ;
                    break;
                case FACE_XNEG:
                    inset = rb.renderMinX;
                    break;
                case FACE_XPOS:
                    inset = 1 - rb.renderMaxX;
                    break;
                default:
                    inset = 0;
            }
            data[INSET_OFFSET + dir.ordinal()] = Float.floatToRawIntBits((float) MathUtil.clamp(inset, 0, 1));
        }
    }

    private float fetchAOAt(RenderBlocks rb, Block block, int x, int y, int z) {
        return Compat.getAmbientOcclusionLightValue(block, x, y, z, rb.blockAccess);
    }

    private int fetchLightAt(RenderBlocks rb, Block block, int x, int y, int z) {
        return rb.blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(rb.blockAccess, x, y, z));
    }

    private boolean fetchLightTransparencyAt(RenderBlocks rb, Block block, int x, int y, int z) {
        val op = block.getLightOpacity(rb.blockAccess, x, y, z);
        return op < 15;
    }

    private int toIndex(int x, int y, int z) {
        return (z * 3 + y) * 3 + x;
    }
}
