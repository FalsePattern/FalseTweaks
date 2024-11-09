package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.nonthread;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsWorldClient;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.WorldClient;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin implements DynamicLightsWorldClient {
    @Dynamic
    private boolean ft$renderItemInFirstPerson;

    @Override
    public boolean ft$renderItemInFirstPerson() {
        return ft$renderItemInFirstPerson;
    }
}
