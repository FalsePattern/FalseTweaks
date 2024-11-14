package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MOD_ID,
        category = "rendering_safety")
public class RenderingSafetyConfig {
    @Config.Comment("Enable safety wrapper for inventory blocks.")
    @Config.LangKey("config.falsetweaks.rendering_safety.block")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_BLOCK;
    @Config.Comment("Enable safety wrapper for tile entities.")
    @Config.LangKey("config.falsetweaks.rendering_safety.tesr")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_TESR;

    static {
        ConfigurationManager.selfInit();
    }

    public static void init() {

    }
}
