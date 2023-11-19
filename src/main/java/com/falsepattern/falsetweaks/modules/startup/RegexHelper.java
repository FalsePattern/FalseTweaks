/*
 * This file is part of FalseTweaks.
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

package com.falsepattern.falsetweaks.modules.startup;

public class RegexHelper {
    public static boolean zipJarRegex(String toMatch) {
        return toMatch.endsWith(".jar") || toMatch.endsWith(".zip");
    }

    public static boolean classFileRegex(String toMatch) {
        return toMatch.endsWith(".class") && !toMatch.startsWith("$") && !toMatch.endsWith("$.class") &&
               !toMatch.equals("module-info.class");
    }

    public static boolean modClassRegex(String toMatch) {
        String shortName = toMatch.substring(toMatch.lastIndexOf('.') + 1);
        return shortName.startsWith("mod_") && !shortName.contains("$");
    }
}
