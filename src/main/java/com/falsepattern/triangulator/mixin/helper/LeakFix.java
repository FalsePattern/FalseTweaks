package com.falsepattern.triangulator.mixin.helper;

import com.falsepattern.triangulator.TriConfig;
import com.falsepattern.triangulator.Triangulator;
import org.lwjgl.opengl.GL11;

public final class LeakFix {
    public static final boolean ENABLED;
    static {
        switch (TriConfig.MEMORY_LEAK_FIX) {
            default:
                Triangulator.triLog.info("Disabling leak fix because of config flag.");
                ENABLED = false;
                break;
            case Auto:
                boolean isAMD = GL11.glGetString(GL11.GL_VENDOR).toLowerCase().contains("amd");
                if (isAMD) {
                    Triangulator.triLog.info("Enabling leak fix because an AMD gpu was detected.");
                    ENABLED = true;
                } else {
                    Triangulator.triLog.info("Disabling leak fix because an AMD gpu was NOT detected.");
                    ENABLED = false;
                }
                break;
            case Enable:
                Triangulator.triLog.info("Enabling leak fix because of config flag.");
                ENABLED = true;
                break;
        }
    }
}
