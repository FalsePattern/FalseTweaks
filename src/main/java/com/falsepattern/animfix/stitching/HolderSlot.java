package com.falsepattern.animfix.stitching;

import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import lombok.val;
import net.minecraft.client.renderer.texture.Stitcher;

import java.util.Collections;
import java.util.List;

public class HolderSlot extends SpriteSlot {
    private final Stitcher.Holder holder;

    public HolderSlot(Stitcher.Holder holder) {
        this.holder = holder;
        width = holder.getWidth();
        height = holder.getHeight();
    }

    @Override
    public List<Stitcher.Slot> getSlots(Rect2D parent) {
        val slot = new Stitcher.Slot(x + parent.x, y + parent.y, width, height);
        ((IStitcherSlotMixin) slot).insertHolder(holder);
        return Collections.singletonList(slot);
    }
}
