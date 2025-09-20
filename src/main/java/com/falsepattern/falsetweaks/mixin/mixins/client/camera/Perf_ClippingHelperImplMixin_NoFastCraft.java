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
package com.falsepattern.falsetweaks.mixin.mixins.client.camera;

import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;

import java.nio.FloatBuffer;

@Mixin(ClippingHelperImpl.class)
public abstract class Perf_ClippingHelperImplMixin_NoFastCraft extends ClippingHelper {

    @Shadow
    private FloatBuffer projectionMatrixBuffer;

    @Shadow
    private FloatBuffer modelviewMatrixBuffer;

    @Shadow
    private FloatBuffer field_78564_h;
    @Unique
    private Matrix4f ft$projectionMatrix;
    @Unique
    private Matrix4f ft$modelViewMatrix;
    @Unique
    private Matrix4f ft$clippingMatrix;

    @Shadow
    protected abstract void normalize(float[][] p_78559_1_, int p_78559_2_);

    /**
     * @author FalsePattern
     * @reason JOML
     */
    @Overwrite
    public void init() {
        final Matrix4f projectionMatrix;
        final Matrix4f modelViewMatrix;
        final Matrix4f clippingMatrix;
        if (this.ft$projectionMatrix == null) {
            this.ft$projectionMatrix = projectionMatrix = new Matrix4f();
            this.ft$modelViewMatrix = modelViewMatrix = new Matrix4f();
            this.ft$clippingMatrix = clippingMatrix = new Matrix4f();
        } else {
            projectionMatrix = this.ft$projectionMatrix;
            modelViewMatrix = this.ft$modelViewMatrix;
            clippingMatrix = this.ft$clippingMatrix;
        }
        val projectionMatrixBuffer = this.projectionMatrixBuffer;
        val modelviewMatrixBuffer = this.modelviewMatrixBuffer;
        projectionMatrixBuffer.clear();
        modelviewMatrixBuffer.clear();
        this.field_78564_h.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixBuffer);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewMatrixBuffer);
        projectionMatrixBuffer.flip()
                              .limit(16);
        modelviewMatrixBuffer.flip()
                             .limit(16);
        projectionMatrix.set(projectionMatrixBuffer);
        modelViewMatrix.set(modelviewMatrixBuffer);
        projectionMatrix.mul(modelViewMatrix, clippingMatrix);

        val clippingMatrixArr = this.clippingMatrix;
        clippingMatrix.get(clippingMatrixArr);

        val frustum = this.frustum;
        frustum[0][0] = clippingMatrixArr[3] - clippingMatrixArr[0];
        frustum[0][1] = clippingMatrixArr[7] - clippingMatrixArr[4];
        frustum[0][2] = clippingMatrixArr[11] - clippingMatrixArr[8];
        frustum[0][3] = clippingMatrixArr[15] - clippingMatrixArr[12];
        this.normalize(frustum, 0);
        frustum[1][0] = clippingMatrixArr[3] + clippingMatrixArr[0];
        frustum[1][1] = clippingMatrixArr[7] + clippingMatrixArr[4];
        frustum[1][2] = clippingMatrixArr[11] + clippingMatrixArr[8];
        frustum[1][3] = clippingMatrixArr[15] + clippingMatrixArr[12];
        this.normalize(frustum, 1);
        frustum[2][0] = clippingMatrixArr[3] + clippingMatrixArr[1];
        frustum[2][1] = clippingMatrixArr[7] + clippingMatrixArr[5];
        frustum[2][2] = clippingMatrixArr[11] + clippingMatrixArr[9];
        frustum[2][3] = clippingMatrixArr[15] + clippingMatrixArr[13];
        this.normalize(frustum, 2);
        frustum[3][0] = clippingMatrixArr[3] - clippingMatrixArr[1];
        frustum[3][1] = clippingMatrixArr[7] - clippingMatrixArr[5];
        frustum[3][2] = clippingMatrixArr[11] - clippingMatrixArr[9];
        frustum[3][3] = clippingMatrixArr[15] - clippingMatrixArr[13];
        this.normalize(frustum, 3);
        frustum[4][0] = clippingMatrixArr[3] - clippingMatrixArr[2];
        frustum[4][1] = clippingMatrixArr[7] - clippingMatrixArr[6];
        frustum[4][2] = clippingMatrixArr[11] - clippingMatrixArr[10];
        frustum[4][3] = clippingMatrixArr[15] - clippingMatrixArr[14];
        this.normalize(frustum, 4);
        frustum[5][0] = clippingMatrixArr[3] + clippingMatrixArr[2];
        frustum[5][1] = clippingMatrixArr[7] + clippingMatrixArr[6];
        frustum[5][2] = clippingMatrixArr[11] + clippingMatrixArr[10];
        frustum[5][3] = clippingMatrixArr[15] + clippingMatrixArr[14];
        this.normalize(frustum, 5);
    }
}
