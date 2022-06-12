package com.falsepattern.animfix.mixin.mixins.client.minecraft;

import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(Stitcher.Slot.class)
public abstract class StitcherSlotMixin implements IStitcherSlotMixin {
    @Shadow private Stitcher.Holder holder;

    @Shadow public abstract boolean addSlot(Stitcher.Holder p_94182_1_);

    @Shadow private List subSlots;

    @Override
    public void insertHolder(Stitcher.Holder holder) {
        this.holder = holder;
    }

    @Override
    public void insertSlot(Stitcher.Slot slot) {
        if (this.subSlots == null) {
            this.subSlots = new ArrayList();
        }
        this.subSlots.add(slot);
    }
}
