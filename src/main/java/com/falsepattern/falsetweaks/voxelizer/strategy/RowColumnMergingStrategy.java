/*
 * Triangulator
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

package com.falsepattern.falsetweaks.voxelizer.strategy;

import com.falsepattern.falsetweaks.voxelizer.Face;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RowColumnMergingStrategy implements MergingStrategy {
    public static final RowColumnMergingStrategy NoFlip = new RowColumnMergingStrategy(false);
    public static final RowColumnMergingStrategy YesFlip = new RowColumnMergingStrategy(true);

    public static List<RowColumnMergingStrategy> all() {
        return Arrays.asList(NoFlip, YesFlip);
    }

    private final boolean flipIteration;

    @Override
    public void merge(Face[][] faces) {
        if (flipIteration) {
            vertical(faces);
            horizontal(faces);
        } else {
            horizontal(faces);
            vertical(faces);
        }
    }

    private void horizontal(Face[][] faces) {
        for (Face[] row : faces) {
            mergeSide(row);
        }
    }

    private void vertical(Face[][] faces) {
        for (int y = 0; y < faces.length - 1; y++) {
            Face[] rowA = faces[y];
            Face[] rowB = faces[y + 1];
            for (int x = 0; x < rowA.length; x++) {
                Face.tryMerge(rowA[x], rowB[x]);
            }
        }
    }
}
