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

package com.falsepattern.falsetweaks.voxelizer;

public enum VoxelType {
    Solid, SemiSolid, Transparent;

    public int toNumber() {
        switch (this) {
            case Transparent: return 0;
            case SemiSolid: return 1;
            case Solid: return 2;
            default: throw new IllegalStateException();
        }
    }

    public static VoxelType fromNumber(int number) {
        switch (number) {
            case 0: return Transparent;
            case 1: return SemiSolid;
            case 2: return Solid;
            default: throw new IllegalArgumentException(Integer.toString(number));
        }
    }

    public static VoxelType fromAlpha(int alpha) {
        switch (alpha) {
            case 0: return Transparent;
            case 255: return Solid;
            default: return SemiSolid;
        }
    }
}
