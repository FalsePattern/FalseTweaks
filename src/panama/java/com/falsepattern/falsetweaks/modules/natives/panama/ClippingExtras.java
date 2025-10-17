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

package com.falsepattern.falsetweaks.modules.natives.panama;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ClippingExtras {
    private static final Arena arena = Arena.ofConfined();
    private static final MemorySegment frust = arena.allocate(ValueLayout.JAVA_FLOAT, 24);
    public static void setFrustum(float[][] frustum) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                frust.setAtIndex(ValueLayout.JAVA_FLOAT, i * 6 + j, frustum[j][i]);
            }
        }
        Clipping.setFrustum(frust);
    }
}
