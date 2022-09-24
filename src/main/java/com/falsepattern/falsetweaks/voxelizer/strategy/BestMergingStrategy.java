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
import lombok.val;

import java.util.Arrays;
import java.util.List;

public class BestMergingStrategy implements MergingStrategy{
    private final MergingStrategy[] strategies;
    public BestMergingStrategy(MergingStrategy... strategies) {
        this.strategies = Arrays.copyOf(strategies, strategies.length);
    }

    public BestMergingStrategy(List<? extends MergingStrategy> strategies) {
        this(strategies.toArray(new MergingStrategy[0]));
    }

    @Override
    public void merge(Face[][] faces) {
        val sources = new Face[strategies.length][][];
        int bestFaceCount = 0;
        int bestFaceCountIndex = -1;
        sources[0] = faces;
        for (int i = 1; i < strategies.length; i++) {
            sources[i] = MergingStrategy.clone(faces);
        }
        for (int i = 0; i < strategies.length; i++) {
            strategies[i].merge(sources[i]);
            int faceCount = MergingStrategy.countFaces(sources[i]);
            if (faceCount < bestFaceCount || bestFaceCountIndex == -1) {
                bestFaceCount = faceCount;
                bestFaceCountIndex = i;
            }
        }
//        System.out.println(Arrays.toString(faceCounts));
//        System.out.println(bestFaceCount);
//        System.out.println("Reduction: " + (float)bestFaceCount / worstFaceCount);
        MergingStrategy.emplace(faces, sources[bestFaceCountIndex]);
    }
}
