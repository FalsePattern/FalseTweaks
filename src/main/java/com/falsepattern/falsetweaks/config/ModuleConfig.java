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
import lombok.SneakyThrows;

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
                    "which gets emphasized on modern AMD windows drivers, and on MESA with linux. This patch\n" +
                    "Replaces the preallocated block by a dynamic allocation system, with each chunk creating and\n" +
                    "deleting these renderlists based on usage. Requires a game restart to apply.\n" +
                    "NOTICE FOR OPTIFINE USERS:\n" +
                    "Setting this to Auto or Enable blocks Smooth and Multi-Core chunkloading. If you want multicore\n" +
                    "chunk loading, you MUST set this do Disable.\n" +
                    "NOTICE FOR NEODYMIUM USERS:\n" +
                    "If you use Neodymium, this module will automatically disable itself when set to Auto. Set it to\n" +
                    "Enable to bypass.\n" +
                    "FPS impact: It depends")
    @Config.DefaultEnum("Auto")
    public static LeakFixState MEMORY_LEAK_FIX;

    @Config.Comment("Transparent tile entities (beacons, for instance) might render behind other tile entities that are\n" +
                    "actually BEHIND the transparent part. Sorting the tile entities before rendering fixes this bug.\n" +
                    "FPS impact: Slight decrease")
    @Config.DefaultBoolean(false)
    public static boolean TE_TRANSPARENCY_FIX;


    @Config.Comment("Forces all textures to be a power of two, and at least 16x16 in side.\n" +
                    "This fixes mipmaps, but might result in some weird textures when a texture was NOT a power of two.\n" +
                    "FPS impact: none")
    @Config.DefaultBoolean(true)
    public static boolean MIPMAP_FIX;

    static {
        ConfigurationManager.selfInit();
        //noinspection deprecation
        init(DeprecatedConfig.class);
        if (MEMORY_LEAK_FIX != LeakFixState.Disable)
            init(LeakFixConfig.class);
        if (ITEM_RENDER_LISTS)
            init(RenderListConfig.class);
        if (TRIANGULATOR)
            init(TriangulatorConfig.class);
        if (VOXELIZER)
            init(VoxelizerConfig.class);
    }

    @SneakyThrows
    private static void init(Class<?> clazz) {
        Class.forName(clazz.getName(), true, clazz.getClassLoader());
    }

}
