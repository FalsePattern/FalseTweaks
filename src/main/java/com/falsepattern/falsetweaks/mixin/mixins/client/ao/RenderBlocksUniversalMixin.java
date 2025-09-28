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

package com.falsepattern.falsetweaks.mixin.mixins.client.ao;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.RenderBlocks;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksUniversalMixin {
    @Unique
    private int ft$countS;
    @Unique
    private int ft$countB;
    @Unique
    private float ft$lightSky;
    @Unique
    private float ft$lightBlock;

    @Unique
    private void ft$addLight(int light) {
        int S = light & 0xff;
        int B = (light & 0xff0000) >>> 16;
        if (S != 0) {
            ft$lightSky += S;
            ft$countS++;
        }
        if (B != 0) {
            ft$lightBlock += B;
            ft$countB++;
        }
    }

    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public int getAoBrightness(int a, int b, int c, int d) {
        ft$countS = 0;
        ft$countB = 0;
        ft$lightSky = 0;
        ft$lightBlock = 0;
        ft$addLight(a);
        ft$addLight(b);
        ft$addLight(c);
        ft$addLight(d);
        ft$lightSky /= ft$countS;
        ft$lightBlock /= ft$countB;
        return (((int) ft$lightSky) & 0xff) | ((((int) ft$lightBlock) & 0xff) << 16);
    }
}
