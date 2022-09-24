/*
 * Triangulator
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
import com.falsepattern.lib.config.Config;

@Config(modid = Tags.MODID,
        category = "modules")
@Config.RequiresMcRestart
public class ModuleConfig {
    @Config.Comment("Optimizes the way forge scans the classpath during launch.\n" +
                    "Not compatible with some badly-written mods.\n" +
                    "FPS impact: None, but makes startup a bit faster")
    @Config.LangKey("config.falsetweaks.module.startup_optimizations")
    @Config.DefaultBoolean(true)
    public static boolean STARTUP_OPTIMIZATIONS;

    @Config.Comment("Enable/Disable texture optimizations. This includes:\n" +
                    "- Multithreaded animated textures\n" +
                    "- Faster texture atlas packing during startup\n" +
                    "FPS impact: Reduced stuttering in heavily modded packs")
    @Config.LangKey("config.falsetweaks.module.texture_optimizations")
    @Config.DefaultBoolean(true)
    public static boolean TEXTURE_OPTIMIZATIONS;

    @Config.Comment("Enable/Disable item voxelization. This fixes a huge amount of item render issues, but is still\n" +
                    "an experimental feature.\n" +
                    "FPS impact: Very minor, breaking drawers still freezes the game")
    @Config.LangKey("config.falsetweaks.module.item_voxelizer")
    @Config.DefaultBoolean(true)
    public static boolean ITEM_VOXELIZER;
}
