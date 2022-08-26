/*
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

package com.falsepattern.animfix.config;

import com.falsepattern.animfix.Tags;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Config(modid = Tags.MODID)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnimConfig {
    static {
        ConfigurationManager.selfInit();
    }
    @Config.Comment("The largest width and height animated textures can have to get put into the buffer.\n" +
                    "Making this higher will batch higher resolution textures too, but will consume more RAM.")
    @Config.RangeInt(min = 16,
                     max = 1024)
    @Config.DefaultInt(32)
    public static int maximumBatchedTextureSize;

    @Config.Comment("Spends a little more time optimizing the batch during loading so that it uses less resources during gameplay.")
    @Config.DefaultBoolean(true)
    public static boolean optimalPacking;
}