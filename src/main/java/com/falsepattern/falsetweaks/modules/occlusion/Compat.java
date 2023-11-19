/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, and this permission notice shall be included in all copies
 * or substantial portions of the Software.
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

package com.falsepattern.falsetweaks.modules.occlusion;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.lib.util.FileUtil;
import lombok.val;
import shadersmod.client.Shaders;
import stubpackage.Config;
import stubpackage.net.minecraft.client.renderer.EntityRenderer;

import cpw.mods.fml.client.FMLClientHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class Compat {
    public static class FastCraftCompat {
        /**
         * This is here so that people won't cry about "waaaah this thing doesn't work with fastcraft waaaah" and then
         * disable my mod's feature instead of disabling the fastcraft one (the fastcraft one is slower but peer pressure
         * makes people think old = good, new = bad).
         * <p>
         * I'm doing it this way because I was lazy to write a bunch of mixins when i can just do some file magic and get the
         * same result anyway.
         * <p>
         * "apex predator of grug is complexity"
         */
        public static void executeFastCraftCompatibilityHacks() {
            val targetPath = FileUtil.getMinecraftHomePath().resolve("config").resolve("fastcraft.ini");
            if (!Files.exists(targetPath))
                return;
            try {
                val fileText = Files.readAllLines(targetPath);
                val result = fileText.stream().map(line -> {
                    if (line.contains("asyncCulling") || line.contains("enableCullingTweaks"))
                        return line.replace("true", "false");
                    return line;
                }).collect(Collectors.toList());
                Files.write(targetPath, result);
            } catch (IOException e) {
                Share.log.fatal("Failed to apply FastCraft occlusion tweak compatibility patches!", e);
            }
        }
    }

    public static class OptiFineCompat {
        private static final boolean HAS_OPTIFINE = FMLClientHandler.instance().hasOptifine();

        public static boolean isOptiFineFogOff(net.minecraft.client.renderer.EntityRenderer entityRenderer) {
            return HAS_OPTIFINE && Config.isFogOff() && ((EntityRenderer)entityRenderer).fogStandard;
        }

        public static boolean isShadowPass() {
            return HAS_OPTIFINE && Shaders.isShadowPass;
        }
    }
}
