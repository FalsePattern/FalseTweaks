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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.modules.leakfix.LeakFixState;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MODID,
        category = "00_modules")
@Config.RequiresMcRestart
public class ModuleConfig {
    @Config.Comment("Optimizes the way forge scans the classpath during launch.\n" +
                    "Not compatible with some badly-written mods.\n" +
                    "FPS impact: None, but makes startup a bit faster")
    @Config.DefaultBoolean(true)
    public static boolean STARTUP_OPTIMIZATIONS;

    @Config.Comment("Enable/Disable texture optimizations. This includes:\n" +
                    "- Multithreaded animated textures\n" +
                    "- Faster texture atlas packing during startup\n" +
                    "FPS impact: Reduced stuttering in heavily modded packs")
    @Config.DefaultBoolean(true)
    public static boolean TEXTURE_OPTIMIZATIONS;

    @Config.Comment("Enable/Disable item voxelization. This fixes a huge amount of item render issues, but is still\n" +
                    "an experimental feature.\n" +
                    "Also includes the 3D rails.")
    @Config.DefaultBoolean(true)
    public static boolean VOXELIZER;

    @Config.Comment("Enables the Triangulator module. This also includes the ambient occlusion and smooth lighting fix," +
                    "along with the block crack fix.\n" +
                    "If you want to use those fixes without having triangulated meshes, set the ENABLE_QUAD_TRIANGULATION\n" +
                    "property to false in the triangulator category.\n" +
                    "FPS impact: Small performance decrease, but smooth lighting will look way better.")
    @Config.DefaultBoolean(true)
    public static boolean TRIANGULATOR;

    @Config.Comment("Dropped and held items are re-rendered every frame, generating a lot of\n" +
                    "unnecessary CPU load because of the very inefficient way the rendering is done.\n" +
                    "With this enabled, FalseTweaks will cache pre-rendered versions of items into RenderLists\n" +
                    "(same things that chunks use) to minimize the amount of work done by the cpu for every single item.\n" +
                    "FPS impact: Decent improvement with lots of items on ground")
    @Config.DefaultBoolean(true)
    public static boolean ITEM_RENDER_LISTS;

    @Config.Comment("Beacons also have an optimization using renderlists. If you spot any issues related to beacons,\n" +
                    "you can toggle said optimization here.\n" +
                    "FPS impact: Tiny improvement near beacons")
    @Config.DefaultBoolean(true)
    public static boolean BEACON_OPTIMIZATION;

    @Config.Comment("Another renderlist tweak. Minecraft preallocates 55 thousand renderlists for the chunk rendering.\n" +
                    "The main advantage of this is reduced buffer allocations, so theoretically, it's faster.\n" +
                    "Unfortunately, by not clearing these buffers, they start leaking a LOT of memory over time,\n" +
                    "which gets emphasized on 21.X/22.X AMD windows drivers, and on MESA 21.X/22.X with linux. This patch\n" +
                    "Replaces the preallocated block by a dynamic allocation system, with each chunk creating and\n" +
                    "deleting these renderlists based on usage. Requires a game restart to apply.\n" +
                    "NOTICE FOR OPTIFINE USERS:\n" +
                    "Setting this to Enable blocks Smooth and Multi-Core chunkloading. If you want multicore\n" +
                    "chunk loading, you MUST set this do Disable.\n" +
                    "NOTICE FOR NEODYMIUM USERS:\n" +
                    "Setting this to Enable may break Neodymium's render pipeline.\n" +
                    "FPS impact: It depends")
    @Config.DefaultEnum("Disable")
    public static LeakFixState MEMORY_LEAK_FIX;

    @Config.Comment("Transparent tile entities (beacons, for instance) might render behind other tile entities that are\n" +
                    "actually BEHIND the transparent part. Sorting the tile entities before rendering fixes this bug.\n" +
                    "FPS impact: Slight decrease")
    @Config.DefaultBoolean(false)
    public static boolean TE_TRANSPARENCY_FIX;

    @Config.Comment("3D particles (experimental)\n" +
                    "FPS impact: Unknown")
    @Config.DefaultBoolean(false)
    public static boolean CUBIC_PARTICLES;

    @Config.Comment("Improves the mipmap system of minecraft with 2 things:\n" +
                    "1. Every tiny texture is upscaled to at least 16x16 to allow for 4 mipmap levels, even if a mod has a texture smaller than 16x16.\n" +
                    "2. Replaces the mipmap generation with a multithreaded system, which scales with the number of cores in your system.\n" +
                    "FPS impact: none, but resource pack reload times (and startup time) are cut down by a lot")
    @Config.DefaultBoolean(true)
    public static boolean MIPMAP_FIX;

    @Config.Comment("Replaces the minecraft profiler with fully custom logic (used in the Shift+F3 pie chart)\n" +
                    "Also check the profiler config category!\n" +
                    "FPS impact: Slightly faster profiler")
    @Config.DefaultBoolean(true)
    public static boolean ADVANCED_PROFILER;

    static {
        ConfigurationManager.selfInit();
        LeakFixConfig.init();
        ProfilerConfig.init();
        RenderListConfig.init();
        TriangulatorConfig.init();
        VoxelizerConfig.init();
    }

    //This is here to make the static initializer run
    public static void init() {

    }

}
