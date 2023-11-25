package com.falsepattern.falsetweaks.modules.animfix;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.util.ConfigFixUtil;

public class AnimFixCompat {
    public static void executeConfigFixes() {
        HodgePodgeCompat.executeHodgepodgeConfigFixes();
    }

    public static class HodgePodgeCompat {
        public static void executeHodgepodgeConfigFixes() {
            ConfigFixUtil.fixConfig("hodgepodge.cfg", (line) -> {
                if (line.contains("optimizeTextureLoading")) {
                    return line.replace("true", "false");
                }
                return line;
            }, (e) -> Share.log.fatal("Failed to apply HodgePodge texture optimization compatibility patches!", e));
        }
    }
}
