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

package com.falsepattern.falsetweaks.modules.animfix.stitching.packing2d;

import com.falsepattern.falsetweaks.modules.animfix.stitching.Rect2D;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

class PackerFFDH<T extends Rect2D> extends Packer<T> {
    private final List<StripLevel> levels = new ArrayList<>(1);
    private int top = 0;

    public PackerFFDH(int stripWidth, List<T> rectangles) {
        super(stripWidth, rectangles);
    }

    @Override
    public List<T> pack() {
        sortByNonIncreasingHeight(rectangles);
        for (val r : rectangles) {
            var fitsOnALevel = false;
            for (int i = 0; i < levels.size(); i++) {
                val level = levels.get(i);
                fitsOnALevel = level.checkFitRectangle(r);
                if (!fitsOnALevel) {
                    continue;
                }
                val newStrip = level.fitRectangle(r);
                if (newStrip != null) {
                    levels.add(0, newStrip);
                }
                break;
            }
            if (fitsOnALevel) {
                continue;
            }
            val level = new StripLevel(stripWidth, top);
            level.fitRectangle(r);
            levels.add(level);
            top += r.height;
        }
        return rectangles;
    }
}
