package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;
import cpw.mods.fml.common.Loader;

public class TriCompat {
    private static final boolean NEODYMIUM = Loader.isModLoaded("neodymium");
    public static boolean enableTriangulation() {
        return TriConfig.ENABLE_QUAD_TRIANGULATION && !NEODYMIUM;
    }
}
