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

package com.falsepattern.falsetweaks.mixin.mixins.client.crackfix;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.api.PassTrackingTessellator;
import com.falsepattern.falsetweaks.config.CrackFixConfig;
import com.falsepattern.falsetweaks.modules.triangulator.renderblocks.Facing;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

import java.util.Arrays;
import java.util.Objects;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {
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

    @Unique
    private double[] ft$crack$bounds;
    @Unique
    private boolean ft$crack$disable;
    @Unique
    private static String[] ft$crack$currentBlacklistArr;
    @Unique
    private static Class<?>[] ft$crack$currentBlacklistClasses;

    @Unique
    private static boolean ft$crack$isBlacklisted(Class<?> clazz) {
        val blacklist = ft$crack$getBlacklist();
        if (blacklist == null) {
            return false;
        }
        for (val element : blacklist) {
            if (element.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
    @Unique

    private static Class<?>[] ft$crack$getBlacklist() {
        if (ft$crack$currentBlacklistArr != CrackFixConfig.blacklist) {
            ft$crack$currentBlacklistArr = CrackFixConfig.blacklist;
            ft$crack$currentBlacklistClasses = Arrays.stream(ft$crack$currentBlacklistArr)
                                                     .map((name) -> {
                                                        try {
                                                            return Class.forName(name);
                                                        } catch (ClassNotFoundException e) {
                                                            Share.log.info("Could not find class " +
                                                                           name +
                                                                           " for crack fix blacklist!");
                                                            return null;
                                                        }
                                                    })
                                                     .filter(Objects::nonNull)
                                                     .toArray(Class<?>[]::new);
        }
        return ft$crack$currentBlacklistClasses;
    }

    @Redirect(method = "renderFaceXNeg",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                       ordinal = 0),
              require = 1)
    private double xNegBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_XNEG);
        return instance.renderMinX;
    }

    @Redirect(method = "renderFaceXPos",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D",
                       ordinal = 0),
              require = 1)
    private double xPosBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_XPOS);
        return instance.renderMaxX;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Redirect(method = "renderFaceYNeg",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                       ordinal = 5),
              require = 1)
    private double yNegBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_YNEG);
        return instance.renderMinX;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Redirect(method = "renderFaceYPos",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                       ordinal = 5),
              require = 1)
    private double yPosBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_YPOS);
        return instance.renderMinX;
    }

    @Redirect(method = "renderFaceZNeg",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                       ordinal = 6),
              require = 1)
    private double zNegBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_ZNEG);
        return instance.renderMinX;
    }

    @Redirect(method = "renderFaceZPos",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D",
                       ordinal = 5),
              require = 1)
    private double zPosBounds(RenderBlocks instance) {
        ft$crack$preBounds(Facing.Direction.FACE_ZPOS);
        return instance.renderMinX;
    }

    @Unique
    private void ft$crack$preBounds(Facing.Direction skipDir) {
        if (ft$crack$disable) {
            return;
        }
        if (ft$crack$bounds == null) {
            ft$crack$bounds = new double[6];
        }
        ft$crack$bounds[0] = renderMinX;
        ft$crack$bounds[1] = renderMinY;
        ft$crack$bounds[2] = renderMinZ;
        ft$crack$bounds[3] = renderMaxX;
        ft$crack$bounds[4] = renderMaxY;
        ft$crack$bounds[5] = renderMaxZ;

        val tess = (PassTrackingTessellator) Compat.tessellator();
        if (tess.pass() != 0) {
            return;
        }

        if (renderMinX != 0 ||
            renderMinY != 0 ||
            renderMinZ != 0 ||
            renderMaxX != 1 ||
            renderMaxY != 1 ||
            renderMaxZ != 1) {
            return;
        }
        val EPSILON = CrackFixConfig.epsilon;
        renderMinX -= EPSILON;
        renderMinY -= EPSILON;
        renderMinZ -= EPSILON;
        renderMaxX += EPSILON;
        renderMaxY += EPSILON;
        renderMaxZ += EPSILON;
        switch (skipDir) {
            // @formatter:off
            case FACE_XNEG: renderMinX = ft$crack$bounds[0]; break;
            case FACE_YNEG: renderMinY = ft$crack$bounds[1]; break;
            case FACE_ZNEG: renderMinZ = ft$crack$bounds[2]; break;
            case FACE_XPOS: renderMaxX = ft$crack$bounds[3]; break;
            case FACE_YPOS: renderMaxY = ft$crack$bounds[4]; break;
            case FACE_ZPOS: renderMaxZ = ft$crack$bounds[5]; break;
            // @formatter:on
        }
    }

    @Inject(method = {"renderFaceXNeg",
                      "renderFaceXPos",
                      "renderFaceYNeg",
                      "renderFaceYPos",
                      "renderFaceZNeg",
                      "renderFaceZPos"},
            at = @At(value = "RETURN"),
            require = 6)
    private void postBounds(Block p_147798_1_,
                            double p_147798_2_,
                            double p_147798_4_,
                            double p_147798_6_,
                            IIcon p_147798_8_,
                            CallbackInfo ci) {
        if (ft$crack$disable || ft$crack$bounds == null) {
            return;
        }
        renderMinX = ft$crack$bounds[0];
        renderMinY = ft$crack$bounds[1];
        renderMinZ = ft$crack$bounds[2];
        renderMaxX = ft$crack$bounds[3];
        renderMaxY = ft$crack$bounds[4];
        renderMaxZ = ft$crack$bounds[5];
    }

    @Inject(method = "renderBlockByRenderType",
            at = @At("HEAD"),
            require = 1)
    private void exclusion(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        ft$crack$disable = ft$crack$isBlacklisted(block.getClass());
    }

    @Inject(method = "renderBlockByRenderType",
            at = @At("RETURN"),
            require = 1)
    private void endExclusion(Block p_147805_1_,
                              int p_147805_2_,
                              int p_147805_3_,
                              int p_147805_4_,
                              CallbackInfoReturnable<Boolean> cir) {
        ft$crack$disable = false;
    }
}
