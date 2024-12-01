/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

@Config(modid = Tags.MOD_ID,
        category = "00_modules")
@Config.RequiresMcRestart
public class ModuleConfig {
    @Config.Comment({
            "Optimizes the way forge scans the classpath during launch.",
            "Not compatible with some badly-written mods.",
            "FPS impact: None, but makes startup a bit faster"
    })
    @Config.DefaultBoolean(false)
    public static boolean STARTUP_OPTIMIZATIONS_V2;

    @Config.Comment({
            "Enable/Disable texture optimizations. This includes:",
            "- Multithreaded animated textures",
            "- Faster texture atlas packing during startup",
            "FPS impact: Reduced stuttering in heavily modded packs"
    })
    @Config.DefaultBoolean(true)
    public static boolean TEXTURE_OPTIMIZATIONS;

    @Config.Comment({
            "Enable/Disable item voxelization. This fixes a huge amount of item render issues, but is still",
            "an experimental feature.",
            "Also includes the 3D rails."
    })
    @Config.DefaultBoolean(true)
    public static boolean VOXELIZER;

    @Config.Comment({
            "Enables the Triangulator module. This also includes the ambient occlusion and smooth lighting fix," +
            "along with the block crack fix. Also provides the VertexAPI used by the BSP sorter and the threading system.",
            "If you want to use those fixes without having triangulated meshes, set the ENABLE_QUAD_TRIANGULATION",
            "property to false in the triangulator category.",
            "FPS impact: Tiny performance decrease, but smooth lighting will look way better."
    })
    @Config.DefaultBoolean(true)
    public static boolean TRIANGULATOR;

    @Config.Comment({
            "Enable an optimized, BSP-tree based vertex sorting algorithm for transparent blocks.",
            "Force-enables TRIANGULATOR.",
            "FPS impact: A little bit less stuttering when moving around with a lot of stained glass-like blocks around"
    })
    @Config.DefaultBoolean(true)
    public static boolean BSP_SORTING;

    @Config.Comment({
            "Dropped and held items are re-rendered every frame, generating a lot of",
            "unnecessary CPU load because of the very inefficient way the rendering is done.",
            "With this enabled, FalseTweaks will cache pre-rendered versions of items into RenderLists",
            "(same things that chunks use) to minimize the amount of work done by the cpu for every single item.",
            "FPS impact: Decent improvement with lots of items on ground"
    })
    @Config.DefaultBoolean(true)
    public static boolean ITEM_RENDER_LISTS;

    @Config.Comment("Disables the Realms button on the main menu.")
    @Config.DefaultBoolean(true)
    public static boolean NO_REALMS_ON_MENU;

    @Config.Comment({
            "Beacons also have an optimization using renderlists. If you spot any issues related to beacons,",
            "you can toggle said optimization here.",
            "FPS impact: Tiny improvement near beacons"
    })
    @Config.DefaultBoolean(true)
    public static boolean BEACON_OPTIMIZATION;

    @Config.Comment({
            "Transparent tile entities (beacons, for instance) might render behind other tile entities that are",
            "actually BEHIND the transparent part. Sorting the tile entities before rendering fixes this bug.",
            "FPS impact: Slight decrease"
    })
    @Config.DefaultBoolean(false)
    public static boolean TE_TRANSPARENCY_FIX;

    @Config.Comment({
            "Makes translucent geometry of chunks render slightly closer to the camera. (experimental)",
            "This reduces Z-Fighting on blocks which have overlapping opaque and translucent geometry,",
            "at the cost of far away geometry sometimes rendering on top of opaque geometry. (>100~ blocks)",
            "FPS impact: Unknown"
    })
    @Config.DefaultBoolean(false)
    public static boolean BLOCK_LAYER_TRANSPARENCY_FIX;

    @Config.Comment("Particle water transparency fix")
    @Config.DefaultBoolean(true)
    public static boolean PARTICLE_TRANSPARENCY_FIX;

    @Config.Comment({
            "3D particles (experimental)",
            "FPS impact: Unknown"
    })
    @Config.DefaultBoolean(false)
    public static boolean CUBIC_PARTICLES;

    @Config.Comment({
            "Improves the mipmap system of minecraft with 2 things:",
            "1. Every tiny texture is upscaled to at least 16x16 to allow for 4 mipmap levels, even if a mod has a texture smaller than 16x16.",
            "2. Replaces the mipmap generation with a multithreaded system, which scales with the number of cores in your system.",
            "FPS impact: none, but resource pack reload times (and startup time) are cut down by a lot"
    })
    @Config.DefaultBoolean(true)
    public static boolean MIPMAP_FIX;

    @Config.Comment({
            "Replaces the minecraft profiler with fully custom logic (used in the Shift+F3 pie chart)",
            "Also check the profiler config category!",
            "FPS impact: Slightly faster profiler"
    })
    @Config.DefaultBoolean(true)
    public static boolean ADVANCED_PROFILER;

    @Config.Comment({
            "Enables multi-threaded chunk updating.",
            "Not compatible with quad triangulation (automatically disables it if you turn this on)",
            "Force-enables BSP_SORTING.",
            "COMPATIBLE WITH OPTIFINE AND SHADERS",
            "FPS impact: Significant FPS and world rendering speed gains. Even higher with Neodymium installed."
    })
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean THREADED_CHUNK_UPDATES;

    @Config.Comment({
            "OptiFine-style dynamic lights, but works without OptiFine",
            "Force-enables CHUNK_CACHE.",
            "Implicitly enabled when OptiFine is installed for compatibility.",
            "See the dynamiclights config entry for more configs.",
    })
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean DYNAMIC_LIGHTS;

    @Config.Comment({
            "Replaces the renderer chunk cache with a more efficient version.",
            "FPS impact: Faster chunk rendering"
    })
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean FASTER_CHUNK_CACHE;

    @Config.Comment("Wraps block renderer code and tile entity renderer code in extra opengl state guards.")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean RENDERING_SAFETY;

    @Config.Comment("Gets rid of that obnoxious burst of minecart sounds when joining a world.")
    @Config.DefaultBoolean(true)
    public static boolean MINECART_EAR_BLAST_FIX;

    @Config.Comment({
            "Improves the performance of the minecraft sky mesh.",
            "Also fixes the weird white lines that some OptiFine shaderpacks get with huge render distances.",
            "FPS impact: Negligible gain"
    })
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean SKY_MESH_OPTIMIZATION;

    @Config.Comment({
            "Fixes an occasional crash that happens when trying to render a GUI block overlay (more common with optifine shaders enabled.)",
            "FPS impact: Zero"
    })
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OVERLAY_CRASH_FIX;

    @Config.Comment("Suppresses logspam coming from optifine's shader system. Makes shaderpack reloads faster.")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OPTIFINE_LOGSPAM_FIX;

    static {
        ConfigurationManager.selfInit();
        ProfilerConfig.init();
        RenderListConfig.init();
        TriangulatorConfig.init();
        VoxelizerConfig.init();
        TranslucentBlockLayersConfig.init();
        OcclusionConfig.init();
        ThreadingConfig.init();
        DynamicLightsConfig.init();
        RenderingSafetyConfig.init();
        OptiSpamConfig.init();
    }

    public static boolean TRIANGULATOR() {
        return TRIANGULATOR || BSP_SORTING();
    }

    public static boolean THREADED_CHUNK_UPDATES() {
        return THREADED_CHUNK_UPDATES;
    }

    public static boolean BSP_SORTING() {
        return BSP_SORTING || THREADED_CHUNK_UPDATES();
    }

    //This is here to make the static initializer run
    public static void init() {

    }

}
