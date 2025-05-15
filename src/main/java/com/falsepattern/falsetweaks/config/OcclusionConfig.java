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

import static com.falsepattern.falsetweaks.api.Constants.MaximumRenderDistance;

@Config.Comment("Improved chunk rendering scheduler. Also look at the threading category.")
@Config(modid = Tags.MOD_ID,
        category = "occlusion")
@Config.LangKey
public class OcclusionConfig {

    @Config.Comment("Similar to OptiFine's \"Dynamic Updates\" feature, where chunks load faster when you don't move the player at all.")
    @Config.LangKey
    @Config.Name(value = "dynamicChunkUpdates", migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean DYNAMIC_CHUNK_UPDATES;

    @Config.Comment({
            "The amount of chunks renderers to update PER SECOND. This is a MAXIMUM limit, not a minimum.",
            "Every chunk has 32 chunk renderers (16 subchunks, each has 2 render passes)"
    })
    @Config.LangKey
    @Config.Name(value = "chunkUpdatesPerSecond", migrations = "")
    @Config.RangeInt(min = 10, max = 10000)
    @Config.DefaultInt(200)
    public static int CHUNK_UPDATES_PER_SECOND;

    @Config.Comment({
            "The occlusion caller uses a dynamic allocation for renderlists.",
            "You can set this to any value above zero, but setting it too high will eat a bit more VRAM. 4096 is",
            "a decent safe point.",
            "FPS impact: zero when tuned right"
    })
    @Config.LangKey
    @Config.Name(value = "cacheSizeTarget", migrations = "")
    @Config.RangeInt(min = 0)
    @Config.DefaultInt(4096)
    public static int CACHE_SIZE_TARGET;

    @Config.Comment({
            "Changes the maximum render distance.",
            "NOTE: things might get extremely laggy above 32 without serverside performance mods!"
    })
    @Config.LangKey
    @Config.Name(value = "renderDistance", migrations = "")
    @Config.RangeInt(min = 16, max = MaximumRenderDistance)
    @Config.DefaultInt(32)
    public static int RENDER_DISTANCE;

    @Config.Comment({
            "Makes sure that the clipping helper is only initialized once per frame.",
            "Saves a bunch of opengl data retrieval calls and some matrix math, but might lead of weird/broken",
            "culling behaviour, so this is disabled by default.",
            "DO NOT REPORT BUGS IF YOU TURNED THIS ON!"
    })
    @Config.LangKey
    @Config.Name(value = "aggressiveClippingHelperOptimizations", migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean AGGRESSIVE_CLIPPING_HELPER_OPTIMIZATIONS;


    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
