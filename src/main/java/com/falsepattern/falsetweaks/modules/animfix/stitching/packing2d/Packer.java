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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Packer<T extends Rect2D> {
    final int stripWidth;
    final List<T> rectangles;

    Packer(int stripWidth, List<T> rectangles) {
        this.stripWidth = stripWidth;
        this.rectangles = rectangles;
    }

    public static <U extends Rect2D> List<U> pack(List<U> rectangles, Algorithm algorithm, int stripWidth) {
        Packer<U> packer;
        switch (algorithm) {
            case FIRST_FIT_DECREASING_HEIGHT:
                packer = new PackerFFDH<>(stripWidth, rectangles);
                return packer.pack();
            case BEST_FIT_DECREASING_HEIGHT:
                packer = new PackerBFDH<>(stripWidth, rectangles);
                break;
            default:
                return new ArrayList<>();
        }
        return packer.pack();
    }

    public abstract List<T> pack();

    void sortByNonIncreasingHeight(List<T> rectangles) {
        rectangles.sort(Comparator.<T>comparingInt((rect) -> rect.height)
                                  .reversed());
    }
}
