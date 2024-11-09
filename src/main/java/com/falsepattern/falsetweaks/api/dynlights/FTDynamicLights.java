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
    public static boolean isDynamicHandLight() {
        return DynamicLightsDrivers.isDynamicHandLight();
    }
}
