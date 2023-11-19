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

package com.falsepattern.falsetweaks.modules.voxelizer;

import lombok.Builder;

public class Face implements Cloneable {
    public final Dir dir;
    public final int z;
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    public float u1;
    public float u2;
    public float v1;
    public float v2;
    public Face parent = null;
    public boolean used = false;

    @Builder
    public Face(Dir dir, int z, int minX, int maxX, int minY, int maxY, float u1, float u2, float v1, float v2) {
        this.dir = dir;
        this.z = z;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.u1 = u1;
        this.u2 = u2;
        this.v1 = v1;
        this.v2 = v2;
    }

    public static boolean tryMerge(Face a, Face b) {
        if (a == null || b == null) {
            return false;
        }
        while (a.parent != null) {
            a = a.parent;
        }
        while (b.parent != null) {
            b = b.parent;
        }
        return a.tryMerge(b);
    }

    public boolean tryMerge(Face other) {
        //Check presence
        if (other == null) {
            return false;
        }
        //Check alignment and layer
        if (other.dir != dir || other.z != z) {
            return false;
        }
        //Horizontal merging
        if (other.minY == minY && other.maxY == maxY) {
            //Merge if side by side
            //from the left
            if (other.maxX == minX - 1) {
                other.maxX = maxX;
                other.u2 = u2;
                parent = other;
                return true;
            }
            //from the right
            if (maxX == other.minX - 1) {
                maxX = other.maxX;
                u2 = other.u2;
                other.parent = this;
                return true;
            }
        }
        //Vertical merging
        if (other.minX == minX && other.maxX == maxX) {
            //Merge if side by side
            //from the top
            if (other.maxY == minY - 1) {
                other.maxY = maxY;
                other.v2 = v2;
                parent = other;
                return true;
            }
            //from the bottom
            if (maxY == other.minY - 1) {
                maxY = other.maxY;
                v2 = other.v2;
                other.parent = this;
                return true;
            }
        }
        //Not side by side
        return false;
    }

    @Override
    public Face clone() {
        try {
            return (Face) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
