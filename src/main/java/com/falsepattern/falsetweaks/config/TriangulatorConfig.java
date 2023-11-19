/*
 * This file is part of FalseTweaks.
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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MODID,
        category = "triangulator")
public class TriangulatorConfig {
    @Config.Comment("Used to toggle the namesake feature of this mod: quad triangulation.\n" +
                    "If you turn this off, the triangulation will not execute, but you will still have the AO and the\n" +
                    "smooth lighting fixes.\n" +
                    "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,\n" +
                    "on weaker systems, it may noticeably decrease render performance (integrated graphics).\n" +
                    "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.\n" +
                    "FPS impact: System-dependent. Intel iGPUs struggle when this is enabled.")
    @Config.LangKey("config.falsetweaks.triangulator.enable_quad_triangulation")
    @Config.DefaultBoolean(false)
    public static boolean ENABLE_QUAD_TRIANGULATION;

    @Config.Comment("Block corners and edges between chunks might have \"cracks\" in them. This option fixes it.\n" +
                    "FPS impact: None")
    @Config.LangKey("config.falsetweaks.triangulator.fix_block_crack")
    @Config.DefaultBoolean(true)
    public static boolean FIX_BLOCK_CRACK;

    @Config.Comment("Try setting this to true if the game crashes with a mixin conflict inside RenderBlocks.\n" +
                    "FPS impact: Minor decrease")
    @Config.LangKey("config.falsetweaks.triangulator.render_hook_compat_mode")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean RENDER_HOOK_COMPAT_MODE;

    @Config.Comment("The \"epsilon\" value for the block crack fix inside chunks. Set this a bit higher if you can\n" +
                    "still see light leaking between solid blocks in dark areas.\n" +
                    "Advanced setting.\n" +
                    "FPS impact: None")
    @Config.LangKey("config.falsetweaks.triangulator.block_crack_fix_epsilon")
    @Config.DefaultDouble(0.0005)
    @Config.RangeDouble(min = 0,
                        max = 0.005)
    public static double BLOCK_CRACK_FIX_EPSILON;

    @Config.Comment("Block classes that have bugs when rendering with the crack fix can be put here to avoid manipulating them\n.")
    @Config.LangKey("config.falsetweaks.triangulator.block_crack_fix_blacklist")
    @Config.DefaultStringList({"net.minecraft.block.BlockCauldron", "net.minecraft.block.BlockStairs"})
    public static String[] BLOCK_CRACK_FIX_BLACKLIST;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
