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

@Config.Comment("")
@Config(modid = Tags.MOD_ID,
        category = "ao_fix")
@Config.LangKey
public class AOFixConfig {
    @Config.Comment({"Try setting this to true if the game crashes with a mixin conflict inside RenderBlocks."})
    @Config.LangKey
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean renderHookCompatMode;

    @Config.Comment({"Disable this if some modded blocks have weird smooth lighting."})
    @Config.LangKey
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean universalPatch;

    @Config.Comment("Modifies the LittleTiles renderer to use the FalseTweaks AO system")
    @Config.LangKey
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean patchLittleTiles;

    @Config.Comment({
            "Stairs and slabs need additional AO workarounds to look good.",
            "You can toggle these fixes here (requires f3+a world renderer refresh to see the changes)"
    })
    @Config.LangKey
    @Config.DefaultBoolean(true)
    public static boolean stairAOFix;

    static {
        ConfigurationManager.selfInit();
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
