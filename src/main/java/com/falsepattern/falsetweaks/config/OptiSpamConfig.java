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

@Config.Comment("Options to reduce the amount of logspam by OptiFine")
@Config(modid = Tags.MOD_ID,
        category = "optifine_log_spam_fixes")
@Config.LangKey
public class OptiSpamConfig {
    @Config.Comment("Suppresses \"Ambiguous shader option: ...\" warnings.")
    @Config.LangKey
    @Config.Name(value = "ambiguousShaderOption", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean AMBIGUOUS_SHADER_OPTION;

    @Config.Comment("Suppresses \"Block not found for name: ...\" warnings.")
    @Config.LangKey
    @Config.Name(value = "blockNotFound", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean BLOCK_NOT_FOUND;

    @Config.Comment("Suppresses \"Invalid block metadata: ...\" and \"Invalid block ID mapping: ...\" warnings.")
    @Config.LangKey
    @Config.Name(value = "invalidId", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean INVALID_ID;

    @Config.Comment("Suppresses \"Expression already defined: ...\" and \"Custom uniform/variable: ...\" logs.")
    @Config.LangKey
    @Config.Name(value = "customUniforms", migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean CUSTOM_UNIFORMS;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
