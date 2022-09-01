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

package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;

public class TriCompat {
    private static Boolean NEODYMIUM = null;

    public static boolean neodymiumInstalled() {
        if (NEODYMIUM == null) {
            try {
                NEODYMIUM = ((LaunchClassLoader)TriCompat.class.getClassLoader()).getClassBytes("makamys.neodymium.Neodymium") != null;
            } catch (IOException e) {
                e.printStackTrace();
                NEODYMIUM = false;
            }
        }
        return NEODYMIUM;
    }
    public static boolean enableTriangulation() {
        return TriConfig.ENABLE_QUAD_TRIANGULATION && !neodymiumInstalled();
    }
}
