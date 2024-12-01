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

package com.falsepattern.falsetweaks.modules.mipmapfix;

// Merged from Hodgepodge Copyright (C) GTNH Team
public class Mipmaps {

    private static final float[] VALS = new float[256];

    public static float get(int i) {
        return VALS[i & 0xFF];
    }

    public static int getColorComponent(int one, int two, int three, int four, int bits) {
        float f = Mipmaps.get(one >> bits);
        float g = Mipmaps.get(two >> bits);
        float h = Mipmaps.get(three >> bits);
        float i = Mipmaps.get(four >> bits);
        float j = (float) Math.pow((f + g + h + i) * 0.25, 0.45454545454545453);
        return (int) (j * 255.0);
    }

    static {
        for (int i = 0; i < VALS.length; ++i) {
            VALS[i] = (float) Math.pow((float) i / 255.0F, 2.2);
        }
    }
}