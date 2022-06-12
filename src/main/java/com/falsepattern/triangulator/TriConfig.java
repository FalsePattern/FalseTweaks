package com.falsepattern.triangulator;

import com.falsepattern.lib.config.Config;

@Config(modid = Tags.MODID)
public class TriConfig {
    @Config.Comment("Used to toggle the namesake feature of this mod: quad triangulation.\n" +
                    "If you turn this off, the triangulation will not execute, but you will still have the AO and the\n" +
                    "smooth lighting fixes.\n" +
                    "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,\n" +
                    "on weaker systems, it may noticeably decrease render performance (integrated graphics).\n" +
                    "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.")
    public static boolean ENABLE_QUAD_TRIANGULATION = true;
}
