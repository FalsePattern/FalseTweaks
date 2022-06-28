package com.falsepattern.animfix.config;

import com.falsepattern.animfix.Tags;
import com.falsepattern.lib.config.Config;

@Config(modid = Tags.MODID)
public class AnimConfig {
    @Config.Comment("The largest width and height animated textures can have to get put into the buffer.\n" +
                    "Making this higher will batch higher resolution textures too, but will consume more RAM.")
    @Config.RangeInt(min = 16, max = 1024)
    public static int maximumBatchedTextureSize = 32;
}