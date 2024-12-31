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

@Config.Comment("Options for the translucent block layer fixes")
@Config(modid = Tags.MOD_ID,
        category = "translucent_block_layers_fix")
@Config.LangKey
public class TranslucentBlockLayersConfig {
    @Config.Comment({
            "The \"epsilon\" value used when shifting translucent block geometry closer to the camera.",
            "Values too low will cause near blocks to suffer from Z-Fighting,",
            "values too high will cause translucent geometry to leak through far away blocks.",
            "Advanced setting.",
            "FPS impact: Unknown"
    })
    @Config.Name(value = "epsilon", migrations = "")
    @Config.LangKey
    @Config.RangeDouble(min = 0,
                        max = 0.1)
    @Config.DefaultDouble(0.001)
    public static double TRANSLUCENT_BLOCK_LAYERS_FIX_EPSILON;

    @Config.Comment({
            "Runtime toggle for enabling the matrix transforms.",
            "This does not affect the mixin loading, use the toggle in the modules section for that."
    })
    @Config.Name(value = "enabled")
    @Config.LangKey
    @Config.DefaultBoolean(true)
    public static boolean ENABLED;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
