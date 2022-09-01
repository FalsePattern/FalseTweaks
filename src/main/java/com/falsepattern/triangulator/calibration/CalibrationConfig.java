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

package com.falsepattern.triangulator.calibration;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.triangulator.Tags;

@Config(modid = Tags.MODID,
        category = "calibration")
public class CalibrationConfig {
    static {
        ConfigurationManager.selfInit();
    }
    @Config.Comment("Modifies the way ambient occlusion alignment is calculated. Used for compatibility purposes,\n" +
                    "because different graphics cards have different ways of processing quads.\n" +
                    "This is useful when quad triangulation is disabled, or if the triangulator gets disabled internally\n" +
                    "for compatibility reasons.")
    @Config.DefaultBoolean(false)
    public static boolean FLIP_DIAGONALS;

    @Config.Comment("The SHA256 hash of the graphics card that this calibration was configured for.")
    @Config.DefaultString("undefined")
    public static String GPU_HASH;
}
