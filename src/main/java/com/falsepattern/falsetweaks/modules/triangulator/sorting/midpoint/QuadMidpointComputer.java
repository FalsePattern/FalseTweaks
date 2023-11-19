/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.triangulator.sorting.midpoint;

import com.falsepattern.falsetweaks.modules.triangulator.sorting.SharedMath;
import lombok.val;
import org.joml.Vector3f;

public class QuadMidpointComputer implements MidpointComputer {
    public static final QuadMidpointComputer INSTANCE = new QuadMidpointComputer();

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
        val dx = Float.intBitsToFloat(vertexData[i + vertexSize * 3]);
        val dy = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 1]);
        val dz = Float.intBitsToFloat(vertexData[i + vertexSize * 3 + 2]);

        val minX = Math.min(Math.min(ax, bx), Math.min(cx, dx));
        val minY = Math.min(Math.min(ay, by), Math.min(cy, dy));
        val minZ = Math.min(Math.min(az, bz), Math.min(cz, dz));

        val maxX = Math.max(Math.max(ax, bx), Math.max(cx, dx));
        val maxY = Math.max(Math.max(ay, by), Math.max(cy, dy));
        val maxZ = Math.max(Math.max(az, bz), Math.max(cz, dz));

        SharedMath.getMidpoint(minX, minY, minZ, maxX, maxY, maxZ, output);
    }
}
