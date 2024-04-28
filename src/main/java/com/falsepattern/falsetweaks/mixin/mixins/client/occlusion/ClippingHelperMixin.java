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
package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import lombok.val;
import org.joml.Math;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.culling.ClippingHelper;

@Mixin(ClippingHelper.class)
public abstract class ClippingHelperMixin {
    @Shadow
    public float[][] frustum;

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for (int i = 0; i < 6; ++i) {
            val frust = this.frustum[i];
            float fX = frust[0];
            float fY = frust[1];
            float fZ = frust[2];
            float fW = frust[3];
            float FmX = fX * (float) minX;
            float FMX = fX * (float) maxX;
            float FmY = fY * (float) minY;
            float FMY = fY * (float) maxY;

            float F_mZ_W = Math.fma(fZ, (float) minZ, fW);
            float F_MZ_W = Math.fma(fZ, (float) maxZ, fW);

            float F_mY_mZ_W = FmY + F_mZ_W;
            float F_MY_mZ_W = FMY + F_mZ_W;

            float F_mY_MZ_W = FmY + F_MZ_W;
            float F_MY_MZ_W = FMY + F_MZ_W;

            boolean signSum = true;

            signSum &= signum(FmX + F_mY_mZ_W);
            signSum &= signum(FMX + F_mY_mZ_W);
            signSum &= signum(FmX + F_MY_mZ_W);
            signSum &= signum(FMX + F_MY_mZ_W);
            signSum &= signum(FmX + F_mY_MZ_W);
            signSum &= signum(FMX + F_mY_MZ_W);
            signSum &= signum(FmX + F_MY_MZ_W);
            signSum &= signum(FMX + F_MY_MZ_W);


            if (signSum) {
                return false;
            }
        }

        return true;
    }

    private static boolean signum(float x) {
        return x < 0;
    }
}
