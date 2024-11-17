package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
@Config(modid = Tags.MOD_ID,
        category = "optifine_log_spam_fixes")
public class OptiSpamConfig {
    @Config.Comment("Suppresses \"Ambiguous shader option: ...\" warnings.")
    @Config.LangKey("config.falsetweaks.optispam.ambiguous")
    @Config.DefaultBoolean(true)
    public static boolean AMBIGUOUS_SHADER_OPTION;

    @Config.Comment("Suppresses \"Block not found for name: ...\" warnings.")
    @Config.LangKey("config.falsetweaks.optispam.block_not_found")
    @Config.DefaultBoolean(true)
    public static boolean BLOCK_NOT_FOUND;

    @Config.Comment("Suppresses \"Invalid block metadata: ...\" and \"Invalid block ID mapping: ...\" warnings.")
    @Config.LangKey("config.falsetweaks.optispam.invalid_id")
    @Config.DefaultBoolean(true)
    public static boolean INVALID_ID;

    @Config.Comment("Suppresses \"Expression already defined: ...\" and \"Custom uniform/variable: ...\" logs.")
    @Config.LangKey("config.falsetweaks.optispam.custom_uniforms")
    @Config.DefaultBoolean(true)
    public static boolean CUSTOM_UNIFORMS;
    static {
        ConfigurationManager.selfInit();
    }
    //This is here to make the static initializer run
    public static void init() {

    }
}
