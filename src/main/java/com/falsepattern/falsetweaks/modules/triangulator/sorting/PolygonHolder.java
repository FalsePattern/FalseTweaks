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

package com.falsepattern.falsetweaks.modules.triangulator.sorting;

import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.NormalAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.QuadAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.area.TriangleAreaComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.centroid.CentroidComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.centroid.QuadCentroidComputer;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.centroid.TriangleCentroidComputer;
import lombok.Getter;
import org.joml.Vector3f;

public class PolygonHolder {
    @Getter
    private int[] vertexData;
    @Getter
    private int polygonCount = 0;

    private final int polygonSize;
    private final int vertexSize;
    public final int vertexStride;
    private final CentroidComputer centroidComputer;
    private final NormalAreaComputer areaComputer;

    public PolygonHolder(boolean triangleMode, boolean shaderMode) {
        this.polygonSize = triangleMode ? 3 : 4;
        vertexSize = VertexInfo.recomputeVertexInfo(shaderMode ? VertexInfo.OPTIFINE_SIZE : VertexInfo.VANILLA_SIZE, 1);
        centroidComputer = triangleMode ? TriangleCentroidComputer.INSTANCE : QuadCentroidComputer.INSTANCE;
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

    public void centroid(int polygon, Vector3f output) {
        centroidComputer.getCentroid(vertexData, polygon * vertexStride, vertexSize, output);
    }

    public float area(int polygon) {
        return areaComputer.getArea(vertexData, polygon * vertexStride, vertexSize);
    }

    public void normal(int polygon, Vector3f output) {
        areaComputer.getNormal(vertexData, polygon * vertexStride, vertexSize, output);
    }
}
