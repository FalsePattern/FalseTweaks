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

import lombok.val;
import org.joml.Vector3f;

public class QuadAreaComputer implements NormalAreaComputer {
    public static final QuadAreaComputer INSTANCE = new QuadAreaComputer();
    private final Vector3f buf = new Vector3f();
    @Override
    public float getArea(int[] vertexData, int i, int vertexSize) {
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
        float tri1Area = buf.set(bx - ax, by - ay, bz - az).cross(cx - ax, cy - ay, cz - az).lengthSquared() / 4;

        //1 -- a, 2 -- c, 3 -- d
        float tri2Area = buf.set(cx - ax, cy - ay, cz - az).cross(dx - ax, dy - ay, dz - az).lengthSquared() / 4;
        return tri1Area + tri2Area;
    }

    @Override
    public void getNormal(int[] vertexData, int i, int vertexSize, Vector3f output) {
        val ax = Float.intBitsToFloat(vertexData[i]);
        val ay = Float.intBitsToFloat(vertexData[i + 1]);
        val az = Float.intBitsToFloat(vertexData[i + 2]);
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]);
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]);
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]);
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]);
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]);
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]);
        output.set(bx - ax, by - ay, bz - az).cross(cx - ax, cy - ay, cz - az);
    }
}
