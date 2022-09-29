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

package com.falsepattern.falsetweaks.modules.startup;

public class RegexHelper {
    public static boolean zipJarRegex(String toMatch) {
        return toMatch.endsWith(".jar") || toMatch.endsWith(".zip");
    }

    public static boolean classFileRegex(String toMatch) {
        return toMatch.endsWith(".class") && !toMatch.startsWith("$") && !toMatch.endsWith("$.class");
    }

    public static boolean modClassRegex(String toMatch) {
        String shortName = toMatch.substring(toMatch.lastIndexOf('.') + 1);
        return shortName.startsWith("mod_") && !shortName.contains("$");
    }
}
