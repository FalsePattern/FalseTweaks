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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import com.falsepattern.falsetweaks.modules.misc.TranslucentBlockLayers;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

import static com.falsepattern.falsetweaks.config.TranslucentBlockLayersConfig.ENABLED;


/**
 * Allows translucent layers of blocks to render on the same plane as opaque.
 */
@Mixin(RenderGlobal.class)
public abstract class TranslucentBlockLayers_RenderGlobalMixin {
    private TranslucentBlockLayers ft$tbl;

    private boolean doResetMatrix;

    /**
     * Vanilla hook
     */
    @Inject(method = "sortAndRender",
            at = @At("HEAD"),
            require = 1)
    private void offsetProjection(EntityLivingBase entityLivingBase, int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        ft$offsetProjection(renderPass);
    }

    /**
     * OptiFine hook
     */
    @Dynamic
    @Inject(method = "renderAllSortedRenderers",
            at = @At("HEAD"),
            remap = false,
            require = 0,
            expect = 0)
    private void offsetProjection(int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        ft$offsetProjection(renderPass);
    }

    /**
     * Vanilla hook
     */
    @Inject(method = "sortAndRender",
            at = @At("RETURN"),
            require = 1)
    private void resetProjection(EntityLivingBase entityLivingBase, int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        ft$resetProjection();
    }

    /**
     * OptiFine hook
     */
    @Dynamic
    @Inject(method = "renderAllSortedRenderers",
            at = @At("RETURN"),
            remap = false,
            require = 0,
            expect = 0)
    private void resetProjection(int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        ft$resetProjection();
    }

    /**
     * Moves the Z-Clip towards the camera by the {@link com.falsepattern.falsetweaks.config.TranslucentBlockLayersConfig#TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON epsilon} value when rendering translucent blocks.
     */
    @Unique
    private void ft$offsetProjection(int renderPass) {
        if (!ENABLED || renderPass != 1) {
            doResetMatrix = false;
            return;
        }

        doResetMatrix = true;

        if (ft$tbl == null) {
            ft$tbl = new TranslucentBlockLayers();
        }
        ft$tbl.offsetProjection();
    }

    /**
     * Resets the projection after rendering translucent blocks.
     */
    @Unique
    private void ft$resetProjection() {
        if (!doResetMatrix) {
            return;
        }

        doResetMatrix = false;

        ft$tbl.resetProjection();
    }
}
