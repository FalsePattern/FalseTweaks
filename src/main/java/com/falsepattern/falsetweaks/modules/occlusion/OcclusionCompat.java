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

import Reika.DragonAPI.Extras.ChangePacketRenderer;
import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.util.ConfigFixUtil;
import lombok.val;
import org.embeddedt.archaicfix.config.ArchaicConfig;
import shadersmod.client.Shaders;
import stubpackage.Config;
import stubpackage.DynamicLights;
import stubpackage.net.minecraft.client.renderer.EntityRenderer;

import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.common.Loader;

import java.io.IOException;
import java.util.List;

public class OcclusionCompat {

    public static void executeConfigFixes() {
        FastCraftCompat.executeFastCraftConfigFixes();
        ArchaicFixCompat.executeArchaicFixConfigFixes();
    }

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
        public static void executeFastCraftConfigFixes() {
            ConfigFixUtil.fixConfig("fastcraft.ini", (line) -> {
                if (line.contains("asyncCulling") ||
                    line.contains("enableCullingTweaks"))
                    return line.replace("true", "false");
                if (line.contains("maxViewDistance"))
                    return line.replaceAll("\\d+", "32");
                return line;
            }, e -> Share.log.fatal("Failed to apply FastCraft occlusion tweak compatibility patches!", e));
        }
    }

    public static class ArchaicFixCompat {
        /**
         * See {@link FastCraftCompat#executeFastCraftConfigFixes()}
         */
        public static void executeArchaicFixConfigFixes() {
            ConfigFixUtil.fixConfig("archaicfix.cfg", line -> {
                if (line.contains("raiseMaxRenderDistance") ||
                    line.contains("enableOcclusionTweaks") ||
                    line.contains("enableThreadedChunkUpdates"))
                    return line.replaceAll("[tT][rR][uU][eE]", "false");
                return line;
            }, e -> Share.log.fatal("Failed to apply ArchaicFix occlusion tweak compatibility patches!", e));
        }

        /**
         * People keep repeating almost the exact same bug reports, so i'll just crash here with a descriptive crash report.
         */
        public static void crashIfUnsupportedConfigsAreActive() {
            try {
                if (Launch.classLoader.getClassBytes("org.embeddedt.archaicfix.config.ArchaicConfig") == null)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            boolean crash = false;
            val sb = new StringBuilder("\n");
            //try/catch with ignore so that field name changes don't cause uncontrollable crashes.
            try {
                if (ArchaicConfig.raiseMaxRenderDistance) {
                    crash = true;
                    sb.append(alarm("raiseMaxRenderDistance"));
                }
            } catch (Throwable ignored) {}
            try {
                if (ArchaicConfig.enableOcclusionTweaks) {
                    crash = true;
                    sb.append(alarm("enableOcclusionTweaks"));
                }
            } catch (Throwable ignored) {}
            try {
                if (ArchaicConfig.enableThreadedChunkUpdates) {
                    crash = true;
                    sb.append(alarm("enableThreadedChunkUpdates"));
                }
            } catch (Throwable ignored) {}
            if (crash) {
                val exception = new IllegalStateException(sb.toString());
                exception.setStackTrace(new StackTraceElement[0]);
                throw exception;
            }

        }

        private static String alarm(String configProperty) {
            return "ArchaicFix's " + configProperty + " config is not compatible with FalseTweaks' occlusion culling. Please set it to false in archaicfix.cfg.\n";
        }
    }

    public static class OptiFineCompat {

        public static boolean isOptiFineFogOff(net.minecraft.client.renderer.EntityRenderer entityRenderer) {
            return Compat.optiFineInstalled() && Config.isFogOff() && ((EntityRenderer)entityRenderer).fogStandard;
        }

        public static boolean isShadowPass() {
            return Compat.optiFineHasShaders() && Shaders.isShadowPass;
        }

        public static boolean disableControl(List addToList, Object control) {
            if (!(control instanceof GuiOptionButton))
                return false;
            val button = (GuiOptionButton) control;
            button.enabled = false;
            return addToList.add(button);
        }

        public static void updateDynamicLights(RenderGlobal rg) {
            if (!Compat.optiFineHasDynamicLights())
                return;
            if (!Config.isDynamicLights())
                return;
            DynamicLights.update(rg);
        }
    }

    public static class DragonAPICompat {
        private static final boolean DRAGONAPI_PRESENT = Loader.isModLoaded("DragonAPI");

        public static void ChangePacketRenderer$onChunkRerender(int mx, int my, int mz, int px, int py, int pz, WorldRenderer r) {
            if (!DRAGONAPI_PRESENT)
                return;
            try {
                ChangePacketRenderer.onChunkRerender(mx, my, mz, px, py, pz, r);
            } catch (Exception e) {
                val warning = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    warning.append("THIS IS NOT A DRAGONAPI BUG! CONTACT FALSEPATTERN FIRST, THIS IS MOST LIKELY A FALSETWEAKS BUG!!!\n");
                }
                throw new RuntimeException(warning.toString(), e);
            }
        }
    }
}
