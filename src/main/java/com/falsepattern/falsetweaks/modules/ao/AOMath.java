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

package com.falsepattern.falsetweaks.modules.ao;

import lombok.val;

public final class AOMath {
    private AOMath() {}

    public static float biLerpAO(float q00, float q10, float q01, float q11, float tx, float ty) {
        val x1 = lerpAO(q00, q10, tx);
        val x2 = lerpAO(q01, q11, tx);
        return lerpAO(x1, x2, ty);
    }

    public static float averageAO(float a, float b) {
        return (a + b) / 2.0f;
    }

    public static float averageAO(float a, float b, float c, float d) {
        return (a + b + c + d) / 4.0f;
    }

    public static float lerpAO(float a, float b, float fract) {
        return a + (b - a) * fract;
    }
}
