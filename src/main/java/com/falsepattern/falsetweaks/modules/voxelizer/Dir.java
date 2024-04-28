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

import org.joml.Vector3i;
import org.joml.Vector3ic;

public enum Dir {
    Up(0, -1, 0, Voxel.OFFSET_UP),
    Down(0, 1, 0, Voxel.OFFSET_DOWN),
    Left(-1, 0, 0, Voxel.OFFSET_LEFT),
    Right(1, 0, 0, Voxel.OFFSET_RIGHT),
    Back(0, 0, -1, Voxel.OFFSET_IN),
    Front(0, 0, 1, Voxel.OFFSET_OUT);

    public final Vector3ic dir;
    public final int bit;

    Dir(int x, int y, int z, int bit) {
        dir = new Vector3i(x, y, z);
        this.bit = bit;
    }

    public Dir opposite() {
        switch (this) {
            case Right:
                return Left;
            case Up:
                return Down;
            case Front:
                return Back;
            case Left:
                return Right;
            case Down:
                return Up;
            case Back:
                return Front;
            default:
                throw new IllegalStateException();
        }
    }
}
