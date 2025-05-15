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

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeBlockBounds;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Block.class)
public abstract class BlockMixin_Impl implements ThreadSafeBlockBounds {
    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        ft$bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMinX() {
        return ft$minX();
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMaxX() {
        return ft$maxX();
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMinY() {
        return ft$minY();
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMaxY() {
        return ft$maxY();
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMinZ() {
        return ft$minZ();
    }

    /**
     * @author Ven
     * @reason Thread-Safety
     */
    @Overwrite
    public final double getBlockBoundsMaxZ() {
        return ft$maxZ();
    }
}
