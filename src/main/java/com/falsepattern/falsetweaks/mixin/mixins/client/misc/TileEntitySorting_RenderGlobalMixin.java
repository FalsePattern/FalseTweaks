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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.Comparator;
import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class TileEntitySorting_RenderGlobalMixin {
    @SuppressWarnings("rawtypes")
    @Shadow
    public List tileEntities;

    private static double AABBDistance(AxisAlignedBB AABB, EntityLivingBase player) {
        val x = player.posX;
        val y = player.posY;
        val z = player.posZ;
        val dx = max(AABB.minX - x, 0, x - AABB.maxX);
        val dy = max(AABB.minY - y, 0, y - AABB.maxY);
        val dz = max(AABB.minZ - z, 0, z - AABB.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    private static double max(double a, double b, double c) {
        return Math.max(a, Math.max(b, c));
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderHelper;enableStandardItemLighting()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void sortTEs(EntityLivingBase player, ICamera camera, float time, CallbackInfo ci) {
        val TEs = (List<TileEntity>) tileEntities;
        TEs.sort(Comparator.comparingDouble((te) -> -AABBDistance(te.getRenderBoundingBox(), player)));
    }
}
