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

package com.falsepattern.falsetweaks;

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.github.basdxz.apparatus.defenition.managed.IParaBlock;
import lombok.Getter;
import stubpackage.Config;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;

import java.io.IOException;

public class Compat {
    private static Boolean NEODYMIUM = null;

    public static boolean neodymiumInstalled() {
        if (NEODYMIUM == null) {
            try {
                NEODYMIUM = ((LaunchClassLoader) Compat.class.getClassLoader()).getClassBytes(
                        "makamys.neodymium.Neodymium") != null;
            } catch (IOException e) {
                e.printStackTrace();
                NEODYMIUM = false;
            }
            if (NEODYMIUM) {
                Share.log.warn("Neodymium detected! Incompatible modules will be disabled.");
                Share.log.warn("Incompatible modules:");
                Share.log.warn(
                        "Leak Fix (Change from Auto for Enable to bypass the safety check and enable it anyways)");
                Share.log.warn("Quad Triangulation");
            }
        }
        return NEODYMIUM;
    }

    public static void applyCompatibilityTweaks() {
        ThreadingCompat.init();
        if (Loader.isModLoaded("archaicfix")) {
            ArchaicFixCompat.init();
        }
        if (Loader.isModLoaded("apparatus")) {
            ApparatusCompat.init();
        }
        if (FMLClientHandler.instance().hasOptifine()) {
            OptiFineCompat.init();
        }
    }

    public static boolean enableTriangulation() {
        //Threaded chunk updates mess up the triangulator, so keep it off for now until the root cause is found.
        return TriangulatorConfig.ENABLE_QUAD_TRIANGULATION && !ModuleConfig.THREADED_CHUNK_UPDATES && !neodymiumInstalled();
    }

    public static Tessellator tessellator() {
        if (ThreadingCompat.isThreadedChunkUpdatingEnabled()) {
            return ThreadedChunkUpdates.getThreadTessellator();
        }
        if (ArchaicFixCompat.isThreadedChunkUpdatingEnabled()) {
            return ArchaicFixCompat.threadTessellator();
        }
        return Tessellator.instance;
    }

    public static boolean isShaders() {
        if (OptiFineCompat.isShadersModPresent()) {
            return OptiFineCompat.isShaders();
        }
        return false;
    }

    public static float getAmbientOcclusionLightValue(Block block, int x, int y, int z, IBlockAccess blockAccess) {
        if (ApparatusCompat.isApparatusPresent()) {
            return ApparatusCompat.getAmbientOcclusionLightValue(block, x, y, z, blockAccess);
        } else {
            return block.getAmbientOcclusionLightValue();
        }
    }

    private static class ApparatusCompat {
        @Getter
        private static boolean apparatusPresent = false;

        private static void init() {
            apparatusPresent = true;
        }

        public static float getAmbientOcclusionLightValue(Block block, int x, int y, int z, IBlockAccess blockAccess) {
            if (!(block instanceof IParaBlock)) {
                return block.getAmbientOcclusionLightValue();
            }

            return ((IParaBlock) block).paraTile(blockAccess, x, y, z).getAmbientOcclusionLightValue();
        }
    }

    private static class ArchaicFixCompat {

        @Getter
        private static boolean isThreadedChunkUpdatingEnabled;

        private static void init() {
            isThreadedChunkUpdatingEnabled =  org.embeddedt.archaicfix.threadedupdates.api.ThreadedChunkUpdates.isEnabled();
        }

        public static Tessellator threadTessellator() {
            return org.embeddedt.archaicfix.threadedupdates.api.ThreadedChunkUpdates.getThreadTessellator();
        }
    }

    private static class ThreadingCompat {

        @Getter
        private static boolean isThreadedChunkUpdatingEnabled;

        private static void init() {
            isThreadedChunkUpdatingEnabled = ThreadedChunkUpdates.isEnabled();
        }

        public static Tessellator threadTessellator() {
            return ThreadedChunkUpdates.getThreadTessellator();
        }
    }

    private static class OptiFineCompat {
        @Getter
        private static boolean isShadersModPresent = false;
        @Getter
        private static boolean isOptiFinePresent = false;

        private static void init() {
            isOptiFinePresent = true;
            try {
                isShadersModPresent = Launch.classLoader.getClassBytes("shadersmod.client.Shaders") != null;
            } catch (IOException ignored) {
            }
        }

        public static boolean isShaders() {
            return Config.isShaders();
        }
    }
}
