package com.falsepattern.triangulator.api;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ToggleableTessellator {
    void suspendQuadTriangulation();

    void resumeQuadTriangulation();
}
