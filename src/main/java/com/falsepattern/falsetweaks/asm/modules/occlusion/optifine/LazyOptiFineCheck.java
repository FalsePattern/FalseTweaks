/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

package com.falsepattern.falsetweaks.asm.modules.occlusion.optifine;

import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

public class LazyOptiFineCheck {
    private static Boolean optifineDetected = null;

    public static boolean hasOptiFine() {
        Boolean detected = optifineDetected;
        if (detected == null) {
            if (FMLLaunchHandler.side().isClient()) {
                try {
                    //We might be too early but let's try the standard way
                    detected = FMLClientHandler.instance().hasOptifine();
                } catch (Throwable ignored) {
                    //Ok, we'll do it manually then
                    try {
                        ClassLoader cl;
                        cl = Loader.instance().getModClassLoader();
                        if (cl == null) {
                            cl = Launch.classLoader;
                        }
                        Class.forName("Config", false, cl);
                        detected = true;
                    } catch (Throwable ignored1) {
                        //99.9% sure that optifine is not present
                        detected = false;
                    }
                }
            } else {
                //server shouldn't have OF
                detected = false;
            }
            optifineDetected = detected;
        }
        return detected;
    }
}
