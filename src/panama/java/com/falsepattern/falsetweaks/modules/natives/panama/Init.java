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

package com.falsepattern.falsetweaks.modules.natives.panama;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.natives.CPUID;
import com.falsepattern.falsetweaks.modules.natives.NativeLoader;
import com.falsepattern.falsetweaks.modules.natives.UnsupportedPlatformException;
import com.falsepattern.falsetweaks.modules.natives.camera.Clipping;
import com.falsepattern.zanama.NativeContext;
import com.ventooth.vnativeloader.api.UnsatisfiedLinkException;
import lombok.val;

import java.io.IOException;
import java.lang.foreign.Arena;

import static com.falsepattern.falsetweaks.Share.log;

public class Init {
    private static NativeContext CTX;
    public static void load() throws UnsupportedPlatformException {
        log.info("Initializing natives (panama)");
        val loader = new NativeLoader(CPUID.class);
        log.info("Initializing panama context");
        CTX = NativeContext.create(loader.nativesDir);
        log.info("Unpacking CPUID natives");
        val libCPUID = loader.unpackNative("cpuid", "x86_64");
        log.info("Loading CPUID natives");
        try {
            CpuID_z_init.lib = CTX.load(CpuID_z_init.class, libCPUID);
        } catch (UnsatisfiedLinkException | IOException e) {
            throw new UnsupportedPlatformException(e);
        }
        log.info("Fetching CPU arch");
        String arch;
        try (val arena = Arena.ofConfined()) {
            val buf = arena.allocate(CpuID.maxNameLength());
            CpuID.getCpuModel(buf);
            arch = buf.getString(0);
        }
        log.info("CPU arch: {}", arch);
        if (ModuleConfig.CLIPPING_HELPER_OPTS) {
            log.info("Unpacking FalseTweaks natives");
            val libFT = loader.unpackNative("FalseTweaks", arch);
            log.info("Loading FalseTweaks natives");
            try {
                FalseTweaks_z_init.lib = CTX.load(FalseTweaks_z_init.class, libFT);
            } catch (UnsatisfiedLinkException | IOException e) {
                throw new UnsupportedPlatformException(e);
            }
        }
        log.info("Natives initialized!");
    }
}
