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

package com.falsepattern.falsetweaks.modules.natives;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.natives.occlusion.Clipping;
import lombok.Getter;
import lombok.val;

import static com.falsepattern.falsetweaks.Share.log;

public class Natives {
    @Getter
    private static boolean isLoaded = false;

    public static void load() throws UnsupportedPlatformException {
        log.info("Initializing natives");
        val loader = new NativeLoader(CPUID.class);
        log.info("Loading JNI stubs");
        loader.loadNative("jni");
        log.info("Loading CPUID natives");
        val libCPUID = loader.loadNative("cpuid");
        log.info("Linking CPUID natives");
        CPUID.link(libCPUID);
        log.info("Fetching CPU arch");
        val arch = CPUID.getX86Version();
        log.info("CPU arch: {}", arch);
        String libFT = null;
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            log.info("Loading FalseTweaks natives");
            libFT = loader.loadNative("FalseTweaks-" + arch);
        }
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            log.info("Linking native clipping helper");
            if (!Clipping.link(libFT)) {
                throw new UnsupportedPlatformException("Failed to initialize native clipping helper");
            }
        }
        log.info("Natives initialized!");
        isLoaded = true;
    }
}
