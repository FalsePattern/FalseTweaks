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

package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.renderblocks.Facing;
import com.falsepattern.triangulator.renderblocks.IFaceRenderer;
import com.falsepattern.triangulator.renderblocks.RenderState;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksUltraMixin {
    @Shadow
    public static boolean fancyGrass;
    @Shadow
    public boolean enableAO;
    @Shadow
    public IBlockAccess blockAccess;
    @Shadow
    public boolean renderAllFaces;
    @Shadow(aliases = "colorRedTopLeftF")
    public float colorRedTopLeft;
    @Shadow(aliases = "colorGreenTopLeftF")
    public float colorGreenTopLeft;
    @Shadow(aliases = "colorBlueTopLeftF")
    public float colorBlueTopLeft;
    @Shadow(aliases = "colorRedBottomLeftF")
    public float colorRedBottomLeft;
    @Shadow(aliases = "colorGreenBottomLeftF")
    public float colorGreenBottomLeft;
    @Shadow(aliases = "colorBlueBottomLeftF")
    public float colorBlueBottomLeft;
    @Shadow(aliases = "colorRedBottomRightF")
    public float colorRedBottomRight;
    @Shadow(aliases = "colorGreenBottomRightF")
    public float colorGreenBottomRight;
    @Shadow(aliases = "colorBlueBottomRightF")
    public float colorBlueBottomRight;
    @Shadow(aliases = "colorRedTopRightF")
    public float colorRedTopRight;
    @Shadow(aliases = "colorGreenTopRightF")
    public float colorGreenTopRight;
    @Shadow(aliases = "colorBlueTopRightF")
    public float colorBlueTopRight;

    @Shadow
    public int brightnessTopLeft;
    @Shadow
    public int brightnessBottomLeft;
    @Shadow
    public int brightnessBottomRight;
    @Shadow
    public int brightnessTopRight;
    int countS;
    int countB;
    float lightSky;
    float lightBlock;
    private Boolean frontSlab = null;
    private RenderState state;

    @Shadow
    public abstract IIcon getBlockIcon(Block p_147793_1_, IBlockAccess p_147793_2_, int p_147793_3_, int p_147793_4_, int p_147793_5_, int p_147793_6_);

    @Shadow
    public abstract IIcon getBlockIcon(Block p_147745_1_);

    @Shadow
    public abstract boolean hasOverrideBlockTexture();

    @Shadow
    public abstract void renderFaceYNeg(Block p_147768_1_, double p_147768_2_, double p_147768_4_, double p_147768_6_, IIcon p_147768_8_);

    @Shadow
    public abstract void renderFaceYPos(Block p_147806_1_, double p_147806_2_, double p_147806_4_, double p_147806_6_, IIcon p_147806_8_);

    @Shadow
    public abstract void renderFaceZNeg(Block p_147761_1_, double p_147761_2_, double p_147761_4_, double p_147761_6_, IIcon p_147761_8_);

    @Shadow
    public abstract void renderFaceZPos(Block p_147734_1_, double p_147734_2_, double p_147734_4_, double p_147734_6_, IIcon p_147734_8_);

    @Shadow
    public abstract void renderFaceXNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_);

    @Shadow
    public abstract void renderFaceXPos(Block p_147764_1_, double p_147764_2_, double p_147764_4_, double p_147764_6_, IIcon p_147764_8_);

    private void addLight(int light) {
        int S = light & 0xff;
        int B = (light & 0xff0000) >>> 16;
        if (S != 0) {
            lightSky += S;
            countS++;
        }
        if (B != 0) {
            lightBlock += B;
            countB++;
        }
    }

    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public int getAoBrightness(int a, int b, int c, int d) {
        countS = 0;
        countB = 0;
        lightSky = 0;
        lightBlock = 0;
        addLight(a);
        addLight(b);
        addLight(c);
        addLight(d);
        lightSky /= countS;
        lightBlock /= countB;
        return (((int) lightSky) & 0xff) | ((((int) lightBlock) & 0xff) << 16);
    }

    private Block getBlockOffset(int x, int y, int z, Vector3ic offset) {
        return blockAccess.getBlock(x + offset.x(), y + offset.y(), z + offset.z());
    }

    private int getMixedBrightnessForBlockOffset(int x, int y, int z, Vector3ic offset, int face) {
        return getMixedBrightnessForBlockOffset(x, y, z, offset, false, face);
    }

    private int getMixedBrightnessForBlockOffset(int x, int y, int z, Vector3ic offset, boolean front, int face) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        Block block = blockAccess.getBlock(x, y, z);
        int l = blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));

        if (block instanceof BlockSlab) {
            if (block.isOpaqueCube()) {
                return 0;
            }
            boolean topSlab = (blockAccess.getBlockMetadata(x, y, z) & 8) != 0;
            switch (face) {
                case 0:
                    if (topSlab) {
                        y--;
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if (front) {
                        frontSlab = null;
                    }
                    if (offset.y() < 0) {
                        y++;
                    } else if (offset.y() > 0) {
                        y--;
                    } else if (frontSlab != null) {
                        if (frontSlab) {
                            y--;
                        } else {
                            y++;
                        }
                    } else if (topSlab) {
                        y--;
                        if (front) {
                            frontSlab = true;
                        }
                    } else {
                        y++;
                        if (front) {
                            frontSlab = false;
                        }
                    }
                    break;
            }


            block = blockAccess.getBlock(x, y, z);
            return blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
        } else if (block instanceof BlockStairs) {
            if (front) {
                return l;
            } else if (face <= 2) {
                x -= offset.x();
                y -= offset.y();
                z -= offset.z();
            } else if (offset.y() < 0) {
                y++;
            } else if (offset.y() > 0) {
                y--;
            } else {
                return 0;
            }
            block = blockAccess.getBlock(x, y, z);
            return blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
        } else {
            if (front) {
                frontSlab = null;
            }
            return l;
        }

    }

    private int getBlockMetadataOffset(int x, int y, int z, Vector3ic offset) {
        return blockAccess.getBlockMetadata(x + offset.x(), y + offset.y(), z + offset.z());
    }

    private boolean shouldSideBeRenderedQuick(Block block, int x, int y, int z, Facing facing) {
        return block.shouldSideBeRendered(blockAccess, x + facing.front.x(), y + facing.front.y(), z + facing.front.z(), facing.face);
    }

    private boolean renderFace(IFaceRenderer renderer, Facing facing) {
        Block block = state.block;
        int x = state.x;
        int y = state.y;
        int z = state.z;
        if (!(this.renderAllFaces || shouldSideBeRenderedQuick(block, x, y, z, facing))) {
            return false;
        }
        boolean shift = facing.shift((RenderBlocks) (Object) this);

        int light = state.light;

        if (shift || !getBlockOffset(x, y, z, facing.front).isOpaqueCube()) {
            light = getMixedBrightnessForBlockOffset(x, y, z, facing.front, true, facing.face);
        }
        if (shift) {
            x += facing.front.x();
            y += facing.front.y();
            z += facing.front.z();
        }

        final float lightLeft, lightTopLeft, lightTop, lightTopRight,
                lightRight, lightBottomRight, lightBottom, lightBottomLeft;
        final int brightnessLeft, brightnessTopLeft, brightnessTop, brightnessTopRight,
                brightnessRight, brightnessBottomRight, brightnessBottom, brightnessBottomLeft;

        lightLeft = getBlockOffset(x, y, z, facing.left).getAmbientOcclusionLightValue();
        lightRight = getBlockOffset(x, y, z, facing.right).getAmbientOcclusionLightValue();
        lightBottom = getBlockOffset(x, y, z, facing.bottom).getAmbientOcclusionLightValue();
        lightTop = getBlockOffset(x, y, z, facing.top).getAmbientOcclusionLightValue();
        brightnessLeft = getMixedBrightnessForBlockOffset(x, y, z, facing.left, facing.face);
        brightnessRight = getMixedBrightnessForBlockOffset(x, y, z, facing.right, facing.face);
        brightnessBottom = getMixedBrightnessForBlockOffset(x, y, z, facing.bottom, facing.face);
        brightnessTop = getMixedBrightnessForBlockOffset(x, y, z, facing.top, facing.face);
        boolean transparentLeft = getBlockOffset(x, y, z, facing.left).getCanBlockGrass();
        boolean transparentRight = getBlockOffset(x, y, z, facing.right).getCanBlockGrass();
        boolean transparentBottom = getBlockOffset(x, y, z, facing.bottom).getCanBlockGrass();
        boolean transparentTop = getBlockOffset(x, y, z, facing.top).getCanBlockGrass();

        if (!transparentLeft && !transparentBottom) {
            lightBottomLeft = lightLeft;
            brightnessBottomLeft = brightnessLeft;
        } else {
            lightBottomLeft = getBlockOffset(x, y, z, facing.bottomLeft).getAmbientOcclusionLightValue();
            brightnessBottomLeft = getMixedBrightnessForBlockOffset(x, y, z, facing.bottomLeft, facing.face);
        }

        if (!transparentLeft && !transparentTop) {
            lightTopLeft = lightLeft;
            brightnessTopLeft = brightnessLeft;
        } else {
            lightTopLeft = getBlockOffset(x, y, z, facing.topLeft).getAmbientOcclusionLightValue();
            brightnessTopLeft = getMixedBrightnessForBlockOffset(x, y, z, facing.topLeft, facing.face);
        }

        if (!transparentRight && !transparentBottom) {
            lightBottomRight = lightRight;
            brightnessBottomRight = brightnessRight;
        } else {
            lightBottomRight = getBlockOffset(x, y, z, facing.bottomRight).getAmbientOcclusionLightValue();
            brightnessBottomRight = getMixedBrightnessForBlockOffset(x, y, z, facing.bottomRight, facing.face);
        }

        if (!transparentRight && !transparentTop) {
            lightTopRight = lightRight;
            brightnessTopRight = brightnessRight;
        } else {
            lightTopRight = getBlockOffset(x, y, z, facing.topRight).getAmbientOcclusionLightValue();
            brightnessTopRight = getMixedBrightnessForBlockOffset(x, y, z, facing.topRight, facing.face);
        }

        if (shift) {
            x -= facing.front.x();
            y -= facing.front.y();
            z -= facing.front.z();
        }

        float aoFront = getBlockOffset(x, y, z, facing.front).getAmbientOcclusionLightValue();
        float aoTopLeft = (lightLeft + lightTopLeft + aoFront + lightTop) / 4.0F;
        float aoTopRight = (aoFront + lightTop + lightRight + lightTopRight) / 4.0F;
        float aoBottomRight = (lightBottom + aoFront + lightBottomRight + lightRight) / 4.0F;
        float aoBottomLeft = (lightBottomLeft + lightLeft + lightBottom + aoFront) / 4.0F;
        this.brightnessTopLeft = getAoBrightness(brightnessLeft, brightnessTopLeft, brightnessTop, light);
        this.brightnessBottomLeft = getAoBrightness(brightnessBottomLeft, brightnessLeft, brightnessBottom, light);
        this.brightnessBottomRight = getAoBrightness(brightnessBottom, brightnessBottomRight, brightnessRight, light);
        this.brightnessTopRight = getAoBrightness(brightnessTop, brightnessRight, brightnessTopRight, light);

        if (facing.worldUp >= 0) {
            Block frontBlock = getBlockOffset(x, y, z, facing.front);
            if (frontBlock instanceof BlockSlab && !frontBlock.isOpaqueCube()) {
                int dir = facing.worldUp ^ ((~getBlockMetadataOffset(x, y, z, facing.front) & 8) >>> 2);
                switch (dir) {
                    case 0:
                        this.brightnessTopLeft = this.brightnessBottomLeft;
                        this.brightnessTopRight = this.brightnessBottomRight;
                        break;
                    case 1:
                        this.brightnessTopRight = this.brightnessTopLeft;
                        this.brightnessBottomRight = this.brightnessBottomLeft;
                        break;
                    case 2:
                        this.brightnessBottomLeft = this.brightnessTopLeft;
                        this.brightnessBottomRight = this.brightnessTopRight;
                        break;
                    case 3:
                        this.brightnessTopLeft = this.brightnessTopRight;
                        this.brightnessBottomLeft = this.brightnessBottomRight;
                        break;
                }
            }
        }

        boolean useCustomColor = state.useCustomColor;
        if (useCustomColor || facing.face == 1) {
            this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = state.r * facing.brightness;
            this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = state.g * facing.brightness;
            this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = state.b * facing.brightness;
        } else {
            this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = facing.brightness;
            this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = facing.brightness;
            this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = facing.brightness;
        }

        this.colorRedTopLeft *= aoTopLeft;
        this.colorGreenTopLeft *= aoTopLeft;
        this.colorBlueTopLeft *= aoTopLeft;
        this.colorRedBottomLeft *= aoBottomLeft;
        this.colorGreenBottomLeft *= aoBottomLeft;
        this.colorBlueBottomLeft *= aoBottomLeft;
        this.colorRedBottomRight *= aoBottomRight;
        this.colorGreenBottomRight *= aoBottomRight;
        this.colorBlueBottomRight *= aoBottomRight;
        this.colorRedTopRight *= aoTopRight;
        this.colorGreenTopRight *= aoTopRight;
        this.colorBlueTopRight *= aoTopRight;
        IIcon icon = getBlockIcon(block, blockAccess, x, y, z, facing.face);
        renderer.render(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, facing.face));
        if (facing.face >= 2) {
            if (fancyGrass && icon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture()) {
                float r = state.r;
                float g = state.g;
                float b = state.b;
                this.colorRedTopLeft *= r;
                this.colorRedBottomLeft *= r;
                this.colorRedBottomRight *= r;
                this.colorRedTopRight *= r;
                this.colorGreenTopLeft *= g;
                this.colorGreenBottomLeft *= g;
                this.colorGreenBottomRight *= g;
                this.colorGreenTopRight *= g;
                this.colorBlueTopLeft *= b;
                this.colorBlueBottomLeft *= b;
                this.colorBlueBottomRight *= b;
                this.colorBlueTopRight *= b;
                renderer.render(block, x, y, z, BlockGrass.getIconSideOverlay());
            }
        }

        return true;
    }

    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public boolean renderStandardBlockWithAmbientOcclusion(Block block, int x, int y, int z, float r, float g, float b) {
        this.enableAO = true;
        int light = block.getMixedBrightnessForBlock(this.blockAccess, x, y, z);
        Tessellator.instance.setBrightness(0x000f000f);

        boolean useColor = !(getBlockIcon(block).getIconName().equals("grass_top") || hasOverrideBlockTexture());

        if (state == null) {
            state = new RenderState();
        }
        state.set(block, x, y, z, r, g, b, useColor, light);
        boolean drewSomething;
        drewSomething = renderFace(this::renderFaceYNeg, Facing.YNEG);
        drewSomething |= renderFace(this::renderFaceYPos, Facing.YPOS);
        drewSomething |= renderFace(this::renderFaceZNeg, Facing.ZNEG);
        drewSomething |= renderFace(this::renderFaceZPos, Facing.ZPOS);
        drewSomething |= renderFace(this::renderFaceXNeg, Facing.XNEG);
        drewSomething |= renderFace(this::renderFaceXPos, Facing.XPOS);

        this.enableAO = false;
        return drewSomething;
    }
}
