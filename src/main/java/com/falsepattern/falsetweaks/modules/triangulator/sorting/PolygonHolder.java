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

package com.falsepattern.falsetweaks.modules.triangulator.sorting;

import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.NormalAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.QuadAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.TriangleAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.midpoint.MidpointComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.midpoint.QuadMidpointComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.midpoint.TriangleMidpointComputer;
import lombok.Getter;
import org.joml.Vector3f;

public class PolygonHolder {
    public final int vertexStride;
    private final int polygonSize;
    private final int vertexSize;
    private final MidpointComputer midpointComputer;
    private final NormalAreaComputer areaComputer;
    @Getter
    private int[] vertexData;
    @Getter
    private int polygonCount = 0;

    public PolygonHolder(boolean triangleMode, boolean shaderMode) {
        this.polygonSize = triangleMode ? 3 : 4;
        vertexSize = VertexInfo.recomputeVertexInfo(shaderMode ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1);
        midpointComputer = triangleMode ? TriangleMidpointComputer.INSTANCE : QuadMidpointComputer.INSTANCE;
        areaComputer = triangleMode ? TriangleAreaComputer.INSTANCE : QuadAreaComputer.INSTANCE;
        vertexStride = polygonSize * vertexSize;
    }

    public void setVertexData(int[] vertexData) {
        this.vertexData = vertexData;
        polygonCount = (vertexData.length / vertexSize) / polygonSize;
    }

    public void clearVertexData() {
        vertexData = null;
    }

    public void midpoint(int polygon, Vector3f output) {
        midpointComputer.getMidpoint(vertexData, polygon * vertexStride, vertexSize, output);
    }

    public float area(int polygon, Vector3f scratchBuffer) {
        return areaComputer.getArea(vertexData, polygon * vertexStride, vertexSize, scratchBuffer);
    }

    public void normal(int polygon, Vector3f output) {
        areaComputer.getNormal(vertexData, polygon * vertexStride, vertexSize, output);
    }
}
