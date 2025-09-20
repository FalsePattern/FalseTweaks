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

package com.falsepattern.falsetweaks.api.triangulator;

import com.falsepattern.falsetweaks.modules.triangulator.VertexInfo;
import com.falsepattern.lib.StableAPI;

/**
 * Useful for adding "more data" to tessellator vertices.
 */
@StableAPI(since = "2.4.0")
public class VertexAPI {
    /**
     * Allocates extra usable space in vertex data.
     * Only usable during preInit, and only if the Triangulator module is enabled. Otherwise, it's a hard crash.
     *
     * @param count         The amount of integers to allocate (1 count = 4 bytes).
     * @param indices       The indices of the allocated vertex slots. Size must be equal to count.
     * @param shaderIndices The indices of the allocated vertex slots when shaders are active. Size must be equal to count.
     */
    @StableAPI.Expose
    public static void allocateExtraVertexSlots(int count, int[] indices, int[] shaderIndices) {
        VertexInfo.allocateExtraVertexSlots(count, indices, shaderIndices);
    }

    /**
     * Used for correcting any custom tessellator size/stride/offset logic.
     *
     * @param current    The "vanilla" vertex size to be corrected
     * @param multiplier The amount of vertices to compute the size/stride/offset for
     *
     * @return The computed value
     */
    @StableAPI.Expose
    public static int recomputeVertexInfo(int current, int multiplier) {
        return VertexInfo.recomputeVertexInfo(current, multiplier);
    }
}
