package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.of;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "ChunkCacheOF",
       remap = false)
public abstract class ChunkCacheOFMixin {
    @Redirect(method = "getLightBrightnessForSkyBlocksRaw",
              at = @At(value = "INVOKE",
                       target = "LConfig;isDynamicLights()Z"),
              require = 1)
    private boolean ftDynamicLights() {
        return DynamicLightsDrivers.frontend.enabled();
    }

    @Redirect(method = "getLightBrightnessForSkyBlocksRaw",
              at = @At(value = "INVOKE",
                       target = "LDynamicLights;getCombinedLight(IIII)I"),
              require = 1)
    private int ftCombinedLights(int x, int y, int z, int combinedLight) {
        return DynamicLightsDrivers.frontend.getCombinedLight(x, y, z, combinedLight);
    }
}
