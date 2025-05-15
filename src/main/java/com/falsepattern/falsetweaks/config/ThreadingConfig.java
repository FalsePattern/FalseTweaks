/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

@Config.Comment("Multithreaded rendering settings. Also look at the occlusion category.")
@Config(modid = Tags.MOD_ID,
        category = "threading")
@Config.LangKey
public class ThreadingConfig {
    @Config.Comment({
            "The number of threads to use for chunk building.",
            "1   - Recommended",
            "2-8 - For higher-end systems, with diminishing results"
    })
    @Config.LangKey
    @Config.Name(value = "threads", migrations = "")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 1,
                     max = 8)
    public static int CHUNK_UPDATE_THREADS;

    @Config.Comment("Disable this to use a slower, but more accurate thread safety check in the tessellator.")
    @Config.LangKey
    @Config.Name(value = "fastChecks", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean FAST_SAFETY_CHECKS;

    @Config.Comment({
            "This enabled even deeper integration with Neodymium.",
            "Needs a game restart to change.",
            "Only effective if Neodymium is installed.",
            "WARNING: This is known to make blocks like MicroBlocks, ProjectRed wires, and Mekanism pipes flicker!",
            "It might also break other mods' custom block renderers.",
            "ONLY ENABLE IF YOU KNOW WHAT YOU'RE DOING, AND TURN THIS OFF BEFORE REPORTING ANY BUGS!",
            "FPS Impact: Huge increase"
    })
    @Config.LangKey
    @Config.Name(value = "experimentalNeodymiumThreading", migrations = "UNSTABLE_EXPERIMENTAL_NEODYMIUM_THREADING_DO_NOT_REPORT_BUGS")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean NEODYMIUM_THREADING;

    @Config.Comment({
            "Enables some extra debug info for error stacktraces.",
            "EXPENSIVE! Only turn this on for debugging purposes!",
            "FPS Impact: significant slowdown"
    })
    @Config.LangKey
    @Config.Name(value = "extraDebugInfo", migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean EXTRA_DEBUG_INFO;

    @Config.Comment({
            "Classes added here will be automatically patched to use the threaded Tessellator.",
            "FalseTweaks also includes an internal hardcoded list of patched classes.",
            "Use * at the end of a line for a wildcard match (useful for targeting whole packages!)",
            "This patch covers most edge cases, however some implementations will still require manual patches."
    })
    @Config.LangKey
    @Config.Name(value = "tessellatorReplacementTargets", migrations = "")
    @Config.ListMaxLength(Integer.MAX_VALUE)
    @Config.StringMaxLength(65535)
    @Config.DefaultStringList({})
    @Config.RequiresMcRestart
    public static String[] TESSELLATOR_USE_REPLACEMENT_TARGETS;

    @Config.Comment("Patches every class with the thread safe tessellator code. Overrides TESSELLATOR_USE_REPLACEMENT_TARGETS")
    @Config.LangKey
    @Config.Name(value = "tessellatorReplaceEverything", migrations = "")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean TESSELLATOR_REPLACE_EVERYTHING;

    @Config.Comment({
            "ISimpleBlockRenderingHandler classes added here will be treated as thread-safe.",
            "In many cases, these classes should also be included in TESSELLATOR_USE_REPLACEMENT_TARGETS.",
            "Syntax: classname:constructor",
            "Examples:",
            "Implicitly thread-safe (stateless):                                                 com.example.ExampleRenderer:safe",
            "Default constructor (aka: new ExampleRenderer()):                                   com.example.ExampleRenderer:default!",
            "Custom constructor supplied by a utility mod (creates a new instance every call):   com.example.ExampleRenderer:com.mymod.ThreadTools!createExampleRenderer",
            "Custom threadlocal managed by a utility mod (returns the same instance per thread): com.example.ExampleRenderer:com.mymod.ThreadTools?threadExampleRenderer",
            "All of these MUST be zero argument methods!"
    })
    @Config.LangKey
    @Config.Name(value = "threadSafeBlockRenderers", migrations = "")
    @Config.DefaultStringList({})
    @Config.ListMaxLength(Integer.MAX_VALUE)
    @Config.StringMaxLength(65535)
    @Config.RequiresMcRestart
    public static String[] THREAD_SAFE_ISBRHS;

    @Config.Comment("Disables the logging of block rendering handler registrations.")
    @Config.LangKey
    @Config.Name(value = "logBlockRendererErrors", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean LOG_ISBRH_ERRORS;

    @Config.Comment("Speeds up the threading of block bounds, try turning this off if you get compatibility issues.")
    @Config.LangKey
    @Config.Name(value = "fastThreadedBlockBounds", migrations = "")
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
                AGGRESSIVE_NEODYMIUM_THREADING = NEODYMIUM_THREADING;
            }
        }
        return AGGRESSIVE_NEODYMIUM_THREADING;
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
