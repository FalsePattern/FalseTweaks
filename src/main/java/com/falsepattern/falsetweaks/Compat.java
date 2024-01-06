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

package com.falsepattern.falsetweaks;

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.github.basdxz.apparatus.defenition.managed.IParaBlock;
import lombok.Getter;
import makamys.neodymium.Neodymium;
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
    private static Boolean OPTIFINE = null;
    private static Boolean DYNLIGHTS = null;
    private static Boolean SHADERS = null;

    public static boolean neodymiumInstalled() {
        if (NEODYMIUM != null) {
            return NEODYMIUM;
        }
        try {
            NEODYMIUM = ((LaunchClassLoader) Compat.class.getClassLoader()).getClassBytes(
                    "makamys.neodymium.Neodymium") != null;
        } catch (IOException e) {
            e.printStackTrace();
            NEODYMIUM = false;
        }
        return NEODYMIUM;
    }

    public static boolean neodymiumActive() {
        return neodymiumInstalled() && Neodymium.isActive();
    }

    public static boolean optiFineInstalled() {
        if (OPTIFINE != null) {
            return OPTIFINE;
        }
        try {
            OPTIFINE = Launch.classLoader.getClassBytes("Config") != null;
        } catch (IOException e) {
            e.printStackTrace();
            OPTIFINE = false;
        }
        return OPTIFINE;
    }

    public static boolean optiFineHasDynamicLights() {
        if (!optiFineInstalled()) {
            return false;
        }
        if (DYNLIGHTS != null) {
            return DYNLIGHTS;
        }
        try {
            DYNLIGHTS = Launch.classLoader.getClassBytes("DynamicLights") != null;
        } catch (IOException e) {
            e.printStackTrace();
            DYNLIGHTS = false;
        }
        return DYNLIGHTS;
    }

    public static boolean optiFineHasShaders() {
        if (!optiFineInstalled()) {
            return false;
        }
        if (SHADERS != null) {
            return SHADERS;
        }
        try {
            SHADERS = Launch.classLoader.getClassBytes("shadersmod.client.Shaders") != null;
        } catch (IOException e) {
            e.printStackTrace();
            SHADERS = false;
        }
        return SHADERS;
    }

    public static void applyCompatibilityTweaks() {
        ThreadingCompat.init();
        if (Loader.isModLoaded("archaicfix")) {
            ArchaicFixCompat.init();
        }
        if (Loader.isModLoaded("apparatus")) {
            ApparatusCompat.init();
        }
    }

    public static boolean enableTriangulation() {
        //Threaded chunk updates mess up the triangulator, so keep it off for now until the root cause is found.
        return TriangulatorConfig.ENABLE_QUAD_TRIANGULATION && !ModuleConfig.THREADED_CHUNK_UPDATES && !neodymiumActive();
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
        return optiFineHasShaders() && Config.isShaders();
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
            try {
                isThreadedChunkUpdatingEnabled =  org.embeddedt.archaicfix.threadedupdates.api.ThreadedChunkUpdates.isEnabled();
            } catch (Throwable ignored) {
                isThreadedChunkUpdatingEnabled = false;
            }
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

    private static class NeodymiumCompat {
        public static boolean isActive() {
            return Neodymium.isActive();
        }
    }
}
