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

    @Config.Comment("Enable an optimized, BSP-tree based vertex sorting algorithm for transparent blocks.\n" +
                    "FPS impact: A little bit less stuttering when moving around with a lot of stained glass-like blocks around")
    @Config.DefaultBoolean(true)
    public static boolean BSP_SORTING;

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

    @Config.Comment("Transparent tile entities (beacons, for instance) might render behind other tile entities that are\n" +
                    "actually BEHIND the transparent part. Sorting the tile entities before rendering fixes this bug.\n" +
                    "FPS impact: Slight decrease")
    @Config.DefaultBoolean(false)
    public static boolean TE_TRANSPARENCY_FIX;

    @Config.Comment("Makes translucent geometry of chunks render slightly closer to the camera. (experimental)\n" +
                    "This reduces Z-Fighting on blocks which have overlapping opaque and translucent geometry,\n" +
                    "at the cost of far away geometry sometimes rendering on top of opaque geometry. (>100~ blocks)\n" +
                    "FPS impact: Unknown")
    @Config.DefaultBoolean(false)
    public static boolean BLOCK_LAYER_TRANSPARENCY_FIX;

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

    @Config.Comment("Enables the 1.8-style occlusion culling originally developed by CoFHTweaks.\n" +
                    "Not compatible with ArchaicFix's occlusion tweaks.\n" +
                    "COMPATIBLE WITH OPTIFINE AND SHADERS\n" +
                    "FPS impact: Potentially huge gains, much faster chunk rendering")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OCCLUSION_TWEAKS;

    @Config.Comment("Enables multi-threaded chunk updating. It only works if enableOcclusionTweaks is turned on.\n" +
                    "Not compatible with quad triangulation (automatically disables it if you turn this on)\n" +
                    "COMPATIBLE WITH OPTIFINE AND SHADERS\n" +
                    "FPS impact: Depends on your CPU, but should be pretty good on modern CPUs")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean THREADED_CHUNK_UPDATES;

    @Config.Comment("Improves the performance of the minecraft sky mesh.\n" +
                    "Also fixes the weird white lines that some OptiFine shaderpacks get with huge render distances.\n" +
                    "FPS impact: Negligible gain")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean SKY_MESH_OPTIMIZATION;


    static {
        ConfigurationManager.selfInit();
        ProfilerConfig.init();
        RenderListConfig.init();
        TriangulatorConfig.init();
        VoxelizerConfig.init();
        TranslucentBlockLayersConfig.init();
        OcclusionConfig.init();
        ThreadingConfig.init();
    }

    //This is here to make the static initializer run
    public static void init() {

    }

}
