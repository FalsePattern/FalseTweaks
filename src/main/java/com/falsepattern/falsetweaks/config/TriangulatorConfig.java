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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config.Comment("Miscellaneous renderer tweaks")
@Config(modid = Tags.MOD_ID,
        category = "triangulator")
@Config.LangKey
public class TriangulatorConfig {
    @Config.Comment({"Used to toggle the namesake feature of this mod: quad triangulation.",
                     "If you turn this off, the triangulation will not execute, but you will still have the AO and the",
                     "smooth lighting fixes.",
                     "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,",
                     "on weaker systems, it may noticeably decrease render performance (integrated graphics).",
                     "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.",
                     "FPS impact: System-dependent. Intel iGPUs struggle when this is enabled."})
    @Config.LangKey
    @Config.Name(value = "quadTriangulation",
                 migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean ENABLE_QUAD_TRIANGULATION;

    @Config.Comment({"Block corners and edges between chunks might have \"cracks\" in them. This option fixes it.",
                     "FPS impact: None"})
    @Config.LangKey
    @Config.Name(value = "fixBlockCrack",
                 migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean FIX_BLOCK_CRACK;

    @Config.Comment({"Try setting this to true if the game crashes with a mixin conflict inside RenderBlocks.",
                     "FPS impact: Minor decrease"})
    @Config.LangKey
    @Config.Name(value = "renderHookCompatMode",
                 migrations = "")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean RENDER_HOOK_COMPAT_MODE;

    @Config.Comment("Disable the calibration chat prompt")
    @Config.LangKey
    @Config.Name(value = "hideCalibrationPrompt",
                 migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean SUPPRESS_CALIBRATION;

    @Config.Comment({"The \"epsilon\" value for the block crack fix inside chunks. Set this a bit higher if you can",
                     "still see light leaking between solid blocks in dark areas.",
                     "Advanced setting.",
                     "FPS impact: None"})
    @Config.LangKey
    @Config.Name(value = "blockCrackFixEpsilon",
                 migrations = "")
    @Config.RangeDouble(min = 0,
                        max = 0.005)
    @Config.DefaultDouble(0.0005)
    public static double BLOCK_CRACK_FIX_EPSILON;

    @Config.Comment("Block classes that have bugs when rendering with the crack fix can be put here to avoid manipulating them\n.")
    @Config.LangKey
    @Config.Name(value = "blockCrackFixBlacklist",
                 migrations = "")
    @Config.DefaultStringList({"net.minecraft.block.BlockCauldron", "net.minecraft.block.BlockStairs"})
    public static String[] BLOCK_CRACK_FIX_BLACKLIST;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }

    @Config(modid = Tags.MOD_ID,
            customPath = "falsetweaks_userspecific",
            category = "calibration")
    public static class Calibration {
        @Config.Comment("Modifies the way ambient occlusion alignment is calculated. Used for compatibility purposes,\n" +
                        "because different graphics cards have different ways of processing quads.\n" +
                        "This is useful when quad triangulation is disabled, or if the triangulator gets disabled internally\n" +
                        "for compatibility reasons.")
        @Config.DefaultBoolean(false)
        public static boolean FLIP_DIAGONALS;

        @Config.Comment("The SHA256 hash of the graphics card that this calibration was configured for.")
        @Config.DefaultString("undefined")
        public static String GPU_HASH;

        static {
            ConfigurationManager.selfInit();
        }
    }
}
