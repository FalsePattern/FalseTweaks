package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.techguns;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import techguns.client.renderer.block.RenderLadder;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

@Mixin(value = RenderLadder.class,
       remap = false)
public abstract class RenderLadderMixin {
    /**
     * @author FalsePattern
     * @reason This method is stupid
     */
    @Overwrite
    private void adjustLightFixture(IBlockAccess world, int i, int j, int k, Block block) {
    }
}
