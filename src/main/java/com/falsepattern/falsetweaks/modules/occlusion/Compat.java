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

import shadersmod.client.Shaders;
import stubpackage.Config;
import stubpackage.net.minecraft.client.renderer.EntityRenderer;

import cpw.mods.fml.client.FMLClientHandler;

public class Compat {
    public static boolean isOptiFineFogOff(net.minecraft.client.renderer.EntityRenderer entityRenderer) {
        return FMLClientHandler.instance().hasOptifine() && OptiFineCompat.isFogOff((EntityRenderer) entityRenderer);
    }

    public static boolean isShadowPass() {
        return FMLClientHandler.instance().hasOptifine() && OptiFineCompat.isShadowPass();
    }

    private static class OptiFineCompat {
        static boolean isFogOff(EntityRenderer entityRenderer) {
            return Config.isFogOff() && entityRenderer.fogStandard;
        }

        static boolean isShadowPass() {
            return Shaders.isShadowPass;
        }
    }
}
