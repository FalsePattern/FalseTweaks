package com.falsepattern.animfix.mixins.client;

import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(Stitcher.Slot.class)
public abstract class StitcherSlotMixin implements IStitcherSlotMixin {
    @Shadow private Stitcher.Holder holder;

    @Shadow public abstract boolean addSlot(Stitcher.Holder p_94182_1_);

    @Override
    public void insertHolder(Stitcher.Holder holder) {
        this.holder = holder;
    }

    @Override
    public void insertSlot(Stitcher.Slot slot) {
        Stitcher.Holder holder = slot.getStitchHolder();
        if (holder == null) {
            List<Stitcher.Slot> subSlots = new ArrayList<>();
            slot.getAllStitchSlots(subSlots);
            for (Stitcher.Slot subSlot : subSlots) {
                insertSlot(subSlot);
            }
        } else {
            addSlot(holder);
        }
    }
}
