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
package com.falsepattern.falsetweaks.modules.occlusion;

import net.minecraft.client.renderer.ActiveRenderInfo;

public class PreviousActiveRenderInfo {
    public static float objectX = Float.NaN, objectY, objectZ;
    public static float rotationX, rotationZ, rotationYZ;

    public static boolean changed() {
        return PreviousActiveRenderInfo.objectX != ActiveRenderInfo.objectX ||
            PreviousActiveRenderInfo.objectY != ActiveRenderInfo.objectY ||
            PreviousActiveRenderInfo.objectZ != ActiveRenderInfo.objectZ ||
            PreviousActiveRenderInfo.rotationX != ActiveRenderInfo.rotationX ||
            PreviousActiveRenderInfo.rotationYZ != ActiveRenderInfo.rotationYZ ||
            PreviousActiveRenderInfo.rotationZ != ActiveRenderInfo.rotationZ;
    }

    public static void update() {
        PreviousActiveRenderInfo.objectX = ActiveRenderInfo.objectX;
        PreviousActiveRenderInfo.objectY = ActiveRenderInfo.objectY;
        PreviousActiveRenderInfo.objectZ = ActiveRenderInfo.objectZ;
        PreviousActiveRenderInfo.rotationX = ActiveRenderInfo.rotationX;
        PreviousActiveRenderInfo.rotationYZ = ActiveRenderInfo.rotationYZ;
        PreviousActiveRenderInfo.rotationZ = ActiveRenderInfo.rotationZ;
    }
}
