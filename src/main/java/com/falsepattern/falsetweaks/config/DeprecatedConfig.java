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

@Config(modid = Tags.MODID)
@Deprecated
public class DeprecatedConfig {
    @Config.Comment("This category is deprecated. If you used FalseTweaks in previous alpha versions," +
                    "delete this file and let it regenerate. This category will be removed from the code once the mod" +
                    "leaves alpha.\n" +
                    "(changing this config value does nothing, this is just a notification)")
    @Config.DefaultBoolean(true)
    public static boolean AAAA_DEPRECATED_AAAA;

    static {
        ConfigurationManager.selfInit();
    }
}
