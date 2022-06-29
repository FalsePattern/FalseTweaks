package com.falsepattern.triangulator.calibration;

import com.falsepattern.lib.config.Config;
import com.falsepattern.triangulator.Tags;

@Config(modid = Tags.MODID,
        category = "calibration")
public class CalibrationConfig {
    @Config.Comment("Modifies the way ambient occlusion alignment is calculated. Used for compatibility purposes,\n" +
                    "because different graphics cards have different ways of processing quads.\n" +
                    "This is useful when quad triangulation is disabled, or if the triangulator gets disabled internally\n" +
                    "for compatibility reasons.")
    @Config.DefaultBoolean(false)
    public static boolean FLIP_DIAGONALS;

    @Config.Comment("The SHA256 hash of the graphics card that this calibration was configured for.")
    @Config.DefaultString("undefined")
    public static String GPU_HASH;
}
