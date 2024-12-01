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
