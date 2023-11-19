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
        category = "occlusion and threading")
public class OcclusionConfig {
    @Config.Comment("The number of threads to use for chunk building.\n" +
                    "The default is 1, which shouldn't be any laggier than vanilla but will reduce stutter.\n" +
                    "If you have a lot of cores increasing this may be beneficial.\n" +
                    "The value of 0 will set it to half of your total system threads (unaware of P/E cores on modern intel cpus!)")
    @Config.DefaultInt(1)
    @Config.RangeInt(min = 0)
    @Config.RequiresMcRestart
    public static int CHUNK_UPDATE_THREADS;

    @Config.Comment("The maximum amount of queued chunk updates per thread.\n" +
                    "Set this higher if you have a CPU with powerful cores.")
    @Config.DefaultInt(16)
    @Config.RangeInt(min = 1,
                     max = 256)
    @Config.RequiresMcRestart
    public static int UPDATE_QUEUE_SIZE_PER_THREAD;

    @Config.Comment("Changes the enableThreadedChunkUpdates option to never wait for chunk updates.\n" +
                    "Improves framerate when blocks are placed or destroyed, at the cost of introducing visual delay.\n" +
                    "This is analogous to 1.18's 'Chunk Builder' option, false meaning 'Fully Blocking', and true meaning 'Threaded'.")
    @Config.DefaultBoolean(false)
    public static boolean DISABLE_BLOCKING_CHUNK_UPDATES;

    @Config.Comment("Similar to OptiFine's \"Dynamic Updates\" feature, where chunks load faster when you don't move the player at all.\n" +
                    "This deadline is in FPS.\n" +
                    "0 to disable")
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


    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
