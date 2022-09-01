package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;

public class TriCompat {
    private static Boolean NEODYMIUM = null;

    public static boolean neodymiumInstalled() {
        if (NEODYMIUM == null) {
            try {
                NEODYMIUM = ((LaunchClassLoader)TriCompat.class.getClassLoader()).getClassBytes("makamys.neodymium.Neodymium") != null;
            } catch (IOException e) {
                e.printStackTrace();
                NEODYMIUM = false;
            }
        }
        return NEODYMIUM;
    }
    public static boolean enableTriangulation() {
        return TriConfig.ENABLE_QUAD_TRIANGULATION && !neodymiumInstalled();
    }
}
