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

package com.falsepattern.falsetweaks.api;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.lib.StableAPI;

import java.util.Optional;

@StableAPI(since = "2.0.0")
public final class Modules {
    @StableAPI.Expose
    public static boolean startupOptimizationsActive() {
        return ModuleConfig.STARTUP_OPTIMIZATIONS;
    }

    @StableAPI.Expose
    public static boolean textureOptimizationsActive() {
        return ModuleConfig.TEXTURE_OPTIMIZATIONS;
    }

    @StableAPI.Expose
    public static boolean itemVoxelizerActive() {
        return ModuleConfig.VOXELIZER;
    }

    @StableAPI.Expose
    public static boolean triangulatorActive() {
        return ModuleConfig.TRIANGULATOR();
    }

    @StableAPI.Expose
    public static boolean itemRenderListsActive() {
        return ModuleConfig.ITEM_RENDER_LISTS;
    }

    @StableAPI.Expose
    public static boolean beaconOptimizationActive() {
        return ModuleConfig.BEACON_OPTIMIZATION;
    }

    @StableAPI.Expose(since = "2.2.0")
    public static boolean advancedProfilerActive() {
        return ModuleConfig.ADVANCED_PROFILER;
    }

    @StableAPI.Expose
    public static boolean tileEntityTransparencyFixActive() {
        return ModuleConfig.TE_TRANSPARENCY_FIX;
    }

    @StableAPI.Expose
    public static boolean threadingActive() {
        return ModuleConfig.THREADED_CHUNK_UPDATES();
    }

    @StableAPI.Expose
    public static boolean bspSortingActive() {
        return ModuleConfig.BSP_SORTING();
    }

    @StableAPI.Expose
    @Deprecated
    public static Optional<Boolean> leakFixActive() {
        return Optional.of(ModuleConfig.THREADED_CHUNK_UPDATES());
    }

    @StableAPI.Expose
    @Deprecated
    public static boolean leakFixMixinsInjected() {
        return ModuleConfig.THREADED_CHUNK_UPDATES();
    }
}
