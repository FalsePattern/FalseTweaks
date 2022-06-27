package com.falsepattern.triangulator.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.triangulator.Tags;
import com.falsepattern.triangulator.leakfix.LeakFixState;

@Config(modid = Tags.MODID)
public class TriConfig {
    @Config.Comment("Used to toggle the namesake feature of this mod: quad triangulation.\n" +
                    "If you turn this off, the triangulation will not execute, but you will still have the AO and the\n" +
                    "smooth lighting fixes.\n" +
                    "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,\n" +
                    "on weaker systems, it may noticeably decrease render performance (integrated graphics).\n" +
                    "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.")
    @Config.LangKey("config.triangulator.enable_quad_triangulation")
    public static boolean ENABLE_QUAD_TRIANGULATION = true;

    @Config.Comment("In vanilla code, dropped and held items are re-rendered every frame, generating a lot of\n" +
                    "unnecessary CPU load because of the very inefficient way the rendering is done.\n" +
                    "With this enabled, Triangulator will cache pre-rendered versions of items into RenderLists\n" +
                    "(same things that chunks use) to minimize the amount of work done by the cpu for every single item.\n" +
                    "Notice: Turn this off if you notice any weird rendering artifacts with items, as this feature\n" +
                    "is still highly experimental.\n")
    @Config.LangKey("config.triangulator.enable_item_renderlists")
    public static boolean ENABLE_ITEM_RENDERLISTS = true;

    @Config.Comment("Beacons also have an optimization using renderlists. If you spot any issues related to beacons,\n" +
                    "you can toggle said optimization here.")
    @Config.LangKey("config.triangulator.enable_beacon_optimization")
    public static boolean ENABLE_BEACON_OPTIMIZATION = true;

    @Config.Comment("Another renderlist tweak. Minecraft preallocates 55 thousand renderlists for the chunk rendering.\n" +
                    "The main advantage of this is reduced buffer allocations, so theoretically, it's faster.\n" +
                    "Unfortunately, by not clearing these buffers, they start leaking a LOT of memory over time,\n" +
                    "which gets emphasized on modern AMD windows drivers, and on MESA with linux. This patch\n" +
                    "Replaces the preallocated block by a dynamic allocation system, with each chunk creating and\n" +
                    "deleting these renderlists based on usage.\n" +
                    "Requires a game restart to apply.")
    @Config.RequiresMcRestart
    @Config.LangKey("config.triangulator.enable_memory_leak_fix")
    public static LeakFixState MEMORY_LEAK_FIX = LeakFixState.Auto;

    @Config.Comment("The memory leak optimization unfortunately increases the amount of calls sent to the GPU.\n" +
                    "This pressure can be reduced with the help of caching, which temporarily stores inactive renderlists\n" +
                    "in a buffer, where renderers can then fetch them from when needed.\n" +
                    "You can set this to any value above zero, but setting it too high will eat a LOT of VRAM. 1024 is\n" +
                    "a decent safe point.")
    @Config.RangeInt(min = 0)
    public static int MEMORY_LEAK_FIX_CACHE_SIZE_TARGET = 1024;

    @Config.Comment("The total amount of renderlists Triangulator may allocate for optimized item rendering.\n" +
                    "When the limit is exceeded, the render list that was used the longest time ago gets released.\n" +
                    "Bigger buffer sizes use more VRAM, but also get a higher average performance.\n" +
                    "256 should be good enough for most modded games, and going above 1024 is not recommended unless\n" +
                    "you have a lot of VRAM.\n" +
                    "(Only useful if you have ENABLE_ITEM_RENDERLISTS turned on)")
    @Config.RangeInt(min = 64)
    @Config.LangKey("config.triangulator.item_renderlist_buffer_max_size")
    public static int ITEM_RENDERLIST_BUFFER_MAX_SIZE = 256;
}
