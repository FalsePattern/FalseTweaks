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
package com.falsepattern.falsetweaks.modules.threadedupdates.saftey;

import com.falsepattern.falsetweaks.asm.modules.threadedupdates.block.Threading_BlockMinMaxRedirector;

import net.minecraft.util.AxisAlignedBB;

/**
 * These methods are injected as redirects using ASM.
 *
 * @see Threading_BlockMinMaxRedirector
 */
@SuppressWarnings("unused")
public interface ThreadSafeBlockBounds {
    boolean ft$boundsModified();

    void ft$bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    void ft$minX(double minX);

    double ft$minX();

    void ft$maxX(double maxX);

    double ft$maxX();

    void ft$minY(double minY);

    double ft$minY();

    void ft$maxY(double maxY);

    double ft$maxY();

    void ft$minZ(double minZ);

    double ft$minZ();

    void ft$maxZ(double maxZ);

    double ft$maxZ();

    AxisAlignedBB ft$writeableBounds();

    AxisAlignedBB ft$readableBounds();
}
