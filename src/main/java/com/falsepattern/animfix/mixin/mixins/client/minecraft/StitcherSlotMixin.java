package com.falsepattern.animfix.mixin.mixins.client.minecraft;

import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import lombok.val;
import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(Stitcher.Slot.class)
public abstract class StitcherSlotMixin implements IStitcherSlotMixin {
    @Shadow private Stitcher.Holder holder;

    @Shadow public abstract boolean addSlot(Stitcher.Holder p_94182_1_);

    @Shadow private List subSlots;

    @Mutable
    @Shadow @Final private int originX;

    @Mutable
    @Shadow @Final private int originY;

    @Override
    public void insertHolder(Stitcher.Holder holder) {
        this.holder = holder;
    }

    @Override
    public void reparent(int offsetX, int offsetY) {
        originX += offsetX;
        originY += offsetY;
        if (this.subSlots != null) {
            for (val subSlot: subSlots) {
                ((IStitcherSlotMixin)subSlot).reparent(offsetX, offsetY);
            }
        }
    }

    @Override
    public void insertSlot(Stitcher.Slot slot) {
        if (this.subSlots == null) {
            this.subSlots = new ArrayList();
        }
        ((IStitcherSlotMixin)slot).reparent(originX, originY);
        this.subSlots.add(slot);
    }
}
