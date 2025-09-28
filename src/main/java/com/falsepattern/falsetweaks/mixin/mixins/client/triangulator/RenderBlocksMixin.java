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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.api.triangulator.ToggleableTessellator;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.IRenderBlocksMixin;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import com.falsepattern.falsetweaks.modules.triangulator.renderblocks.Facing;
import lombok.val;
import lombok.var;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

import java.util.Arrays;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin implements IRenderBlocksMixin {
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

    @Unique
    private boolean[] ft$tri$states;
    @Unique
    private boolean[] ft$tri$reusePreviousStates;
    @Unique
    private boolean ft$tri$enableMultiRenderReuse;

    @Override
    public void ft$enableMultiRenderReuse(boolean state) {
        this.ft$tri$enableMultiRenderReuse = state;
    }

    @Override
    public void ft$reusePreviousStates(boolean state) {
        Arrays.fill(ft$tri$reusePreviousStates, state);
    }

    @Inject(method = {"<init>()V", "<init>(Lnet/minecraft/world/IBlockAccess;)V"},
            at = @At(value = "RETURN"),
            require = 2)
    private void setupStates(CallbackInfo ci) {
        ft$tri$states = new boolean[6];
        ft$tri$reusePreviousStates = new boolean[6];
    }

    @Unique
    private void ft$reuseDiagonal(Facing.Direction dir) {
        val ord = dir.ordinal();
        if (ft$tri$reusePreviousStates[ord]) {
            ((ITriangulatorTessellator) Compat.tessellator()).alternativeTriangulation(ft$tri$states[ord]);
        } else {
            ft$tri$states[dir.ordinal()] = ((ITriangulatorTessellator) Compat.tessellator()).alternativeTriangulation();
        }
        if (ft$tri$enableMultiRenderReuse) {
            ft$tri$reusePreviousStates[ord] = true;
        }
    }

    @Unique
    private void ft$setTriangulatorDiagonal(Facing.Direction dir) {
        if (ft$tri$reusePreviousStates[dir.ordinal()]) {
            return;
        }
        var avgTopLeft = (colorRedTopLeft + colorGreenTopLeft + colorBlueTopLeft) / 3f;
        var avgBottomLeft = (colorRedBottomLeft + colorGreenBottomLeft + colorBlueBottomLeft) / 3f;
        var avgBottomRight = (colorRedBottomRight + colorGreenBottomRight + colorBlueBottomRight) / 3f;
        var avgTopRight = (colorRedTopRight + colorGreenTopRight + colorBlueTopRight) / 3f;
        if (((ToggleableTessellator) Compat.tessellator()).isTriangulatorDisabled() &&
            TriangulatorConfig.Calibration.FLIP_DIAGONALS) {
            var tmp = avgTopLeft;
            avgTopLeft = avgBottomLeft;
            avgBottomLeft = tmp;
            tmp = avgTopRight;
            avgTopRight = avgBottomRight;
            avgBottomRight = tmp;
        }
        val mainDiagonalDiff = Math.abs(avgTopLeft - avgBottomRight);
        val altDiagonalDiff = Math.abs(avgBottomLeft - avgTopRight);
        if (Math.abs(mainDiagonalDiff - altDiagonalDiff) < 0.01) {
            val mainDiagonalAvg = (avgTopLeft + avgBottomRight) / 2f;
            val altDiagonalAvg = (avgBottomLeft + avgTopRight) / 2f;
            if (Math.abs(mainDiagonalAvg - altDiagonalAvg) > 0.01 && mainDiagonalAvg < altDiagonalAvg) {
                ((ITriangulatorTessellator) Compat.tessellator()).alternativeTriangulation(true);
                return;
            }
        } else if (altDiagonalDiff < mainDiagonalDiff) {
            ((ITriangulatorTessellator) Compat.tessellator()).alternativeTriangulation(true);
            return;
        }
        ((ITriangulatorTessellator) Compat.tessellator()).alternativeTriangulation(false);
    }

    @Inject(method = {"renderFaceXNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXNeg(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_XNEG);
        ft$reuseDiagonal(Facing.Direction.FACE_XNEG);
    }

    @Inject(method = {"renderFaceXPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXPos(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_XPOS);
        ft$reuseDiagonal(Facing.Direction.FACE_XPOS);
    }

    @Inject(method = {"renderFaceYNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYNeg(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_YNEG);
        ft$reuseDiagonal(Facing.Direction.FACE_YNEG);
    }

    @Inject(method = {"renderFaceYPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYPos(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_YPOS);
        ft$reuseDiagonal(Facing.Direction.FACE_YPOS);
    }

    @Inject(method = {"renderFaceZNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZNeg(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_ZNEG);
        ft$reuseDiagonal(Facing.Direction.FACE_ZNEG);
    }

    @Inject(method = {"renderFaceZPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZPos(Block p_147798_1_,
                           double p_147798_2_,
                           double p_147798_4_,
                           double p_147798_6_,
                           IIcon p_147798_8_,
                           CallbackInfo ci) {
        ft$setTriangulatorDiagonal(Facing.Direction.FACE_ZPOS);
        ft$reuseDiagonal(Facing.Direction.FACE_ZPOS);
    }
}
