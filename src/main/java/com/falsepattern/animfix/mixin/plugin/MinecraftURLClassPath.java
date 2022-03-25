package com.falsepattern.animfix.mixin.plugin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import net.minecraft.launchwrapper.LaunchClassLoader;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Backport from spongemixins 1.3 for compat with the curseforge 1.2.0 version
 */
public final class MinecraftURLClassPath {
    /**
     *  Utility to manipulate the minecraft URL ClassPath
     */

    private static final Field modClassLoaderField;
    private static final Field loaderinstanceField;
    private static final Field mainClassLoaderField;
    private static final Field ucpField;
    private static final ModClassLoader modClassLoader;
    private static final LaunchClassLoader mainClassLoader;
    private static final URLClassPath ucp;

    static {
        try {
            modClassLoaderField = Loader.class.getDeclaredField("modClassLoader");
            modClassLoaderField.setAccessible(true);

            loaderinstanceField = Loader.class.getDeclaredField("instance");
            loaderinstanceField.setAccessible(true);

            mainClassLoaderField = ModClassLoader.class.getDeclaredField("mainClassLoader");
            mainClassLoaderField.setAccessible(true);

            ucpField = LaunchClassLoader.class.getSuperclass().getDeclaredField("ucp");
            ucpField.setAccessible(true);

            Object loader = loaderinstanceField.get(null);
            modClassLoader = (ModClassLoader)modClassLoaderField.get(loader);
            mainClassLoader = (LaunchClassLoader)mainClassLoaderField.get(modClassLoader);
            ucp = (URLClassPath)ucpField.get(mainClassLoader);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Adds a Jar to the Minecraft URL ClassPath
     *  - Needed when using mixins on classes outside of Minecraft or other coremods
     */
    public static void addJar(File pathToJar) throws Exception {
        ucp.addURL(pathToJar.toURI().toURL());
    }

    private MinecraftURLClassPath() {
    }


}
