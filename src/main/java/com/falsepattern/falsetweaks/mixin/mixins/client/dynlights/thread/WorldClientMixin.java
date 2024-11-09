package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.thread;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsWorldClient;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin implements DynamicLightsWorldClient {
    @Dynamic
    private boolean ft$renderItemInFirstPerson;

    @Override
    public boolean ft$renderItemInFirstPerson() {
        return ft$renderItemInFirstPerson && ThreadedChunkUpdateHelper.isMainThread();
    }
}
