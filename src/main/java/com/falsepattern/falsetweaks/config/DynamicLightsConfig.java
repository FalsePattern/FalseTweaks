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

@Config.Comment("Dynamic hand and entity lights")
@Config(modid = Tags.MOD_ID,
        category = "dynamic_lights",
        categoryMigrations = "dynamiclights")
@Config.LangKey
public class DynamicLightsConfig {

    @Config.Comment("Enable/disable dynamic lights without restarting the game")
    @Config.LangKey
    @Config.Name(value = "state", migrations = "")
    @Config.DefaultEnum("Fast")
    public static DynamicLightsState STATE;

    @Config.Comment("Should items/blocks held by the player emit light?")
    @Config.LangKey
    @Config.Name(value = "handLight", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean DYNAMIC_HAND_LIGHT;

    @Config.Comment("Set this to false if you want to make dynamic lights diamond-shaped, like block lights.")
    @Config.LangKey
    @Config.Name(value = "circular", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean CIRCULAR;

    static {
        ConfigurationManager.selfInit();
    }
    //This is here to make the static initializer run
    public static void init() {

    }

    public enum DynamicLightsState {
        Fast,
        Fancy,
        Disabled
    }
}
