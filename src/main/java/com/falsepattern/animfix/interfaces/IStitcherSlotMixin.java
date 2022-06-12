package com.falsepattern.animfix.interfaces;

import net.minecraft.client.renderer.texture.Stitcher;

public interface IStitcherSlotMixin {
    void insertHolder(Stitcher.Holder holder);
    void insertSlot(Stitcher.Slot slot);
    void reparent(int offsetX, int offsetY);
}
