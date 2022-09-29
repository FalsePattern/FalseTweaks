/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.voxelizer.strategy;

import com.falsepattern.lib.StableAPI;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.function.Supplier;

@RequiredArgsConstructor
@StableAPI(since = "__INTERNAL__")
public enum StrategyPreset {
    @StableAPI.Expose
    Unoptimized_0(NullMergingStrategy.NULL),
    @StableAPI.Expose
    Fast_1(RowColumnMergingStrategy.NoFlip),
    @StableAPI.Expose
    Regular_2(ExpandingRectMergingStrategy.NoFlipNoInvRD),
    @StableAPI.Expose
    Best_3(((Supplier<MergingStrategy>)() -> {
        val strats = new ArrayList<MergingStrategy>();
        strats.addAll(RowColumnMergingStrategy.all());
        strats.addAll(ExpandingRectMergingStrategy.all());
        return new BestMergingStrategy(strats);
    }).get())
    ;
    @StableAPI.Expose
    public final MergingStrategy strategy;
}
