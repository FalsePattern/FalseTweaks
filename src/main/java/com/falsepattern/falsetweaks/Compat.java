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

package com.falsepattern.falsetweaks;

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadTessellator;
import com.github.basdxz.apparatus.defenition.managed.IParaBlock;
import lombok.Getter;
import stubpackage.Config;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.common.Loader;

import java.io.IOException;

public class Compat {
    public static boolean swanSongInstalled() {
        return SWANSONG.PRESENT;
    }

    public static boolean optiFineInstalled() {
        return OPTIFINE.PRESENT;
    }

    public static boolean dynamicLightsPresent() {
        return ModuleConfig.DYNAMIC_LIGHTS || optiFineHasDynamicLights();
    }

    public static boolean optiFineHasDynamicLights() {
        return OPTIFINE.PRESENT && DYNLIGHTS.PRESENT;
    }

    public static boolean optiFineHasShaders() {
        return OPTIFINE.PRESENT && SHADERS.PRESENT;
    }

    public static boolean beddiumInstalled() {
        return BEDDIUM.PRESENT;
    }

    public static void applyCompatibilityTweaks() {
        ThreadingCompat.init();
        if (Loader.isModLoaded("apparatus")) {
            ApparatusCompat.init();
        }
    }

    public static boolean enableTriangulation() {
        // TODO add triangulator compat in beddium
        return TriangulatorConfig.ENABLE_QUAD_TRIANGULATION && !beddiumInstalled();
    }

    public static Tessellator tessellator() {
        if (ThreadingCompat.isThreadedChunkUpdatingEnabled()) {
            return ThreadTessellator.getThreadTessellator();
        }
        return ThreadTessellator.mainThreadTessellator();
    }

    public static ShaderType shaderType() {
        return SWANSONG.PRESENT ? ShaderType.Swansong
                                : optiFineHasShaders() && OptifineCompat.isShaders() ? ShaderType.Optifine
                                                                                     : ShaderType.None;
    }

    public static boolean isSTBIStitcher() {
        try {
            return LWJGL3IFY.PRESENT && LWJGL3IfyCompat.stbiTextureStitching();
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static float getAmbientOcclusionLightValue(Block block, int x, int y, int z, IBlockAccess blockAccess) {
        if (ApparatusCompat.isApparatusPresent()) {
            return ApparatusCompat.getAmbientOcclusionLightValue(block, x, y, z, blockAccess);
        } else {
            return block.getAmbientOcclusionLightValue();
        }
    }

    public enum ShaderType {
        None,
        Optifine,
        Swansong
    }

    private static class OPTIFINE {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("Config") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
        }
    }

    private static class DYNLIGHTS {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("DynamicLights") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
        }
    }

    private static class SHADERS {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("shadersmod.client.Shaders") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
        }
    }

    private static class LWJGL3IFY {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("me.eigenraven.lwjgl3ify.core.Lwjgl3ifyCoremod") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
        }
    }

    private static class BEDDIUM {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("com.ventooth.beddium.Share") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
        }
    }

    private static class SWANSONG {
        private static final boolean PRESENT;

        static {
            boolean present;
            try {
                present = Launch.classLoader.getClassBytes("com.ventooth.swansong.Share") != null;
            } catch (IOException e) {
                e.printStackTrace();
                present = false;
            }
            PRESENT = present;
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

            return ((IParaBlock) block).paraTile(blockAccess, x, y, z)
                                       .getAmbientOcclusionLightValue();
        }
    }

    private static class ThreadingCompat {

        @Getter
        private static boolean isThreadedChunkUpdatingEnabled;

        private static void init() {
            isThreadedChunkUpdatingEnabled = ThreadedChunkUpdates.isEnabled();
        }
    }

    private static class LWJGL3IfyCompat {
        public static boolean stbiTextureStitching() {
            return me.eigenraven.lwjgl3ify.core.Config.MIXIN_STBI_TEXTURE_STITCHING;
        }
    }

    private static class OptifineCompat {
        public static boolean isShaders() {
            return Config.isShaders();
        }
    }
}
