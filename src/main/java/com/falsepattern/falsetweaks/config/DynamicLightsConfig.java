package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MOD_ID,
        category = "dynamiclights")
public class DynamicLightsConfig {
    @Config.Comment("Enable/disable dynamic lights without restarting the game")
    @Config.LangKey("config.falsetweaks.dynlights.state")
    @Config.DefaultEnum("Fast")
    public static DynamicLightsState STATE;
    @Config.Comment("Should items/blocks held by the player emit light?")
    @Config.LangKey("config.falsetweaks.dynlights.hand_light")
    @Config.DefaultBoolean(true)
    public static boolean DYNAMIC_HAND_LIGHT;
    static {
        ConfigurationManager.selfInit();
    }
    //This is here to make the static initializer run
    public static void init() {

    }

    public enum DynamicLightsState {
        Fast,
        Fancy,
        Disabled
    }
}
