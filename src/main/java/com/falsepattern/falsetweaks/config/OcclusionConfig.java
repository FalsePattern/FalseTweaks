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
        category = "occlusion")
public class OcclusionConfig {

    @Config.Comment("Similar to OptiFine's \"Dynamic Updates\" feature, where chunks load faster when you don't move the player at all.\n" +
                    "This deadline is in FPS.\n" +
                    "0 to disable")
    @Config.LangKey("config.falsetweaks.occlusion.deadline")
    @Config.DefaultInt(60)
    @Config.RangeInt(min = 0,
                     max = 1000)
    public static int DYNAMIC_CHUNK_UPDATES_DEADLINE;


    @Config.Comment("The occlusion caller uses a dynamic allocation for renderlists.\n" +
                    "You can set this to any value above zero, but setting it too high will eat a bit more VRAM. 4096 is\n" +
                    "a decent safe point.\n" +
                    "FPS impact: zero when tuned right")
    @Config.RangeInt(min = 0)
    @Config.LangKey("config.falsetweaks.occlusion.cache_size_target")
    @Config.DefaultInt(4096)
    public static int CACHE_SIZE_TARGET;

    @Config.Comment("Changes the maximum render distance.\n" +
                    "NOTE: things might get extremely laggy above 32 without serverside performance mods!\n")
    @Config.RangeInt(min = 16, max = 64)
    @Config.LangKey("config.falsetweaks.occlusion.render_distance")
    @Config.DefaultInt(32)
    public static int RENDER_DISTANCE;

    @Config.Comment("Makes sure that the clipping helper is only initialized once per frame.\n" +
                    "Saves a bunch of opengl data retrieval calls and some matrix math, but might lead of weird/broken\n" +
                    "culling behaviour, so this is disabled by default.\n" +
                    "DO NOT REPORT BUGS IF YOU TURNED THIS ON!")
    @Config.LangKey("config.falsetweaks.occlusion.aggressive_clipping_helper")
    @Config.DefaultBoolean(false)
    public static boolean AGGRESSIVE_CLIPPING_HELPER_OPTIMIZATIONS;



    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
