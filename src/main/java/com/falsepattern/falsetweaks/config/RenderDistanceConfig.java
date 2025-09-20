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

@Config.Comment("Render distance tweaks")
@Config(modid = Tags.MOD_ID,
        category = "render_distance")
@Config.LangKey
public class RenderDistanceConfig {
    @Config.Comment({"Changes the maximum render distance.",
                     "No effect if FastCraft is installed, use its config if it's installed!",
                     "NOTE: things might get extremely laggy above 32 without serverside performance mods!"})
    @Config.LangKey
    @Config.Name(value = "renderDistance",
                 migrations = "")
    @Config.RangeInt(min = 16,
                     max = MaximumRenderDistance)
    @Config.DefaultInt(32)
    public static int RENDER_DISTANCE;


    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
