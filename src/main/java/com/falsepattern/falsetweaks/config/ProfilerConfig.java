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

@Config.Comment("Improved Shift+F3 profiler")
@Config(modid = Tags.MOD_ID,
        category = "profiler")
@Config.LangKey
public class ProfilerConfig {
    @Config.Comment("Enable this to dump the profiler data to a file when the F3 profiler is closed.")
    @Config.LangKey
    @Config.Name(value = "dumpOnClose", migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean DUMP_ON_CLOSE;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
