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

import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;

import net.minecraft.client.util.QuadComparator;

public class SmartComparator extends QuadComparator {
    private final CenterComputer computer;
    protected final int vertexSize;
    public SmartComparator(int[] vertices, float playerX, float playerY, float playerZ, CenterComputer computer, boolean shaderMode) {
        super(vertices, playerX, playerY, playerZ);
        this.computer = computer;
        this.vertexSize = VertexInfo.recomputeVertexInfo(shaderMode ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1);
    }

    private int compare(int a, int b, float x, float y, float z, int[] vertexData, int vertexSize) {
        return Float.compare(computer.getCenter(x, y, z, vertexData, b, vertexSize),
                             computer.getCenter(x, y, z, vertexData, a, vertexSize));
    }

    @Override
    public int compare(Integer aObj, Integer bObj) {
        return compare(aObj, bObj, this.field_147630_a, this.field_147628_b, this.field_147629_c, this.field_147627_d, vertexSize);
    }

}
