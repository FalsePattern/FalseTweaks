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

package com.falsepattern.falsetweaks.modules.triangulator.sorting;

import org.joml.Vector3f;

public class SharedMath {
    public static float getTriArea(float ax, float ay, float az, float cx, float cy, float cz, float dx, float dy, float dz, Vector3f buf) {
        float triArea;
        if ((ax == cx && ay == cy && az == cz)
            || (cx == dx && cy == dy && cz == dz)
            || (ax == dx && ay == dy && az == dz)) {
            triArea = 0;
        } else {
            getTriangleNormalUnscaled(ax, ay, az, cx, cy, cz, dx, dy, dz, buf);
            triArea = unscaledNormalToArea(buf);
        }
        return triArea;
    }

    public static float unscaledNormalToArea(Vector3f normal) {
        return normal.lengthSquared() / 4;
    }

    public static void getMidpoint(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Vector3f output) {
        output.set((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
    }

    public static void getTriangleNormalUnscaled(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, Vector3f output) {
        output.set(bx - ax, by - ay, bz - az).cross(cx - ax, cy - ay, cz - az);
    }
}
