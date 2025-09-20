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

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.ventooth.swansong.tessellator.ShaderTess;

public class VertexInfo {
    private static final int VANILLA_SIZE = 8;
    private static int SHADER_SIZE;
    private static int shaderOffset;
    private static int vanillaOffset = 8;
    private static int extraVertexInts = 0;
    private static boolean shaderInitialized = false;

    private static void initShaderCompat() {
        if (shaderInitialized) {
            return;
        }
        shaderInitialized = true;
        if (Compat.swanSongInstalled()) {
            SHADER_SIZE = SwansongCompat.getVertexStrideInts(1);
            shaderOffset = SHADER_SIZE;
        } else {
            SHADER_SIZE = 18;
            shaderOffset = 18;
        }
    }

    public static synchronized void allocateExtraVertexSlots(int count, int[] indices, int[] shaderIndices) {
        initShaderCompat();
        if (!ModuleConfig.TRIANGULATOR()) {
            throw new IllegalStateException(
                    "Could not allocate vertex slots. Please enable the Triangulator module inside falsetweaks.cfg");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be > 0");
        }
        if (indices.length != count) {
            throw new IllegalArgumentException("indices array size must be equal to count");
        }
        if (shaderIndices.length != count) {
            throw new IllegalArgumentException("shaderIndices array size must be equal to count");
        }
        for (int i = 0; i < count; i++) {
            indices[i] = vanillaOffset++;
            shaderIndices[i] = shaderOffset++;
        }
        extraVertexInts += count;
    }

    public static int recomputeVertexInfo(int currentInts, int multiplier) {
        return (currentInts + extraVertexInts) * multiplier;
    }

    public static int getVertexInfo(Compat.ShaderType type, int multiplier) {
        initShaderCompat();
        switch (type) {
            case None:
                return recomputeVertexInfo(VANILLA_SIZE, multiplier);
            case Optifine:
                return recomputeVertexInfo(SHADER_SIZE, multiplier);
            case Swansong:
                return SwansongCompat.getVertexStrideInts(multiplier);
        }
        throw new AssertionError();
    }

    private static class SwansongCompat {
        public static int getVertexStrideInts(int multiplier) {
            return ShaderTess.vertexStrideInt() * multiplier;
        }
    }
}
