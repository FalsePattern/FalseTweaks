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

import lombok.Getter;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class CameraInfo {

    @Getter
    private static final CameraInfo instance = new CameraInfo();

    /**
     * The transformed eye position, which takes the third person camera offset into account.
     */
    @Getter
    private double x, y, z;
    /**
     * The untransformed eye position, which is not affected by the third person camera. It's always at the player character's eyes.
     */
    @Getter
    private double eyeX, eyeY, eyeZ;
    /**
     * The chunk coordinates of the transformed eye position, which takes the third person camera offset into account.
     */
    @Getter
    private int chunkCoordX, chunkCoordY, chunkCoordZ;

    public void update(EntityLivingBase view, double tick) {
        eyeX = view.lastTickPosX + (view.posX - view.lastTickPosX) * tick;
        eyeY = view.lastTickPosY + (view.posY - view.lastTickPosY) * tick;
        eyeZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * tick;

        x = eyeX + ActiveRenderInfo.objectX;
        y = eyeY + ActiveRenderInfo.objectY;
        z = eyeZ + ActiveRenderInfo.objectZ;

        chunkCoordX = MathHelper.floor_double(x / 16.0);
        chunkCoordY = MathHelper.floor_double(y / 16.0);
        chunkCoordZ = MathHelper.floor_double(z / 16.0);
    }
}
