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

package com.falsepattern.falsetweaks.api.dynlights;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import com.falsepattern.lib.StableAPI;

@StableAPI(since = "__EXPERIMENTAL__")
public class FTDynamicLights {
    @StableAPI.Expose
    public static DynamicLightsDriver frontend() {
        return DynamicLightsDrivers.frontend;
    }

    @StableAPI.Expose
    public static void registerBackend(DynamicLightsDriver backend, int priority) {
        DynamicLightsDrivers.registerBackend(backend, priority);
    }

    @StableAPI.Expose
    public static boolean isDynamicLights() {
        return DynamicLightsDrivers.isDynamicLights();
    }

    @StableAPI.Expose
    public static boolean isDynamicLightsFast() {
        return DynamicLightsDrivers.isDynamicLightsFast();
    }

    @StableAPI.Expose
    public static boolean isDynamicHandLight(boolean forWorld) {
        return DynamicLightsDrivers.isDynamicHandLight(forWorld);
    }

    @StableAPI.Expose
    public static boolean isCircular() {
        return DynamicLightsDrivers.isCircular();
    }
}
