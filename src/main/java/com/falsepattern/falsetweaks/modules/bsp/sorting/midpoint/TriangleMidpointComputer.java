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

package com.falsepattern.falsetweaks.modules.bsp.sorting.midpoint;

import com.falsepattern.falsetweaks.modules.bsp.sorting.SharedMath;
import lombok.val;
import org.joml.Vector3f;

public class TriangleMidpointComputer implements MidpointComputer {
    public static final TriangleMidpointComputer INSTANCE = new TriangleMidpointComputer();

    @Override
    public void getMidpoint(int[] vertexData, int i, int vertexSize, Vector3f output) {
        val ax = Float.intBitsToFloat(vertexData[i]);
        val ay = Float.intBitsToFloat(vertexData[i + 1]);
        val az = Float.intBitsToFloat(vertexData[i + 2]);
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]);
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]);
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]);
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]);
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]);
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]);

        val xMin = Math.min(Math.min(ax, bx), cx);
        val yMin = Math.min(Math.min(ay, by), cy);
        val zMin = Math.min(Math.min(az, bz), cz);

        val xMax = Math.max(Math.max(ax, bx), cx);
        val yMax = Math.max(Math.max(ay, by), cy);
        val zMax = Math.max(Math.max(az, bz), cz);

        SharedMath.getMidpoint(xMin, yMin, zMin, xMax, yMax, zMax, output);
    }
}
