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

import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import com.gtnewhorizon.gtnhlib.api.CapturingTesselator;
import lombok.val;

import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.common.Loader;

public class ThreadTessellator {
    private static final ThreadLocal<Tessellator> TESS = ThreadLocal.withInitial(Tessellator::new);
    private static final boolean GTNHLIB_PRESENT = Loader.isModLoaded("gtnhlib");

    public static Tessellator getThreadTessellator() {
        if (GTNHLIB_PRESENT) {
            return GTNHLibInterop.getThreadTessellator();
        } else {
            return getThreadTessellatorRaw();
        }
    }

    private static Tessellator getThreadTessellatorRaw() {
        if (MainThreadContainer.isMainThread()) {
            return mainThreadTessellator();
        } else {
            return TESS.get();
        }
    }

    public static Tessellator mainThreadTessellator() {
        return Tessellator.instance;
    }

    public static Tessellator swapMainTessellator(Tessellator tess) {
        val old = Tessellator.instance;
        Tessellator.instance = tess;
        return old;
    }

    private static class GTNHLibInterop {
        public static Tessellator getThreadTessellator() {
            if (CapturingTesselator.isCapturing()) {
                return CapturingTesselator.getThreadTesselator();
            }
            return getThreadTessellatorRaw();
        }
    }
}
