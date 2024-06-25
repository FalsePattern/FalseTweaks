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
package com.falsepattern.falsetweaks.modules.animfix;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.util.ConfigFixUtil;

public class AnimFixCompat {
    public static void executeConfigFixes() {
        HodgePodgeCompat.executeHodgepodgeConfigFixes();
        Lwjgl3IfyCompat.execute3ifyConfigFixes();
    }

    public static class Lwjgl3IfyCompat {
        public static void execute3ifyConfigFixes() {
            ConfigFixUtil.fixConfig("lwjgl3ify.cfg", line -> {
                if (line.contains("stbiTextureStiching")) {
                    return line.replace("true", "false");
                }
                return line;
            }, e -> Share.log.fatal("Failed to apply LWJGL3Ify texture optimization compatibility patches!"));
        }
    }

    public static class HodgePodgeCompat {
        public static void executeHodgepodgeConfigFixes() {
            ConfigFixUtil.fixConfig("hodgepodge.cfg", (line) -> {
                if (line.contains("optimizeTextureLoading")) {
                    return line.replace("true", "false");
                }
                return line;
            }, (e) -> Share.log.fatal("Failed to apply HodgePodge texture optimization compatibility patches!", e));
        }
    }
}
