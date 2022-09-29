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
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MODID,
        category = "memory_leak_fix")
public class LeakFixConfig {
    @Config.Comment("The memory leak optimization unfortunately increases the amount of calls sent to the GPU.\n" +
                    "This pressure can be reduced with the help of caching, which temporarily stores inactive renderlists\n" +
                    "in a buffer, where renderers can then fetch them from when needed.\n" +
                    "You can set this to any value above zero, but setting it too high will eat a LOT of VRAM. 1024 is\n" +
                    "a decent safe point.\n" +
                    "FPS impact: zero when tuned right")
    @Config.RangeInt(min = 0)
    @Config.LangKey("config.falsetweaks.leakfix.cache_size_target")
    @Config.DefaultInt(1024)
    public static int CACHE_SIZE_TARGET;

    static {
        ConfigurationManager.selfInit();
    }
}
