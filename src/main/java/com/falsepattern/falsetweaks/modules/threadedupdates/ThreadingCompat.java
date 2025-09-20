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

package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.util.ConfigFixUtil;

public class ThreadingCompat {

    public static void executeConfigFixes() {
        FastCraftCompat.executeFastCraftConfigFixes();
    }

    public static class FastCraftCompat {
        public static void executeFastCraftConfigFixes() {
            ConfigFixUtil.fixConfig("fastcraft.ini", (line) -> {
                if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
                    if (line.contains("asyncCulling") || line.contains("enableCullingTweaks")) {
                        return line.replace("true", "false");
                    }
                }
                return line;
            }, e -> Share.log.fatal("Failed to apply FastCraft occlusion tweak compatibility patches!", e));
        }
    }
}
