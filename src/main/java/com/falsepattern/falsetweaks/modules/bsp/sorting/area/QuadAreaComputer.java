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

package com.falsepattern.falsetweaks.modules.bsp.sorting.area;

import com.falsepattern.falsetweaks.modules.bsp.sorting.SharedMath;
import lombok.val;
import lombok.var;
import org.joml.Vector3f;

public class QuadAreaComputer implements NormalAreaComputer {
    public static final QuadAreaComputer INSTANCE = new QuadAreaComputer();

    @Override
    public float getArea(int[] vertexData, int i, int vertexSize, Vector3f buf) {
        val ax = Float.intBitsToFloat(vertexData[i]);
        val ay = Float.intBitsToFloat(vertexData[i + 1]);
        val az = Float.intBitsToFloat(vertexData[i + 2]);
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]);
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]);
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]);
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]);
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]);
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]);
        val dx = Float.intBitsToFloat(vertexData[i + vertexSize * 3]);
        val dy = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 1]);
        val dz = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 2]);

        //1 -- a, 2 -- b, 3 -- c
        float tri1Area = SharedMath.getTriArea(ax, ay, az, bx, by, bz, cx, cy, cz, buf);

        //1 -- a, 2 -- c, 3 -- d
        float tri2Area = SharedMath.getTriArea(ax, ay, az, cx, cy, cz, dx, dy, dz, buf);
        return tri1Area + tri2Area;
    }

    @Override
    public void getNormal(int[] vertexData, int i, int vertexSize, Vector3f output) {
        var ax = Float.intBitsToFloat(vertexData[i]);
        var ay = Float.intBitsToFloat(vertexData[i + 1]);
        var az = Float.intBitsToFloat(vertexData[i + 2]);
        var bx = Float.intBitsToFloat(vertexData[i + vertexSize]);
        var by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]);
        var bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]);
        var cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]);
        var cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]);
        var cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]);

        if ((ax == bx && ay == by && az == bz) || (bx == cx && by == cy && bz == cz)) {
            bx = cx;
            by = cy;
            bz = cz;

            cx = Float.intBitsToFloat(vertexData[i + vertexSize * 3]);
            cy = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 1]);
            cz = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 2]);
        } else if (ax == cx && ay == cy && az == cz) {
            cx = Float.intBitsToFloat(vertexData[i + vertexSize * 3]);
            cy = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 1]);
            cz = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 2]);
        }

        SharedMath.getTriangleNormalUnscaled(ax, ay, az, bx, by, bz, cx, cy, cz, output);
        output.normalize();
    }
}
