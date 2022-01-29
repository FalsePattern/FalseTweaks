package com.falsepattern.animfix.interfaces;

import net.minecraft.client.renderer.texture.Stitcher;

import java.util.List;

public interface IRecursiveStitcher {
    void doNotRecurse();
    List<Stitcher.Slot> getSlots();
}
