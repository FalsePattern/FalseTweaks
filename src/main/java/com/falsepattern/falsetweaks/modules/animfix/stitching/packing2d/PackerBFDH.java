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

package com.falsepattern.falsetweaks.modules.animfix.stitching.packing2d;

import com.falsepattern.falsetweaks.modules.animfix.stitching.Rect2D;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

public class PackerBFDH<T extends Rect2D> extends Packer<T> {
    private final List<StripLevel> levels;

    public PackerBFDH(int stripWidth, List<T> rectangles) {
        super(stripWidth, rectangles);
        levels = new ArrayList<>();
    }

    @Override
    public List<T> pack() {
        var top = 0;
        sortByNonIncreasingHeight(rectangles);
        for (val r : rectangles) {
            StripLevel levelWithSmallestResidual = null;
            for (val level : levels) {
                if (!level.canFit(r)) {
                    continue;
                }
                if (levelWithSmallestResidual != null &&
                    levelWithSmallestResidual.availableWidth() > level.availableWidth()) {
                    levelWithSmallestResidual = level;
                } else if (levelWithSmallestResidual == null) {
                    levelWithSmallestResidual = level;
                }
            }
            if (levelWithSmallestResidual == null) {
                val level = new StripLevel(stripWidth, top);
                level.fitRectangle(r);
                levels.add(level);
                top += r.height;
            } else {
                val newLevel = levelWithSmallestResidual.fitRectangle(r);
                if (newLevel != null) {
                    levels.add(levels.indexOf(levelWithSmallestResidual) + 1, newLevel);
                }
            }

        }
        return rectangles;
    }
}
