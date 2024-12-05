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

@Config.Comment("OpenGL state leak and crash protections")
@Config(modid = Tags.MOD_ID,
        category = "rendering_safety")
@Config.LangKey
public class RenderingSafetyConfig {
    @Config.Comment("Enable safety wrapper for inventory blocks.")
    @Config.LangKey
    @Config.Name(value = "block", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_BLOCK;

    @Config.Comment("Enable safety wrapper for tile entities.")
    @Config.LangKey
    @Config.Name(value = "tesr", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_TESR;

    @Config.Comment("Enable safety wrapper for items.")
    @Config.LangKey
    @Config.Name(value = "item", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_ITEM;

    static {
        ConfigurationManager.selfInit();
    }

    public static void init() {

    }
}
