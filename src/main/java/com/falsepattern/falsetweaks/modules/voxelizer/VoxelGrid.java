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

package com.falsepattern.falsetweaks.modules.voxelizer;

public class VoxelGrid {
    public final int xSize;
    public final int ySize;
    public final int zSize;

    private final byte[] voxels;

    public VoxelGrid(int x, int y, int z) {
        xSize = x;
        ySize = y;
        zSize = z;
        voxels = new byte[xSize * ySize * zSize];
    }


    public int toIndex(int x, int y, int z) {
        if (x < 0 || x >= xSize) {
            return -1;
        }
        if (y < 0 || y >= ySize) {
            return -1;
        }
        if (z < 0 || z >= zSize) {
            return -1;
        }
        return z * ySize * xSize + y * xSize + x;
    }

    public VoxelType getType(int x, int y, int z) {
        return Voxel.getType(voxels[toIndex(x, y, z)]);
    }

    public void setType(int x, int y, int z, VoxelType type) {
        voxels[toIndex(x, y, z)] = Voxel.setType(voxels[toIndex(x, y, z)], type);
    }

    public boolean getFace(int x, int y, int z, Dir dir) {
        return Voxel.getFace(voxels[toIndex(x, y, z)], dir);
    }

    public void setFace(int x, int y, int z, Dir dir, boolean state) {
        voxels[toIndex(x, y, z)] = Voxel.setFace(voxels[toIndex(x, y, z)], dir, state);
    }

    public void exchangeFaces(int aIndex, int bIndex, Dir direction) {
        aIndex = aIndex >= voxels.length ? -1 : aIndex;
        bIndex = bIndex >= voxels.length ? -1 : bIndex;
        byte a = aIndex >= 0 ? voxels[aIndex] : 0;
        byte b = bIndex >= 0 ? voxels[bIndex] : 0;
        VoxelType aVoxelType = Voxel.getType(a);
        VoxelType bVoxelType = Voxel.getType(b);
        if (aIndex >= 0 && aVoxelType != VoxelType.Transparent && (aVoxelType != bVoxelType)) {
            voxels[aIndex] = Voxel.setFace(a, direction, true);
        }
        if (bIndex >= 0 && bVoxelType != VoxelType.Transparent && (aVoxelType != bVoxelType)) {
            voxels[bIndex] = Voxel.setFace(b, direction.opposite(), true);
        }
    }
}
