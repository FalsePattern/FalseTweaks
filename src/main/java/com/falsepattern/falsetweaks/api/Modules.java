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

package com.falsepattern.falsetweaks.api;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.leakfix.LeakFixState;
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
        return ModuleConfig.TRIANGULATOR;
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
    public static Optional<Boolean> leakFixActive() {
        if (Share.LEAKFIX_CLASS_INITIALIZED) {
            return Optional.of(LeakFix.ENABLED);
        } else {
            return ModuleConfig.MEMORY_LEAK_FIX == LeakFixState.Disable ? Optional.of(Boolean.FALSE) : Optional.empty();
        }
    }

    @StableAPI.Expose
    public static boolean leakFixMixinsInjected() {
        return ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable;
    }
}
