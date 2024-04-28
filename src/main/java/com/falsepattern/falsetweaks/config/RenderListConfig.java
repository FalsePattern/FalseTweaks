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

@Config(modid = Tags.MODID,
        category = "item_render_lists")
public class RenderListConfig {
    @Config.Comment("The total amount of renderlists FalseTweaks may allocate for optimized item rendering.\n" +
                    "When the limit is exceeded, the render list that was used the longest time ago gets released.\n" +
                    "Bigger buffer sizes use more VRAM, but also get a higher average performance.\n" +
                    "256 should be good enough for most modded games, and going above 1024 is not recommended unless\n" +
                    "you have a lot of VRAM.\n" + "(Only useful if you have ITEM_RENDERLISTS turned on)\n" +
                    "FPS impact: zero when tuned right")
    @Config.RangeInt(min = 64)
    @Config.LangKey("config.falsetweaks.item_render_lists.max_buffer_size")
    @Config.DefaultInt(256)
    public static int MAX_BUFFER_SIZE;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
