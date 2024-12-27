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

package com.falsepattern.falsetweaks.modules.occlusion.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.renderer.WorldRenderer;

public class ShadowPassOcclusionHelper {
    private static float minCamX, minCamY, minCamZ;
    private static float maxCamX, maxCamY;

    private static float minX, minY, minZ;
    private static float maxX, maxY, maxZ;

    private static Vector3f scratch = new Vector3f();
    public static Matrix4f shadowModelViewMatrix = new Matrix4f();
    public static void begin() {
        minCamX = Float.POSITIVE_INFINITY;
        minCamY = Float.POSITIVE_INFINITY;
        minCamZ = Float.POSITIVE_INFINITY;
        maxCamX = Float.NEGATIVE_INFINITY;
        maxCamY = Float.NEGATIVE_INFINITY;
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        minZ = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        maxZ = Float.NEGATIVE_INFINITY;
    }

    public static void addShadowReceiver(WorldRenderer wr) {
        minX = Math.min(minX, wr.posX);
        minY = Math.min(minY, wr.posY);
        minZ = Math.min(minZ, wr.posZ);
        maxX = Math.max(maxX, wr.posX + 16);
        maxY = Math.max(maxY, wr.posY + 16);
        maxZ = Math.max(maxZ, wr.posZ + 16);
    }

    public static void end() {
        for (int i = 0; i < 8; i++) {
            float x = (i & 1) == 0 ? minX : maxX;
            float y = (i & 2) == 0 ? minY : maxY;
            float z = (i & 4) == 0 ? minZ : maxZ;
            shadowModelViewMatrix.transformPosition(x, y, z, scratch);
            x = scratch.x;
            y = scratch.y;
            z = scratch.z;
            minCamX = Math.min(minCamX, x);
            minCamY = Math.min(minCamY, y);
            minCamZ = Math.min(minCamZ, z);
            maxCamX = Math.max(maxCamX, x);
            maxCamY = Math.max(maxCamY, y);
        }
        if (Float.isNaN(minCamX))
            minCamX = Float.NEGATIVE_INFINITY;
        if (Float.isNaN(minCamY))
            minCamY = Float.NEGATIVE_INFINITY;
        if (Float.isNaN(minCamZ))
            minCamZ = Float.NEGATIVE_INFINITY;
        if (Float.isNaN(maxCamX))
            maxCamX = Float.POSITIVE_INFINITY;
        if (Float.isNaN(maxCamY))
            maxCamY = Float.POSITIVE_INFINITY;
    }

    public static boolean isShadowVisible(WorldRenderer wr) {
        float posX = wr.posX;
        float posY = wr.posY;
        float posZ = wr.posZ;

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            float x = posX + ((i & 1) == 0 ? 0 : 16);
            float y = posY + ((i & 2) == 0 ? 0 : 16);
            float z = posZ + ((i & 4) == 0 ? 0 : 16);
            shadowModelViewMatrix.transformPosition(x, y, z, scratch);
            x = scratch.x;
            y = scratch.y;
            z = scratch.z;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        return maxX > minCamX && minX < maxCamX && maxY > minCamY && minY < maxCamY && maxZ > minCamZ;
    }
}
