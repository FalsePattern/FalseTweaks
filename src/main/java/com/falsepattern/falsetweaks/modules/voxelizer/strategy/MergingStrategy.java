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

package com.falsepattern.falsetweaks.modules.voxelizer.strategy;

import com.falsepattern.falsetweaks.modules.voxelizer.Face;

public interface MergingStrategy {
    static Face[][] clone(Face[][] faces) {
        Face[][] result = new Face[faces.length][];
        for (int y = 0; y < faces.length; y++) {
            Face[] row = faces[y];
            Face[] resultRow = result[y] = new Face[row.length];
            for (int x = 0; x < row.length; x++) {
                Face f = row[x];
                if (f == null) {
                    continue;
                }
                resultRow[x] = f.clone();
            }
        }
        return result;
    }

    static void emplace(Face[][] result, Face[][] source) {
        if (result == source) {
            return;
        }
        System.arraycopy(source, 0, result, 0, source.length);
    }

    static int countFaces(Face[][] faces) {
        int n = 0;
        for (Face[] row : faces) {
            for (Face f : row) {
                if (f != null && f.parent == null) {
                    n++;
                }
            }
        }
        return n;
    }

    void merge(Face[][] faces);

    default void mergeSide(Face[] faces) {
        for (int i = 0; i < faces.length - 1; i++) {
            Face.tryMerge(faces[i], faces[i + 1]);
        }
    }
}
