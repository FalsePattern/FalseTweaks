package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;

public class TriCompat {
    public static boolean enableTriangulation() {
        return TriConfig.ENABLE_QUAD_TRIANGULATION;
    }
}
