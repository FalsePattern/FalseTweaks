package com.falsepattern.triangulator.renderblocks;

import net.minecraft.block.Block;

public class RenderState {
    public Block block;
    public int x;
    public int y;
    public int z;
    public float r;
    public float g;
    public float b;
    public boolean useCustomColor;
    public int light;

    public void set(Block block, int x, int y, int z, float r, float g, float b, boolean useColor, int light) {
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.useCustomColor = useColor;
        this.light = light;
    }
}
