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

package com.falsepattern.falsetweaks.modules.voxelizer;

public final class Voxel {
    /*
    0000 0000
    7654 3210

    UDLR IOTT
     */
    public static final int OFFSET_TYPE = 0;
    public static final int OFFSET_OUT = 2;
    public static final int OFFSET_IN = 3;
    public static final int OFFSET_RIGHT = 4;
    public static final int OFFSET_LEFT = 5;
    public static final int OFFSET_DOWN = 6;
    public static final int OFFSET_UP = 7;
    public static final byte BITMASK_TYPE = 0x3;

    private Voxel() {
        throw new UnsupportedOperationException();
    }

    public static VoxelType getType(byte voxel) {
        return VoxelType.fromNumber((voxel & BITMASK_TYPE) >>> OFFSET_TYPE);
    }

    public static byte setType(byte voxel, VoxelType type) {
        return (byte) ((voxel & ~BITMASK_TYPE) | ((type.toNumber() << OFFSET_TYPE) & BITMASK_TYPE));
    }

    public static boolean getFace(byte voxel, Dir dir) {
        return ((voxel >>> dir.bit) & 1) != 0;
    }

    public static byte setFace(byte voxel, Dir dir, boolean state) {
        return (byte) (state ? voxel | (1 << dir.bit) : voxel & ~(1 << dir.bit));
    }
}
