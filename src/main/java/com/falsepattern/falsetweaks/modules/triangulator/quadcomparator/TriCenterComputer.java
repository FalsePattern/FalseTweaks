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

package com.falsepattern.falsetweaks.modules.triangulator.quadcomparator;

import lombok.val;

public class TriCenterComputer implements CenterComputer {
    public static final TriCenterComputer INSTANCE = new TriCenterComputer();
    @Override
    public float getCenter(float x, float y, float z, int[] vertexData, int i, int vertexSize) {
        val ax = Float.intBitsToFloat(vertexData[i]) - x;
        val ay = Float.intBitsToFloat(vertexData[i + 1]) - y;
        val az = Float.intBitsToFloat(vertexData[i + 2]) - z;
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]) - x;
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]) - y;
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]) - z;
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]) - x;
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]) - y;
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]) - z;

        val xAvg = (ax + bx + cx) / 3f;
        val yAvg = (ay + by + cy) / 3f;
        val zAvg = (az + bz + cz) / 3f;

        return xAvg * xAvg + yAvg * yAvg + zAvg * zAvg;
    }
}
