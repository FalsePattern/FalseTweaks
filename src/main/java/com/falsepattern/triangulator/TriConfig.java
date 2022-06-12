package com.falsepattern.triangulator;

import com.falsepattern.lib.config.Config;

@Config(modid = Tags.MODID)
public class TriConfig {
    @Config.Comment("Used to toggle the namesake feature of this mod: quad triangulation.\n" +
                    "If you turn this off, the triangulation will not execute, but you will still have the AO and the\n" +
                    "smooth lighting fixes.\n" +
                    "Triangulation fixes an issue with incorrectly-aligned quads causing a minor visual bug, however,\n" +
                    "on weaker systems, it may noticeably decrease render performance (integrated graphics).\n" +
                    "By sacrificing a bit of visual quality, you might get back a few extra FPS depending on your system.")
    public static boolean ENABLE_QUAD_TRIANGULATION = true;

    @Config.Comment("In vanilla code, dropped and held items are re-rendered every frame, generating a lot of\n" +
                    "unnecessary CPU load because of the very inefficient way the rendering is done.\n" +
                    "With this enabled, Triangulator will cache pre-rendered versions of items into RenderLists\n" +
                    "(same things that chunks use) to minimize the amount of work done by the cpu for every single item.\n" +
                    "Notice: Turn this off if you notice any weird rendering artifacts with items, as this feature\n" +
                    "is still highly experimental.\n")
    public static boolean ENABLE_ITEM_RENDERLISTS = true;

    @Config.Comment("The total amount of renderlists Triangulator may allocate for optimized item rendering.\n" +
                    "When the limit is exceeded, the render list that was used the longest time ago gets released.\n" +
                    "Bigger buffer sizes use more VRAM, but also get a higher average performance.\n" +
                    "256 should be good enough for most modded games, and going above 1024 is not recommended unless\n" +
                    "you have a lot of VRAM.\n" +
                    "(Only useful if you have ENABLE_ITEM_RENDERLISTS turned on)")
    @Config.RangeInt(min = 64)
    public static int ITEM_RENDERLIST_BUFFER_MAX_SIZE = 256;
}
