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

package com.falsepattern.falsetweaks.modules.triangulator.sorting.area;

import com.falsepattern.falsetweaks.modules.triangulator.sorting.SharedMath;
import lombok.val;
import org.joml.Vector3f;

public class TriangleAreaComputer implements NormalAreaComputer {
    public static final TriangleAreaComputer INSTANCE = new TriangleAreaComputer();
    private final Vector3f buf = new Vector3f();

    @Override
    public float getArea(int[] vertexData, int i, int vertexSize) {
        getNormalUnscaled(vertexData, i, vertexSize, buf);
        return SharedMath.unscaledNormalToArea(buf);
    }

    @Override
    public void getNormal(int[] vertexData, int i, int vertexSize, Vector3f output) {
        getNormalUnscaled(vertexData, i, vertexSize, output);
        output.normalize();
    }

    private void getNormalUnscaled(int[] vertexData, int i, int vertexSize, Vector3f output) {
        val ax = Float.intBitsToFloat(vertexData[i]);
        val ay = Float.intBitsToFloat(vertexData[i + 1]);
        val az = Float.intBitsToFloat(vertexData[i + 2]);
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]);
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]);
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]);
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]);
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]);
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]);
        SharedMath.getTriangleNormalUnscaled(ax, ay, az, bx, by, bz, cx, cy, cz, output);
    }

}
