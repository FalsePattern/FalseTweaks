package com.falsepattern.falsetweaks.modules.dynlights;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;
import com.falsepattern.falsetweaks.config.DynamicLightsConfig;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.dynlights.base.DynamicLights;
import stubpackage.Config;

public class DynamicLightsDrivers {
    private static int backendPriority = 1000;
    public static DynamicLightsDriver backend;
    public static DynamicLightsDriver frontend = DynamicLightsNoOp.INSTANCE;
    private static boolean initialized = false;

    public static void registerBackend(DynamicLightsDriver backend, int priority) {
        if (initialized) {
            throw new IllegalStateException("Frontend already initialized! Register dynamic light backends in preInit or init!");
        }
        if (priority < backendPriority) {
            backendPriority = priority;
            DynamicLightsDrivers.backend = backend;
        } else if (priority == backendPriority) {
            Share.log.warn("Dynamic lights backend with colliding priority registered!\nExisting backend class: {}\nNew backend class: {}",
                           DynamicLightsDrivers.backend.getClass().getName(), backend.getClass().getName());
            DynamicLightsDrivers.backend = backend;
        }
    }

    public static void postInit() {
        initialized = true;
        if (Compat.optiFineHasDynamicLights()) {
            frontend = new DynamicLightsOF();
        } else if (DynamicLightsConfig.STATE != DynamicLightsConfig.DynamicLightsState.Disabled) {
            if (backend == null) {
                backend = new DynamicLights();
            }
            frontend = backend;
        } else {
            backend = frontend = DynamicLightsNoOp.INSTANCE;
        }
    }

    public static boolean isDynamicLights() {
        if (Compat.optiFineHasDynamicLights()) {
            return OptiFineCompat.isDynamicLights();
        } else {
            return ModuleConfig.DYNAMIC_LIGHTS && DynamicLightsConfig.STATE != DynamicLightsConfig.DynamicLightsState.Disabled;
        }
    }

    public static boolean isDynamicLightsFast() {
        if (Compat.optiFineHasDynamicLights()) {
            return OptiFineCompat.isDynamicLightsFast();
        } else {
            return ModuleConfig.DYNAMIC_LIGHTS && DynamicLightsConfig.STATE == DynamicLightsConfig.DynamicLightsState.Fast;
        }
    }
    public static boolean isDynamicHandLight() {
        if (Compat.optiFineHasDynamicLights()) {
            return OptiFineCompat.isDynamicHandLight();
        } else {
            return ModuleConfig.DYNAMIC_LIGHTS && DynamicLightsConfig.STATE != DynamicLightsConfig.DynamicLightsState.Disabled && DynamicLightsConfig.DYNAMIC_HAND_LIGHT;
        }
    }

    private static class OptiFineCompat {
        public static boolean isDynamicLights() {
            return Config.isDynamicLights();
        }
        public static boolean isDynamicLightsFast() {
            return Config.isDynamicLightsFast();
        }
        public static boolean isDynamicHandLight() {
            return Config.isDynamicHandLight();
        }
    }
}