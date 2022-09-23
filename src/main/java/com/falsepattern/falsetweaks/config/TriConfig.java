/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.leakfix.LeakFixState;

@Config(modid = Tags.MODID)
public class TriConfig {
    @Config.Comment("Used to toggle the namesake feature of this mod: quad triangulation.\n" +
                    "If you turn this off, the triangulation will not execute, but you will still have the AO and the\n" +
                    "smooth lighting fixes.\n" +
                    "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,\n" +
                    "on weaker systems, it may noticeably decrease render performance (integrated graphics).\n" +
                    "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.\n" +
                    "FPS impact: System-dependent. Intel iGPUs struggle when this is enabled.")
    @Config.LangKey("config.falsetweaks.enable_quad_triangulation")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_QUAD_TRIANGULATION;

    @Config.Comment("In vanilla code, dropped and held items are re-rendered every frame, generating a lot of\n" +
                    "unnecessary CPU load because of the very inefficient way the rendering is done.\n" +
                    "With this enabled, FalseTweaks will cache pre-rendered versions of items into RenderLists\n" +
                    "(same things that chunks use) to minimize the amount of work done by the cpu for every single item.\n" +
                    "Notice: Turn this off if you notice any weird rendering artifacts with items, as this feature\n" +
                    "is still highly experimental.\n" +
                    "FPS impact: Decent improvement with lots of items on ground")
    @Config.LangKey("config.falsetweaks.enable_item_renderlists")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_ITEM_RENDERLISTS;

    @Config.Comment(
            "Beacons also have an optimization using renderlists. If you spot any issues related to beacons,\n" +
            "you can toggle said optimization here.," +
            "FPS impact: Tiny improvement near beacons")
    @Config.LangKey("config.falsetweaks.enable_beacon_optimization")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_BEACON_OPTIMIZATION;

    @Config.Comment("Another renderlist tweak. Minecraft preallocates 55 thousand renderlists for the chunk rendering.\n" +
                    "The main advantage of this is reduced buffer allocations, so theoretically, it's faster.\n" +
                    "Unfortunately, by not clearing these buffers, they start leaking a LOT of memory over time,\n" +
                    "which gets emphasized on modern AMD windows drivers, and on MESA with linux. This patch\n" +
                    "Replaces the preallocated block by a dynamic allocation system, with each chunk creating and\n" +
                    "deleting these renderlists based on usage. Requires a game restart to apply.\n" +
                    "NOTICE FOR OPTIFINE USERS:\n" +
                    "Setting this to Auto or Enable blocks Smooth and Multi-Core chunkloading. If you want multicore\n" +
                    "chunk loading, you MUST set this do Disable.\n" +
                    "FPS impact: It depends")
    @Config.RequiresMcRestart
    @Config.LangKey("config.falsetweaks.enable_memory_leak_fix")
    @Config.DefaultEnum("Auto")
    public static LeakFixState MEMORY_LEAK_FIX;

    @Config.Comment("The memory leak optimization unfortunately increases the amount of calls sent to the GPU.\n" +
                    "This pressure can be reduced with the help of caching, which temporarily stores inactive renderlists\n" +
                    "in a buffer, where renderers can then fetch them from when needed.\n" +
                    "You can set this to any value above zero, but setting it too high will eat a LOT of VRAM. 1024 is\n" +
                    "a decent safe point.\n" +
                    "FPS impact: zero when tuned right")
    @Config.RangeInt(min = 0)
    @Config.LangKey("config.falsetweaks.memory_leak_fix_cache_size_target")
    @Config.DefaultInt(1024)
    public static int MEMORY_LEAK_FIX_CACHE_SIZE_TARGET;

    @Config.Comment("The total amount of renderlists FalseTweaks may allocate for optimized item rendering.\n" +
                    "When the limit is exceeded, the render list that was used the longest time ago gets released.\n" +
                    "Bigger buffer sizes use more VRAM, but also get a higher average performance.\n" +
                    "256 should be good enough for most modded games, and going above 1024 is not recommended unless\n" +
                    "you have a lot of VRAM.\n" + "(Only useful if you have ENABLE_ITEM_RENDERLISTS turned on)\n" +
                    "FPS impact: zero when tuned right")
    @Config.RangeInt(min = 64)
    @Config.LangKey("config.falsetweaks.item_renderlist_buffer_max_size")
    @Config.DefaultInt(256)
    public static int ITEM_RENDERLIST_BUFFER_MAX_SIZE;

    @Config.Comment("Transparent tile entities (beacons, for instance) might render behind other tile entities that are\n" +
                    "actually BEHIND the transparent part. Sorting the tile entities before rendering fixes this bug.\n" +
                    "FPS impact: Slight decrease")
    @Config.LangKey("config.falsetweaks.te_transparency_fix")
    @Config.DefaultBoolean(false)
    public static boolean TE_TRANSPARENCY_FIX;

    @Config.Comment("Block corners and edges between chunks might have \"cracks\" in them. This option fixes it.\n" +
                    "FPS impact: None")
    @Config.LangKey("config.falsetweaks.fix_block_crack")
    @Config.DefaultBoolean(true)
    public static boolean FIX_BLOCK_CRACK;

    @Config.Comment("Corners on items have \"cracks\" on the corners (for instance, swords). This option fixes it.\n" +
                    "FPS impact: Negligible decrease")
    @Config.LangKey("config.falsetweaks.fix_item_crack")
    @Config.DefaultBoolean(true)
    public static boolean FIX_ITEM_CRACK;

    @Config.Comment("Try setting this to true if the game crashes with a mixin conflict inside RenderBlocks.\n" +
                    "FPS impact: Minor decrease")
    @Config.LangKey("config.falsetweaks.render_hook_compat_mode")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean RENDER_HOOK_COMPAT_MODE;

    static {
        ConfigurationManager.selfInit();
    }
}
