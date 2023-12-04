package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.optifine;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

import static com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.MAIN_THREAD;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin extends World {
    @Dynamic
    @Shadow(remap = false)
    public boolean renderItemInFirstPerson;

    public WorldClientMixin(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
        super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
    }

    @Dynamic
    @Redirect(method = {"getLightBrightnessForSkyBlocks", "func_72802_i"},
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/multiplayer/WorldClient;renderItemInFirstPerson:Z"),
              require = 1)
    private boolean noHandLightOffThread(WorldClient self) {
        return renderItemInFirstPerson && Thread.currentThread() == MAIN_THREAD;
    }
}
