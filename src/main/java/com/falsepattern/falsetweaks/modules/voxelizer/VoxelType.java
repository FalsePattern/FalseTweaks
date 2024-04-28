/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

public enum VoxelType {
    Solid,
    SemiSolid,
    Transparent;

    public static VoxelType[] renderable() {
        return new VoxelType[]{Solid, SemiSolid};
    }

    public static VoxelType fromNumber(int number) {
        switch (number) {
            case 0:
                return Transparent;
            case 1:
                return SemiSolid;
            case 2:
                return Solid;
            default:
                throw new IllegalArgumentException(Integer.toString(number));
        }
    }

    public static VoxelType fromAlpha(int alpha) {
        switch (alpha) {
            case 0:
                return Transparent;
            case 255:
                return Solid;
            default:
                return SemiSolid;
        }
    }

    public int toNumber() {
        switch (this) {
            case Transparent:
                return 0;
            case SemiSolid:
                return 1;
            case Solid:
                return 2;
            default:
                throw new IllegalStateException();
        }
    }
}
