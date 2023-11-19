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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

import java.nio.FloatBuffer;

import static com.falsepattern.falsetweaks.config.TranslucentBlockLayersConfig.TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON;


/**
 * Allows translucent layers of blocks to render on the same plane as opaque.
 */
@Mixin(RenderGlobal.class)
public abstract class TranslucentBlockLayers_RenderGlobalMixin {
    private FloatBuffer fltw$matrixBuffer;

    /**
     * Moves the Z-Clip towards the camera by the {@link com.falsepattern.falsetweaks.config.TranslucentBlockLayersConfig#TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON epsilon} value when rendering translucent blocks.
     *
     * @param entityLivingBase Entity
     * @param renderPass       Render pass
     * @param partialTick      Partial tick
     * @param cir              Mixin callback
     */
    @Inject(method = "sortAndRender",
            at = @At("HEAD"),
            require = 1)
    private void offsetProjection(EntityLivingBase entityLivingBase, int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        if (renderPass != 1) {
            return;
        }

        if (fltw$matrixBuffer == null) {
            fltw$matrixBuffer = BufferUtils.createFloatBuffer(16);
        }

        val lastMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_PROJECTION);

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, fltw$matrixBuffer);

        GL11.glPushMatrix();

        GL11.glLoadIdentity();
        GL11.glScalef(1F, 1F, 1F / (1F - (float) TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON));
        GL11.glTranslatef(0F, 0F, -(float) TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON);
        GL11.glMultMatrix(fltw$matrixBuffer);

        GL11.glMatrixMode(lastMatrixMode);
    }

    /**
     * Resets the projection after rendering translucent blocks.
     *
     * @param entityLivingBase Entity
     * @param renderPass       Render pass
     * @param partialTick      Partial tick
     * @param cir              Mixin callback
     */
    @Inject(method = "sortAndRender",
            at = @At("RETURN"),
            require = 1)
    private void resetProjection(EntityLivingBase entityLivingBase, int renderPass, double partialTick, CallbackInfoReturnable<Integer> cir) {
        if (renderPass != 1) {
            return;
        }

        val lastMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glMatrixMode(lastMatrixMode);
    }
}
