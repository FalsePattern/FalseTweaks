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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.blockbounds;

import com.falsepattern.falsetweaks.modules.threadedupdates.FastThreadLocal;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeBlockBounds;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Unique
@Mixin(Block.class)
public abstract class BlockMixin_Root implements ThreadSafeBlockBounds {
    private final FastThreadLocal.DynamicValue<AxisAlignedBB> ft$threadBounds = new FastThreadLocal.DynamicValue<>();

    private AxisAlignedBB ft$initialBounds;
    private boolean ft$boundsModified;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;setBlockBounds(FFFFFF)V"),
              require = 1)
    private void initBounds(Block instance, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.ft$initialBounds = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 1D, 1D, 1D);
        this.ft$boundsModified = false;
    }

    @Override
    public boolean ft$boundsModified() {
        return ft$boundsModified;
    }

    @Override
    public void ft$bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)  {
        val bounds = ft$writeableBounds();
        bounds.minX = minX;
        bounds.minY = minY;
        bounds.minZ = minZ;
        bounds.maxX = maxX;
        bounds.maxY = maxY;
        bounds.maxZ = maxZ;
    }

    @Override
    public void ft$minX(double minX) {
        ft$writeableBounds().minX = minX;
    }

    @Override
    public double ft$minX() {
        return ft$readableBounds().minX;
    }

    @Override
    public void ft$maxX(double maxX) {
        ft$writeableBounds().maxX = maxX;
    }

    @Override
    public double ft$maxX() {
        return ft$readableBounds().maxX;
    }

    @Override
    public void ft$minY(double minY) {
        ft$writeableBounds().minY = minY;
    }

    @Override
    public double ft$minY() {
        return ft$readableBounds().minY;
    }

    @Override
    public void ft$maxY(double maxY) {
        ft$writeableBounds().maxY = maxY;
    }

    @Override
    public double ft$maxY() {
        return ft$readableBounds().maxY;
    }

    @Override
    public void ft$minZ(double minZ) {
        ft$writeableBounds().minZ = minZ;
    }

    @Override
    public double ft$minZ() {
        return ft$readableBounds().minZ;
    }

    @Override
    public void ft$maxZ(double maxZ) {
        ft$writeableBounds().maxZ = maxZ;
    }

    @Override
    public double ft$maxZ() {
        return ft$readableBounds().maxZ;
    }

    @Override
    public AxisAlignedBB ft$writeableBounds() {
        if (!ft$boundsModified)
            ft$boundsModified = true;
        return ft$bounds();
    }

    @Override
    public AxisAlignedBB ft$readableBounds() {
        return ft$bounds();
    }

    private AxisAlignedBB ft$bounds() {
        if (ft$initialBounds == null)
            throw new AssertionError("Something something we failed during init!");
        if (ThreadedChunkUpdateHelper.isMainThread() || !ft$boundsModified)
            return ft$initialBounds;

        var threadBounds = ft$threadBounds.get();
        if (threadBounds == null) {
            threadBounds = ft$initialBounds.copy();
            ft$threadBounds.set(threadBounds);
        }
        return threadBounds;
    }
}
