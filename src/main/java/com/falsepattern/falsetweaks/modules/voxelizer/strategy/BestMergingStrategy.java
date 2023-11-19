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

package com.falsepattern.falsetweaks.modules.voxelizer.strategy;

import com.falsepattern.falsetweaks.modules.voxelizer.Face;
import lombok.val;

import java.util.Arrays;
import java.util.List;

public class BestMergingStrategy implements MergingStrategy {
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
        MergingStrategy.emplace(faces, sources[bestFaceCountIndex]);
    }
}
