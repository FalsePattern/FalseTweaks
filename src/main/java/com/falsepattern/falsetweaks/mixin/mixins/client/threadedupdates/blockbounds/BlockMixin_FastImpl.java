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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.blockbounds;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeBlockBounds;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin_FastImpl implements ThreadSafeBlockBounds {
    @Unique
    private static final int MIN_Y_SIDE = 0;
    @Unique
    private static final int MAX_Y_SIDE = 1;
    @Unique
    private static final int MIN_Z_SIDE = 2;
    @Unique
    private static final int MAX_Z_SIDE = 3;
    @Unique
    private static final int MIN_X_SIDE = 4;
    @Unique
    private static final int MAX_X_SIDE = 5;

    @Shadow
    public abstract void setBlockBoundsBasedOnState(IBlockAccess world, int posX, int posY, int posZ);

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int offsetPosX, int offsetPosY, int offsetPosZ, int side) {
        if (ft$boundsModified() && ft$areSideBoundsOffset(side))
            return true;
        val block = world.getBlock(offsetPosX, offsetPosY, offsetPosX);
        return !block.isOpaqueCube();
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int posX, int posY, int posZ) {
        return ft$readableBounds().getOffsetBoundingBox(posX, posY, posZ);
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int posX, int posY, int posZ) {
        return ft$readableBounds().getOffsetBoundingBox(posX, posY, posZ);
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    public MovingObjectPosition collisionRayTrace(World world,
                                                  int posX,
                                                  int posY,
                                                  int posZ,
                                                  Vec3 headVec,
                                                  Vec3 endVec) {
        if (headVec == null || endVec == null)
            return null;
        setBlockBoundsBasedOnState(world, posX, posY, posZ);

        headVec = headVec.addVector(-posX, -posY, -posZ);
        endVec = endVec.addVector(-posX, -posY, -posZ);

        val bounds = ft$readableBounds();
        val vecHitMinX = ft$hitVecBySide(bounds, headVec, endVec, MIN_X_SIDE);
        val vecHitMinY = ft$hitVecBySide(bounds, headVec, endVec, MIN_Y_SIDE);
        val vecHitMinZ = ft$hitVecBySide(bounds, headVec, endVec, MIN_Z_SIDE);
        val vecHitMaxX = ft$hitVecBySide(bounds, headVec, endVec, MAX_X_SIDE);
        val vecHitMaxY = ft$hitVecBySide(bounds, headVec, endVec, MAX_Y_SIDE);
        val vecHitMaxZ = ft$hitVecBySide(bounds, headVec, endVec, MAX_Z_SIDE);

        val sqDistMinX = ft$vecSqDistSafe(headVec, vecHitMinX);
        val sqDistMinY = ft$vecSqDistSafe(headVec, vecHitMinY);
        val sqDistMinZ = ft$vecSqDistSafe(headVec, vecHitMinZ);
        val sqDistMaxX = ft$vecSqDistSafe(headVec, vecHitMaxX);
        val sqDistMaxY = ft$vecSqDistSafe(headVec, vecHitMaxY);
        val sqDistMaxZ = ft$vecSqDistSafe(headVec, vecHitMaxZ);

        var hitSqDist = sqDistMinX;
        Vec3 hitVec = vecHitMinX;
        var hitSide = MIN_X_SIDE;

        if (sqDistMaxX < hitSqDist) {
            hitSqDist = sqDistMaxX;
            hitVec = vecHitMaxX;
            hitSide = MAX_X_SIDE;
        }
        if (sqDistMinY < hitSqDist) {
            hitSqDist = sqDistMinY;
            hitVec = vecHitMinY;
            hitSide = MIN_Y_SIDE;
        }
        if (sqDistMinZ < hitSqDist) {
            hitSqDist = sqDistMinZ;
            hitVec = vecHitMinZ;
            hitSide = MIN_Z_SIDE;
        }
        if (sqDistMaxY < hitSqDist) {
            hitSqDist = sqDistMaxY;
            hitVec = vecHitMaxY;
            hitSide = MAX_Y_SIDE;
        }
        if (sqDistMaxZ < hitSqDist) {
            hitSqDist = sqDistMaxZ;
            hitVec = vecHitMaxZ;
            hitSide = MAX_Z_SIDE;
        }

        if (hitSqDist == Double.MAX_VALUE || hitVec == null)
            return null;

        hitVec.xCoord += posX;
        hitVec.yCoord += posY;
        hitVec.zCoord += posZ;
        return new MovingObjectPosition(posX, posY, posZ, hitSide, hitVec);
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    private boolean isVecInsideYZBounds(Vec3 vec) {
        if (vec != null)
            return ft$vecYZBoundsCheck(ft$readableBounds(), vec);
        return false;
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    private boolean isVecInsideXZBounds(Vec3 vec) {
        if (vec != null)
            return ft$vecXZBoundsCheck(ft$readableBounds(), vec);
        return false;
    }

    /**
     * @author Ven
     * @reason Faster Thread-Safety
     */
    @Overwrite
    private boolean isVecInsideXYBounds(Vec3 vec) {
        if (vec != null)
            return ft$vecXYBoundsCheck(ft$readableBounds(), vec);
        return false;
    }

    @Unique
    private boolean ft$areSideBoundsOffset(int side) {
        val bounds = ft$readableBounds();
        switch (side) {
            case MIN_X_SIDE:
                return bounds.minX > 0D;
            case MAX_X_SIDE:
                return bounds.maxX < 1D;
            case MIN_Y_SIDE:
                return bounds.minY > 0D;
            case MAX_Y_SIDE:
                return bounds.maxY < 1D;
            case MIN_Z_SIDE:
                return bounds.minZ > 0D;
            case MAX_Z_SIDE:
                return bounds.maxZ < 1D;
        }
        return false;
    }

    @Unique
    private static double ft$vecSqDistSafe(Vec3 headVec, Vec3 vec) {
        if (vec != null)
            return headVec.squareDistanceTo(vec);
        return Double.MAX_VALUE;
    }

    @Unique
    private static Vec3 ft$hitVecBySide(AxisAlignedBB bounds, Vec3 headVec, Vec3 endVec, int side) {
        val vec = ft$intermediateVecBySide(bounds, headVec, endVec, side);
        if (vec != null && ft$vecBoundsCheckBySide(bounds, vec, side))
            return vec;
        return null;
    }

    @Unique
    private static Vec3 ft$intermediateVecBySide(AxisAlignedBB bounds, Vec3 headVec, Vec3 endVec, int side) {
        switch (side) {
            case MIN_X_SIDE:
                return headVec.getIntermediateWithXValue(endVec, bounds.minX);
            case MIN_Y_SIDE:
                return headVec.getIntermediateWithYValue(endVec, bounds.minY);
            case MIN_Z_SIDE:
                return headVec.getIntermediateWithZValue(endVec, bounds.minZ);
            case MAX_X_SIDE:
                return headVec.getIntermediateWithXValue(endVec, bounds.maxX);
            case MAX_Y_SIDE:
                return headVec.getIntermediateWithYValue(endVec, bounds.maxY);
            case MAX_Z_SIDE:
                return headVec.getIntermediateWithZValue(endVec, bounds.maxZ);
        }
        return null;
    }

    @Unique
    private static boolean ft$vecBoundsCheckBySide(AxisAlignedBB bounds, Vec3 vec, int side) {
        switch (side) {
            case MIN_X_SIDE:
            case MAX_X_SIDE:
                return ft$vecYZBoundsCheck(bounds, vec);
            case MIN_Y_SIDE:
            case MAX_Y_SIDE:
                return ft$vecXZBoundsCheck(bounds, vec);
            case MIN_Z_SIDE:
            case MAX_Z_SIDE:
                return ft$vecXYBoundsCheck(bounds, vec);
        }
        return false;
    }

    @Unique
    private static boolean ft$vecYZBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return ft$vecYBoundsCheck(bounds, vec) && ft$vecZBoundsCheck(bounds, vec);
    }

    @Unique
    private static boolean ft$vecXZBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return ft$vecXBoundsCheck(bounds, vec) && ft$vecZBoundsCheck(bounds, vec);
    }

    @Unique
    private static boolean ft$vecXYBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return ft$vecXBoundsCheck(bounds, vec) && ft$vecYBoundsCheck(bounds, vec);
    }

    @Unique
    private static boolean ft$vecXBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return vec.xCoord >= bounds.minX && vec.xCoord <= bounds.maxX;
    }

    @Unique
    private static boolean ft$vecYBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return vec.yCoord >= bounds.minY && vec.yCoord <= bounds.maxY;
    }

    @Unique
    private static boolean ft$vecZBoundsCheck(AxisAlignedBB bounds, Vec3 vec) {
        return vec.zCoord >= bounds.minZ && vec.zCoord <= bounds.maxZ;
    }
}
