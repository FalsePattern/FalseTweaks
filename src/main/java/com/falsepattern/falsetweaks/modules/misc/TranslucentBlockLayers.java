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

package com.falsepattern.falsetweaks.modules.misc;

import com.falsepattern.falsetweaks.Compat;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import shadersmod.client.SMath;
import shadersmod.client.Shaders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;

import static com.falsepattern.falsetweaks.config.TranslucentBlockLayersConfig.TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON;

public class TranslucentBlockLayers {
    private final FloatBuffer matrixBufferIn = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer matrixBufferOut = BufferUtils.createFloatBuffer(16);
    private final Matrix4f matrixA = new Matrix4f();
    private final Matrix4f matrixB = new Matrix4f();
    public void offsetProjection() {
        val bufIn = matrixBufferIn;
        val bufOut = matrixBufferOut;
        val lastMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        bufIn.position(0);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, bufIn);
        transform();
        GL11.glPushMatrix();
        bufOut.position(0);
        GL11.glLoadMatrix(bufOut);
        GL11.glMatrixMode(lastMatrixMode);
        if (Compat.isShaders()) {
            ShadersCompat.fetchFromShaders(bufIn);
            transform();
            ShadersCompat.setToShaders(bufOut);
        }
    }

    private void transform() {
        val matA = matrixA;
        val matB = matrixB;
        matB.set(0, matrixBufferIn);
        float epsilon = (float) -TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON;
        matA.scaling(1F, 1F, 1F / (1F + epsilon));
        matA.translate(0F, 0F, epsilon);
        matA.mul(matB);
        matA.get(0, matrixBufferOut);
    }

    public void resetProjection() {
        val lastMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(lastMatrixMode);
        if (Compat.isShaders()) {
            ShadersCompat.setToShaders(matrixBufferIn);
        }
    }

    private static class ShadersCompat {
        private static final FloatBuffer projection;
        private static final FloatBuffer projectionInverse;
        private static final float[] faProjection;
        private static final float[] faProjectionInverse;
        private static final Method invertMat4FBFA;
        static {
            try {
                val fProjection = Shaders.class.getDeclaredField("projection");
                val fProjectionInverse = Shaders.class.getDeclaredField("projectionInverse");
                val ffaProjection = Shaders.class.getDeclaredField("faProjection");
                val ffaProjectionInverse = Shaders.class.getDeclaredField("faProjectionInverse");
                invertMat4FBFA = SMath.class.getDeclaredMethod("invertMat4FBFA", FloatBuffer.class, FloatBuffer.class, float[].class, float[].class);
                invertMat4FBFA.setAccessible(true);
                fProjection.setAccessible(true);
                fProjectionInverse.setAccessible(true);
                ffaProjection.setAccessible(true);
                ffaProjectionInverse.setAccessible(true);
                projection = (FloatBuffer) fProjection.get(null);
                projectionInverse = (FloatBuffer) fProjectionInverse.get(null);
                faProjection = (float[]) ffaProjection.get(null);
                faProjectionInverse = (float[]) ffaProjectionInverse.get(null);
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        private static void fetchFromShaders(FloatBuffer buf) {
            buf.position(0);
            projection.position(0);
            buf.put(projection);
            buf.position(0);
            projection.position(0);
        }

        private static void setToShaders(FloatBuffer buf) {
            buf.position(0);
            projection.position(0);
            projection.put(buf);
            buf.position(0);
            projection.position(0);
            try {
                invertMat4FBFA.invoke(null, (FloatBuffer)projectionInverse.position(0), (FloatBuffer)projection.position(0), faProjectionInverse, faProjection);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            Shaders.setProgramUniformMatrix4ARB("gbufferProjection", false, projection);
            Shaders.setProgramUniformMatrix4ARB("gbufferProjectionInverse", false, projectionInverse);
        }
    }
}
