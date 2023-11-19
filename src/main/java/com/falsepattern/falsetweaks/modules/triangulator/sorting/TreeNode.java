/*
 * This file is part of FalseTweaks.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.Data;
import org.joml.Vector3f;

@Data
public class TreeNode {
    public Vector3f normal;
    public Vector3f origin;
    public TIntList triangleRefs;
    public int frontRef;
    public int backRef;

    public TreeNode(Vector3f normal, Vector3f origin, TIntList triangleRefs, int frontRef, int backRef) {
        this.normal = new Vector3f(normal);
        this.origin = new Vector3f(origin);
        this.triangleRefs = new TIntArrayList(triangleRefs);
        this.frontRef = frontRef;
        this.backRef = backRef;
    }
}
