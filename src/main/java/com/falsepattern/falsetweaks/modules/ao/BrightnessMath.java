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

public final class BrightnessMath {
    private BrightnessMath() {}

    public static int lerpBrightness(int a, int b, double fract) {
        int sky = lerpBrightnessChannel(brightnessSky(a), brightnessSky(b), fract);
        int block = lerpBrightnessChannel(brightnessBlock(a), brightnessBlock(b), fract);
        return brightnessPack(sky, block);
    }

    public static int biLerpBrightness(int q00, int q10, int q01, int q11, double tx, double ty) {
        int sky = biLerpBrightnessChannel(brightnessSky(q00),
                                          brightnessSky(q10),
                                          brightnessSky(q01),
                                          brightnessSky(q11),
                                          tx, ty);
        int block = biLerpBrightnessChannel(brightnessBlock(q00),
                                            brightnessBlock(q10),
                                            brightnessBlock(q01),
                                            brightnessBlock(q11),
                                            tx, ty);
        return brightnessPack(sky, block);
    }

    public static int averageBrightness(int a, int b) {
        int sky = averageBrightnessChannel(brightnessSky(a),
                                           brightnessSky(b));

        int block = averageBrightnessChannel(brightnessBlock(a),
                                             brightnessBlock(b));

        return brightnessPack(sky, block);
    }

    public static int averageBrightness(int a, int b, int c, int d) {
        int sky = averageBrightnessChannel(brightnessSky(a),
                                           brightnessSky(b),
                                           brightnessSky(c),
                                           brightnessSky(d));

        int block = averageBrightnessChannel(brightnessBlock(a),
                                             brightnessBlock(b),
                                             brightnessBlock(c),
                                             brightnessBlock(d));

        return brightnessPack(sky, block);
    }

    private static int biLerpBrightnessChannel(int q00, int q10, int q01, int q11, double tx, double ty) {
        val x1 = lerpBrightnessChannel(q00, q10, tx);
        val x2 = lerpBrightnessChannel(q01, q11, tx);
        return lerpBrightnessChannel(x1, x2, ty);
    }

    private static int lerpBrightnessChannel(int a, int b, double fract) {
        return ((int) (a + (b - a) * fract)) & 0xFF;
    }

    private static int averageBrightnessChannel(int a, int b) {
        int count = 0;
        int sum = 0;
        if (a != 0) {
            count++;
            sum += a;
        }
        if (b != 0) {
            count++;
            sum += b;
        }
        return count == 0 ? 0 : ((sum / count) & 0xFF);
    }


    private static int averageBrightnessChannel(int a, int b, int c, int d) {
        int count = 0;
        int sum = 0;
        if (a != 0) {
            count++;
            sum += a;
        }
        if (b != 0) {
            count++;
            sum += b;
        }
        if (c != 0) {
            count++;
            sum += c;
        }
        if (d != 0) {
            count++;
            sum += d;
        }
        return count == 0 ? 0 : ((sum / count) & 0xFF);
    }

    private static int brightnessSky(int x) {
        return x & 0xFF;
    }

    private static int brightnessBlock(int x) {
        return (x & 0xff0000) >>> 16;
    }

    private static int brightnessPack(int sky, int block) {
        return (block << 16) | sky;
    }
}
