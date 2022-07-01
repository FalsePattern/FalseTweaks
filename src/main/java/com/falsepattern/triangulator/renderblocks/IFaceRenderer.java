package com.falsepattern.triangulator.renderblocks;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

public interface IFaceRenderer {
    void render(Block block, int x, int y, int z, IIcon icon);
}
