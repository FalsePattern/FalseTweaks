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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeBlockBounds;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public abstract class BlockMixin implements ThreadSafeBlockBounds {
    @Unique
    private final ThreadLocal<AxisAlignedBB> ft$threadBounds = new ThreadLocal<>();

    @Unique
    private volatile AxisAlignedBB ft$initialBounds;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;setBlockBounds(FFFFFF)V"),
              require = 1)
    private void initBounds(Block instance, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        ft$initialBounds = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 1D, 1D, 1D);
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        val bounds = ft$bounds();

        bounds.minX = minX;
        bounds.minY = minY;
        bounds.minZ = minZ;
        bounds.maxX = maxX;
        bounds.maxY = maxY;
        bounds.maxZ = maxZ;
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMinX() {
        return ft$minX();
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMaxX() {
        return ft$maxX();
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMinY() {
        return ft$minY();
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMaxY() {
        return ft$maxY();
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMinZ() {
        return ft$minZ();
    }

    /**
     * @author Ven
     * @reason Thread-safe
     */
    @Overwrite
    public final double getBlockBoundsMaxZ() {
        return ft$maxZ();
    }

    @Override
    public void ft$minX(double minX) {
        ft$bounds().minX = minX;
    }

    @Override
    public void ft$maxX(double maxX) {
        ft$bounds().maxX = maxX;
    }

    @Override
    public void ft$minY(double minY) {
        ft$bounds().minY = minY;
    }

    @Override
    public void ft$maxY(double maxY) {
        ft$bounds().maxY = maxY;
    }

    @Override
    public void ft$minZ(double minZ) {
        ft$bounds().minZ = minZ;
    }

    @Override
    public void ft$maxZ(double maxZ) {
        ft$bounds().maxZ = maxZ;
    }

    @Override
    public double ft$minX() {
        return ft$bounds().minX;
    }

    @Override
    public double ft$maxX() {
        return ft$bounds().maxX;
    }

    @Override
    public double ft$minY() {
        return ft$bounds().minY;
    }

    @Override
    public double ft$maxY() {
        return ft$bounds().maxY;
    }

    @Override
    public double ft$minZ() {
        return ft$bounds().minZ;
    }

    @Override
    public double ft$maxZ() {
        return ft$bounds().maxZ;
    }

    @Unique
    private AxisAlignedBB ft$bounds() {
        if (ft$initialBounds == null)
            throw new AssertionError("Something something we failed during init!");
        if (ThreadedChunkUpdateHelper.isMainThread())
            return ft$initialBounds;

        var threadBounds = ft$threadBounds.get();
        if (threadBounds == null) {
            threadBounds = ft$initialBounds.copy();
            ft$threadBounds.set(threadBounds);
        }
        return threadBounds;
    }
}
