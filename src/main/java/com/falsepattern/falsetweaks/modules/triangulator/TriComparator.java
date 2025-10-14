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

package com.falsepattern.falsetweaks.modules.triangulator;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.modules.vertexapi.VertexInfo;
import lombok.val;

import net.minecraft.client.util.QuadComparator;

public class TriComparator extends QuadComparator {
    public TriComparator(int[] vertexData, float x, float y, float z) {
        super(vertexData, x, y, z);
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        val vertexData = this.field_147627_d;
        val stride = VertexInfo.getVertexInfo(Compat.shaderType(), 1);
        val stride2 = stride * 2;
        val refX = field_147630_a;
        val refY = field_147628_b;
        val refZ = field_147629_c;
        val aSQ = ft$getDistSQ(vertexData, o1, stride, stride2, refX, refY, refZ);
        val bSQ = ft$getDistSQ(vertexData, o2, stride, stride2, refX, refY, refZ);
        return Float.compare(bSQ, aSQ);
    }
    private static float ft$getDistSQ(int[] vertexData, int primitive, int stride, int stride2, float refX, float refY, float refZ) {
        val x1 = Float.intBitsToFloat(vertexData[primitive]) - refX;
        val y1 = Float.intBitsToFloat(vertexData[primitive + 1]) - refY;
        val z1 = Float.intBitsToFloat(vertexData[primitive + 2]) - refZ;
        val x2 = Float.intBitsToFloat(vertexData[primitive + stride]) - refX;
        val y2 = Float.intBitsToFloat(vertexData[primitive + stride + 1]) - refY;
        val z2 = Float.intBitsToFloat(vertexData[primitive + stride + 2]) - refZ;
        val x3 = Float.intBitsToFloat(vertexData[primitive + stride2]) - refX;
        val y3 = Float.intBitsToFloat(vertexData[primitive + stride2 + 1]) - refY;
        val z3 = Float.intBitsToFloat(vertexData[primitive + stride2 + 2]) - refZ;

        val x = (x1 + x2 + x3) * 0.25F;
        val y = (y1 + y2 + y3) * 0.25F;
        val z = (z1 + z2 + z3) * 0.25F;
        return x * x + y * y + z * z;
    }
}
