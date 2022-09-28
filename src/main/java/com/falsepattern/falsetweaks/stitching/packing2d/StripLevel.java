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

package com.falsepattern.falsetweaks.stitching.packing2d;

import com.falsepattern.falsetweaks.stitching.Rect2D;
import lombok.Getter;

class StripLevel {
    private final int width;
    private final int top;
    private int availableWidth;
    @Getter
    private int tallest = -1;

    StripLevel(int width, int top) {
        this.width = width;
        this.availableWidth = width;
        this.top = top;
    }

    boolean checkFitRectangle(Rect2D r) {
        return (tallest < 0 || r.height <= tallest) && r.width <= availableWidth;
    }

    StripLevel fitRectangle(Rect2D r) {
        if (tallest >= 0 && r.height > tallest) {
            return null;
        }
        StripLevel newStrip = null;
        int leftOver = availableWidth - r.width;
        if (leftOver >= 0) {
            r.x = width - availableWidth;
            r.y = top;
            if (tallest == -1) {
                tallest = r.height;
            }
            if (r.height < tallest) {
                newStrip = new StripLevel(width, top + r.height);
                newStrip.availableWidth = availableWidth;
                newStrip.tallest = tallest - r.height;
                tallest = r.height;
            }
            availableWidth = leftOver;
        }
        return newStrip;
    }

    int availableWidth() {
        return availableWidth;
    }

    boolean canFit(Rect2D r) {
        return availableWidth - r.width >= 0 && (tallest < 0 || r.height <= tallest);
    }
}