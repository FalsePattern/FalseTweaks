package com.falsepattern.falsetweaks.mixin.mixins.client.cc.of;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@Mixin(targets = "ChunkCacheOF",
       remap = false)
public abstract class ChunkCacheOFMixin extends ChunkCache {
    public ChunkCacheOFMixin(World p_i1964_1_, int p_i1964_2_, int p_i1964_3_, int p_i1964_4_, int p_i1964_5_, int p_i1964_6_, int p_i1964_7_, int p_i1964_8_) {
        super(p_i1964_1_, p_i1964_2_, p_i1964_3_, p_i1964_4_, p_i1964_5_, p_i1964_6_, p_i1964_7_, p_i1964_8_);
    }

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
        return DynamicLightsDrivers.frontend.forWorldMesh().getCombinedLight(x, y, z, combinedLight);
    }

    @Redirect(method = "getLightBrightnessForSkyBlocksRaw",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/IBlockAccess;getLightBrightnessForSkyBlocks(IIII)I",
                       remap = true),
              require = 1)
    private int brightnessFromSuper(IBlockAccess instance, int x, int y, int z, int lightValue) {
        return super.getLightBrightnessForSkyBlocks(x, y, z, lightValue);
    }

    @Dynamic
    @Redirect(method = "func_147439_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;",
                       remap = true),
              remap = true,
              require = 3)
    private Block blockFromSuper(IBlockAccess instance, int x, int y, int z) {
        return super.getBlock(x, y, z);
    }
}
