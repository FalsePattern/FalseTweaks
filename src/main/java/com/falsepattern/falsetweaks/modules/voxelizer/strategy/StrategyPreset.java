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

package com.falsepattern.falsetweaks.modules.voxelizer.strategy;

import com.falsepattern.lib.StableAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@StableAPI(since = "__INTERNAL__")
public enum StrategyPreset {
    @StableAPI.Expose Unoptimized_0(NullMergingStrategy.NULL),
    @StableAPI.Expose Fast_1(RowColumnMergingStrategy.NoFlip),
    @StableAPI.Expose Best_2(ExpandingRectMergingStrategy.NoFlipNoInvRD);
    @StableAPI.Expose
    public final MergingStrategy strategy;
}
