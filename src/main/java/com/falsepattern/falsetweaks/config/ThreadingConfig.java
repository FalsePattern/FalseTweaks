/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MOD_ID,
        category = "threading")
public class ThreadingConfig {
    @Config.Comment("The number of threads to use for chunk building.\n" +
                    "0   - For very low-end CPUs. Makes chunk building run on a throttled thread to avoid freezing your pc\n" +
                    "1   - Recommended\n" +
                    "2-8 - For higher-end systems, with diminishing results")
    @Config.LangKey("config.falsetweaks.threading.threads")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 0, max = 8)
    public static int CHUNK_UPDATE_THREADS;

    @Config.Comment("Disable this to use a slower, but more accurate thread safety check in the tessellator.")
    @Config.LangKey("config.falsetweaks.threading.fast_safety")
    @Config.DefaultBoolean(true)
    public static boolean FAST_SAFETY_CHECKS;

    @Config.Comment("EXPERIMENTAL AND UNSUPPORTED FEATURE!\n" +
                    "DO NOT REPORT CRASHES IF YOU TURN THIS ON!\n\n" +
                    "This enabled deep integration with Neodymium.\n" +
                    "Needs a game restart to change.\n" +
                    "Only effective if Neodymium is installed.\n" +
                    "FPS Impact: Unknown")
    @Config.LangKey("config.falsetweaks.threading.neodymium")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean UNSTABLE_EXPERIMENTAL_NEODYMIUM_THREADING_DO_NOT_REPORT_BUGS;

    @Config.Comment("Enables some extra debug info for error stacktraces.\n" +
                    "EXPENSIVE! Only turn this on for debugging purposes!\n" +
                    "FPS Impact: significant slowdown")
    @Config.LangKey("config.falsetweaks.threading.debug")
    @Config.DefaultBoolean(false)
    public static boolean EXTRA_DEBUG_INFO;
    @Config.Comment("Classes added here will be automatically patched to use the threaded Tessellator.\n" +
                    "FalseTweaks also includes an internal hardcoded list of patched classes.\n" +
                    "Use * at the end of a line for a wildcard match (useful for targeting whole packages!)\n" +
                    "This patch covers most edge cases, however some implementations will still require manual patches.")
    @Config.LangKey("config.falsetweaks.threading.tessellatorUseReplacementTargets")
    @Config.DefaultStringList({})
    @Config.ListMaxLength(Integer.MAX_VALUE)
    @Config.StringMaxLength(65535)
    @Config.RequiresMcRestart
    public static String[] TESSELLATOR_USE_REPLACEMENT_TARGETS;

    @Config.Comment("Patches every class with the thread safe tessellator code. Overrides TESSELLATOR_USE_REPLACEMENT_TARGETS")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean TESSELLATOR_REPLACE_EVERYTHING;

    @Config.Comment("ISimpleBlockRenderingHandler classes added here will be treated as thread-safe.\n" +
                    "In many cases, these classes should also be included in TESSELLATOR_USE_REPLACEMENT_TARGETS.\n" +
                    "Syntax: classname:constructor\n" +
                    "Examples:\n" +
                    "Implicitly thread-safe (stateless):                                                 com.example.ExampleRenderer:safe\n" +
                    "Default constructor (aka: new ExampleRenderer()):                                   com.example.ExampleRenderer:default!\n" +
                    "Custom constructor supplied by a utility mod (creates a new instance every call):   com.example.ExampleRenderer:com.mymod.ThreadTools!createExampleRenderer\n" +
                    "Custom threadlocal managed by a utility mod (returns the same instance per thread): com.example.ExampleRenderer:com.mymod.ThreadTools?threadExampleRenderer\n" +
                    "All of these MUST be zero argument methods!")
    @Config.LangKey("config.falsetweaks.threading.threadSafeISBRH")
    @Config.DefaultStringList({})
    @Config.ListMaxLength(Integer.MAX_VALUE)
    @Config.StringMaxLength(65535)
    @Config.RequiresMcRestart
    public static String[] THREAD_SAFE_ISBRHS;

    @Config.Comment("Disables the logging of block rendering handler registrations.")
    @Config.LangKey("config.falsetweaks.threading.logISBRHErrors")
    @Config.DefaultBoolean(true)
    public static boolean LOG_ISBRH_ERRORS;

    @Config.Comment("Speeds up the threading of block bounds, try turning this off if you get compatibility issues.")
    @Config.LangKey("config.falsetweaks.threading.fastThreadedBlockBounds")
    @Config.DefaultBoolean(true)
    public static boolean FAST_THREADED_BLOCK_BOUNDS;

    @Config.Ignore
    private static Boolean AGGRESSIVE_NEODYMIUM_THREADING;

    static {
        ConfigurationManager.selfInit();
    }

    public static boolean AGGRESSIVE_NEODYMIUM_THREADING() {
        if (AGGRESSIVE_NEODYMIUM_THREADING == null) {
            if (!ModuleConfig.THREADED_CHUNK_UPDATES()) {
                AGGRESSIVE_NEODYMIUM_THREADING = false;
            } else if (!Compat.neodymiumInstalled()) {
                AGGRESSIVE_NEODYMIUM_THREADING = false;
            } else {
                AGGRESSIVE_NEODYMIUM_THREADING = UNSTABLE_EXPERIMENTAL_NEODYMIUM_THREADING_DO_NOT_REPORT_BUGS;
            }
        }
        return AGGRESSIVE_NEODYMIUM_THREADING;
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
