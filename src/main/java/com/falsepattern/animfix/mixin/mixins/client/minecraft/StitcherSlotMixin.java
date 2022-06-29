package com.falsepattern.animfix.mixin.mixins.client.minecraft;

import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.texture.Stitcher;

@Mixin(Stitcher.Slot.class)
public abstract class StitcherSlotMixin implements IStitcherSlotMixin {
    @Shadow
    private Stitcher.Holder holder;

    @Override
    public void insertHolder(Stitcher.Holder holder) {
        this.holder = holder;
    }
}
