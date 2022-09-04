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

import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.calibration.CalibrationConfig;
import com.falsepattern.triangulator.mixin.helper.IRenderBlocksMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import com.falsepattern.triangulator.renderblocks.Facing;
import com.falsepattern.triangulator.renderblocks.IFaceRenderer;
import com.falsepattern.triangulator.renderblocks.RenderState;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksUltraMixin implements IRenderBlocksMixin {
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
    public double renderMinX;
    @Shadow
    public double renderMinY;
    @Shadow
    public double renderMinZ;
    @Shadow
    public double renderMaxX;
    @Shadow
    public double renderMaxY;
    @Shadow
    public double renderMaxZ;

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
    private RenderState state;
    private Vector3ic frontDir;
    private boolean[] states;
    private double[] bounds;
    @Setter
    private boolean reusePreviousStates;

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

    private int getMixedBrightnessForBlockOffset(int x, int y, int z, Vector3ic offset, Facing.Direction face) {
        return getMixedBrightnessForBlockOffset(x, y, z, offset, false, face);
    }

    private int getMixedBrightnessForBlockOffset(int x, int y, int z, Vector3ic offset, boolean front, Facing.Direction face) {
        x += offset.x();
        y += offset.y();
        z += offset.z();
        Block block = blockAccess.getBlock(x, y, z);
        int l = blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));

        if (block instanceof BlockSlab) {
            Boolean selfTopSlab = null;
            Boolean frontTopSlab = null;
            if (state.block instanceof BlockSlab) {
                selfTopSlab = (blockAccess.getBlockMetadata(state.x, state.y, state.z) & 8) != 0;
            }
            Block frontBlock = blockAccess.getBlock(state.x + this.frontDir.x(),
                                                    state.y + this.frontDir.y(),
                                                    state.z + this.frontDir.z());
            if (frontBlock instanceof BlockSlab) {
                frontTopSlab = (blockAccess.getBlockMetadata(state.x + this.frontDir.x(),
                                                             state.y + this.frontDir.y(),
                                                             state.z + this.frontDir.z()) & 8) != 0;
            }
            if (block.isOpaqueCube()) {
                return 0;
            }
            boolean topSlab = (blockAccess.getBlockMetadata(x, y, z) & 8) != 0;
            switch (face) {
                case FACE_YNEG:
                    if (selfTopSlab != null) {
                        if (offset.y() == 0) {
                            if (selfTopSlab && topSlab) {
                                if (frontTopSlab == null || frontTopSlab) {
                                    y--;
                                }
                            } else if (!selfTopSlab && !topSlab) {
                                if (frontTopSlab == null || !frontTopSlab) {
                                    y++;
                                }
                            } else {
                                return 0;
                            }
                        } else if (front && frontTopSlab != null) {
                            y++;
                        }
                    } else if (topSlab) {
                        y--;
                    }
                    break;
                case FACE_YPOS:
                    if (selfTopSlab != null) {
                        if (offset.y() == 0) {
                            if (selfTopSlab && topSlab) {
                                if (frontTopSlab == null || frontTopSlab) {
                                    y--;
                                }
                            } else if (!selfTopSlab && !topSlab) {
                                if (frontTopSlab == null || !frontTopSlab) {
                                    y++;
                                }
                            } else {
                                return 0;
                            }
                        } else if (front && frontTopSlab != null) {
                            y--;
                        }
                    } else if (!topSlab) {
                        y++;
                    }
                    break;
                case FACE_ZNEG:
                case FACE_ZPOS:
                case FACE_XNEG:
                case FACE_XPOS:
                    if (offset.y() < 0) {
                        y++;
                    } else if (offset.y() > 0) {
                        y--;
                    } else if (selfTopSlab != null) {
                        if (selfTopSlab && topSlab) {
                            if (frontTopSlab != null && !frontTopSlab) {
                                return 0;
                            }
                            y--;
                        } else if (!selfTopSlab && !topSlab) {
                            if (frontTopSlab != null && frontTopSlab) {
                                return 0;
                            }
                            y++;
                        } else {
                            return 0;
                        }
                    } else if (topSlab) {
                        y--;
                    } else {
                        y++;
                    }
                    break;
            }

            block = blockAccess.getBlock(x, y, z);
            return blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
        } else if (block instanceof BlockStairs) {
            if (front) {
                return l;
            } else if (face == Facing.Direction.FACE_YNEG || face == Facing.Direction.FACE_YPOS) {
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
            return l;
        }

    }

    private int getBlockMetadataOffset(int x, int y, int z, Vector3ic offset) {
        return blockAccess.getBlockMetadata(x + offset.x(), y + offset.y(), z + offset.z());
    }

    private boolean shouldSideBeRenderedQuick(Block block, int x, int y, int z, Facing facing) {
        return block.shouldSideBeRendered(blockAccess, x + facing.front.x(), y + facing.front.y(), z + facing.front.z(), facing.face.ordinal());
    }

    private boolean getIsTransparent(int x, int y, int z, Vector3ic offset, Facing.Direction dir) {
        Block block = getBlockOffset(x, y, z, offset);
        boolean materialTransparent = block.getCanBlockGrass();
        if (block instanceof BlockSlab) {
            boolean topSlab = (getBlockMetadataOffset(x, y, z, offset) & 8) != 0;
            return (!topSlab || dir != Facing.Direction.FACE_YNEG) && (topSlab || dir != Facing.Direction.FACE_YPOS) || materialTransparent;
        } else {
            return materialTransparent;
        }
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

        frontDir = facing.front;
        int light = getMixedBrightnessForBlockOffset(x, y, z, facing.front, true, facing.face);

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
        boolean transparentLeft = getIsTransparent(x, y, z, facing.left, facing.face);
        boolean transparentRight = getIsTransparent(x, y, z, facing.right, facing.face);
        boolean transparentBottom = getIsTransparent(x, y, z, facing.bottom, facing.face);
        boolean transparentTop = getIsTransparent(x, y, z, facing.top, facing.face);

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
        if (useCustomColor || facing.face == Facing.Direction.FACE_YPOS) {
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
        IIcon icon = getBlockIcon(block, blockAccess, x, y, z, facing.face.ordinal());
        renderer.render(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, facing.face.ordinal()));
        if (facing.face != Facing.Direction.FACE_YNEG && facing.face != Facing.Direction.FACE_YPOS) {
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

    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public boolean renderStandardBlockWithAmbientOcclusionPartial(Block block, int x, int y, int z, float r, float g, float b) {
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

    @Inject(method = {"<init>()V", "<init>(Lnet/minecraft/world/IBlockAccess;)V"},
            at = @At(value = "RETURN"),
            require = 2)
    private void setupStates(CallbackInfo ci) {
        states = new boolean[6];
    }

    private void reuse(Facing.Direction dir) {
        if (reusePreviousStates) {
            ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(states[dir.ordinal()]);
        } else {
            states[dir.ordinal()] = ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation();
        }
    }

    private static float avg(final float a, final float b) {
        return (a + b) * 0.5F;
    }

    private static float avg(final float r, final float g, final float b) {
        return (r + g + b) * 0.3333333333333333F;
    }

    private static float diff(final float a, final float b) {
        return Math.abs(a - b);
    }

    private void aoFix() {
        if (reusePreviousStates) {
            return;
        }
        var avgTopLeft = avg(colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft);
        var avgBottomLeft = avg(colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft);
        var avgBottomRight = avg(colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight);
        var avgTopRight = avg(colorRedTopRight, colorGreenTopRight, colorBlueTopRight);
        if (((ToggleableTessellator) Tessellator.instance).isTriangulatorDisabled() &&
            CalibrationConfig.FLIP_DIAGONALS) {
            var tmp = avgTopLeft;
            avgTopLeft = avgBottomLeft;
            avgBottomLeft = tmp;
            tmp = avgTopRight;
            avgTopRight = avgBottomRight;
            avgBottomRight = tmp;
        }
        val mainDiagonalDiff = diff(avgTopLeft, avgBottomRight);
        val altDiagonalDiff = diff(avgBottomLeft, avgTopRight);
        if (Math.abs(mainDiagonalDiff - altDiagonalDiff) < 0.01) {
            val mainDiagonalAvg = avg(avgTopLeft, avgBottomRight);
            val altDiagonalAvg = avg(avgBottomLeft, avgTopRight);
            if (Math.abs(mainDiagonalAvg - altDiagonalAvg) > 0.01 && mainDiagonalAvg < altDiagonalAvg) {
                ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(true);
                return;
            }
        } else if (altDiagonalDiff < mainDiagonalDiff) {
            ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(true);
            return;
        }
        ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(false);
    }

    @Inject(method = {"renderFaceXNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_XNEG);
    }

    @Redirect(method = "renderFaceXNeg",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                                    ordinal = 0),
                           require = 1)
    private double xNegBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_XNEG);
        return instance.renderMinX;
    }

    @Inject(method = {"renderFaceXPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_XPOS);
    }

    @Redirect(method = "renderFaceXPos",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D",
                                    ordinal = 0),
                           require = 1)
    private double xPosBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_XPOS);
        return instance.renderMaxX;
    }

    @Inject(method = {"renderFaceYNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_YNEG);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Redirect(method = "renderFaceYNeg",
              at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                                    ordinal = 5),
              require = 1)
    private double yNegBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_YNEG);
        return instance.renderMinX;
    }

    @Inject(method = {"renderFaceYPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_YPOS);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Redirect(method = "renderFaceYPos",
              at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                                    ordinal = 5),
              require = 1)
    private double yPosBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_YPOS);
        return instance.renderMinX;
    }

    @Inject(method = {"renderFaceZNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_ZNEG);
    }

    @Redirect(method = "renderFaceZNeg",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                                    ordinal = 6),
                           require = 1)
    private double zNegBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_ZNEG);
        return instance.renderMinX;
    }

    @Inject(method = {"renderFaceZPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        aoFix();
        reuse(Facing.Direction.FACE_ZPOS);
    }

    @Redirect(method = "renderFaceZPos",
              at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                                    ordinal = 5),
              require = 1)
    private double zPosBounds(RenderBlocks instance) {
        preBounds(Facing.Direction.FACE_ZPOS);
        return instance.renderMinX;
    }

    private void preBounds(Facing.Direction skipDir) {
        if (bounds == null) {
            bounds = new double[6];
        }
        bounds[0] = renderMinX;
        bounds[1] = renderMinY;
        bounds[2] = renderMinZ;
        bounds[3] = renderMaxX;
        bounds[4] = renderMaxY;
        bounds[5] = renderMaxZ;
        renderMinX -= 0.0001;
        renderMinY -= 0.0001;
        renderMinZ -= 0.0001;
        renderMaxX += 0.0001;
        renderMaxY += 0.0001;
        renderMaxZ += 0.0001;
        switch (skipDir) {
            case FACE_XNEG: renderMinX = bounds[0]; break;
            case FACE_YNEG: renderMinY = bounds[1]; break;
            case FACE_ZNEG: renderMinZ = bounds[2]; break;
            case FACE_XPOS: renderMaxX = bounds[3]; break;
            case FACE_YPOS: renderMaxY = bounds[4]; break;
            case FACE_ZPOS: renderMaxZ = bounds[5]; break;
        }
    }

    @Inject(method = {"renderFaceXNeg", "renderFaceXPos", "renderFaceYNeg", "renderFaceYPos", "renderFaceZNeg", "renderFaceZPos"},
            at = @At(value = "RETURN"),
            require = 6)
    private void postBounds(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        renderMinX = bounds[0];
        renderMinY = bounds[1];
        renderMinZ = bounds[2];
        renderMaxX = bounds[3];
        renderMaxY = bounds[4];
        renderMaxZ = bounds[5];
    }
}
