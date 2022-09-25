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
import com.falsepattern.lib.config.ConfigurationManager;

@Config(modid = Tags.MODID,
        category = "texture_overlays")
public class OverlayConfig {
    @Config.Comment("You can use this property to fix any incorrectly detected overlays.\n" +
                    "Syntax: texture_name=layer, where layer is the multiplier.\n" +
                    "The default behaviour is that if the texture name ends with _overlay, its layer is set to 1,\n" +
                    "This can be used to override that.\n" +
                    "For reference: layer 0 is regular rendering, layer 1 is on top layer 0, layer 2 is on top of layer 1, etc.\n" +
                    "Also supports negatives, but going below -1 is undefined behaviour. (-1 is used for the liquid inside potions by default)")
    @Config.DefaultStringList({"potion_overlay=-1"})
    @Config.RequiresMcRestart
    public static String[] forcedLayers;

    static {
        ConfigurationManager.selfInit();
    }
}
