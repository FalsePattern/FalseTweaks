/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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
package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.config.OcclusionConfig;
import com.falsepattern.falsetweaks.proxy.ClientProxy;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;

import java.nio.FloatBuffer;

@Mixin(ClippingHelperImpl.class)
public abstract class ClippingHelperImplMixin extends ClippingHelper {

    @Shadow
    private FloatBuffer projectionMatrixBuffer;

    @Shadow
    private FloatBuffer modelviewMatrixBuffer;

    @Shadow
    private FloatBuffer field_78564_h;

    @Shadow
    protected abstract void normalize(float[][] p_78559_1_, int p_78559_2_);

    @Unique
    private Matrix4f ft$projectionMatrix;
    @Unique
    private Matrix4f ft$modelViewMatrix;
    @Unique
    private Matrix4f ft$clippingMatrix;

    @Redirect(method = "getInstance",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;init()V"),
              require = 1)
    private static void initOncePerFrame(ClippingHelperImpl instance) {
        if (OcclusionConfig.AGGRESSIVE_CLIPPING_HELPER_OPTIMIZATIONS) {
            if (!ClientProxy.clippingHelperShouldInit) {
                return;
            }
            ClientProxy.clippingHelperShouldInit = false;
        }
        instance.init();
    }

    /**
     * @author FalsePattern
     * @reason JOML
     */
    @Overwrite
    public void init() {
        if (ft$projectionMatrix == null) {
            ft$projectionMatrix = new Matrix4f();
            ft$modelViewMatrix = new Matrix4f();
            ft$clippingMatrix = new Matrix4f();
        }
        projectionMatrixBuffer.clear();
        modelviewMatrixBuffer.clear();
        field_78564_h.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixBuffer);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewMatrixBuffer);
        projectionMatrixBuffer.flip().limit(16);
        modelviewMatrixBuffer.flip().limit(16);
        ft$projectionMatrix.set(projectionMatrixBuffer);
        ft$modelViewMatrix.set(modelviewMatrixBuffer);
        ft$projectionMatrix.mul(ft$modelViewMatrix, ft$clippingMatrix);
        ft$clippingMatrix.get(this.clippingMatrix);

        this.frustum[0][0] = this.clippingMatrix[3] - this.clippingMatrix[0];
        this.frustum[0][1] = this.clippingMatrix[7] - this.clippingMatrix[4];
        this.frustum[0][2] = this.clippingMatrix[11] - this.clippingMatrix[8];
        this.frustum[0][3] = this.clippingMatrix[15] - this.clippingMatrix[12];
        this.normalize(this.frustum, 0);
        this.frustum[1][0] = this.clippingMatrix[3] + this.clippingMatrix[0];
        this.frustum[1][1] = this.clippingMatrix[7] + this.clippingMatrix[4];
        this.frustum[1][2] = this.clippingMatrix[11] + this.clippingMatrix[8];
        this.frustum[1][3] = this.clippingMatrix[15] + this.clippingMatrix[12];
        this.normalize(this.frustum, 1);
        this.frustum[2][0] = this.clippingMatrix[3] + this.clippingMatrix[1];
        this.frustum[2][1] = this.clippingMatrix[7] + this.clippingMatrix[5];
        this.frustum[2][2] = this.clippingMatrix[11] + this.clippingMatrix[9];
        this.frustum[2][3] = this.clippingMatrix[15] + this.clippingMatrix[13];
        this.normalize(this.frustum, 2);
        this.frustum[3][0] = this.clippingMatrix[3] - this.clippingMatrix[1];
        this.frustum[3][1] = this.clippingMatrix[7] - this.clippingMatrix[5];
        this.frustum[3][2] = this.clippingMatrix[11] - this.clippingMatrix[9];
        this.frustum[3][3] = this.clippingMatrix[15] - this.clippingMatrix[13];
        this.normalize(this.frustum, 3);
        this.frustum[4][0] = this.clippingMatrix[3] - this.clippingMatrix[2];
        this.frustum[4][1] = this.clippingMatrix[7] - this.clippingMatrix[6];
        this.frustum[4][2] = this.clippingMatrix[11] - this.clippingMatrix[10];
        this.frustum[4][3] = this.clippingMatrix[15] - this.clippingMatrix[14];
        this.normalize(this.frustum, 4);
        this.frustum[5][0] = this.clippingMatrix[3] + this.clippingMatrix[2];
        this.frustum[5][1] = this.clippingMatrix[7] + this.clippingMatrix[6];
        this.frustum[5][2] = this.clippingMatrix[11] + this.clippingMatrix[10];
        this.frustum[5][3] = this.clippingMatrix[15] + this.clippingMatrix[14];
        this.normalize(this.frustum, 5);
    }
}
