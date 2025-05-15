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

package com.falsepattern.falsetweaks.modules.triangulator;

import com.falsepattern.falsetweaks.config.ModuleConfig;

public class VertexInfo {
    public static final int VANILLA_SIZE = 8;
    public static final int OPTIFINE_SIZE = 18;
    private static int optifineOffset = 18;
    private static int vanillaOffset = 8;
    private static int extraVertexInts = 0;

    public static synchronized void allocateExtraVertexSlots(int count, int[] indices, int[] optiFineIndices) {
        if (!ModuleConfig.TRIANGULATOR()) {
            throw new IllegalStateException("Could not allocate vertex slots. Please enable the Triangulator module inside falsetweaks.cfg");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be > 0");
        }
        if (indices.length != count) {
            throw new IllegalArgumentException("indices array size must be equal to count");
        }
        if (optiFineIndices.length != count) {
            throw new IllegalArgumentException("optiFineIndices array size must be equal to count");
        }
        for (int i = 0; i < count; i++) {
            indices[i] = vanillaOffset++;
            optiFineIndices[i] = optifineOffset++;
        }
        extraVertexInts += count;
    }

    public static int recomputeVertexInfo(int currentInts, int multiplier) {
        return (currentInts + extraVertexInts) * multiplier;
    }
}
