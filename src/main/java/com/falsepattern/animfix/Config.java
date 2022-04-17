package com.falsepattern.animfix;

import lombok.val;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    public static int maximumBatchedTextureSize = 32;

    private static File configFile;

    public static void synchronizeConfiguration(File configFile) {
        Config.configFile = configFile;
        reload();
    }

    public static void reload() {
        if (configFile == null) return;
        val configuration = new Configuration(configFile);
        configuration.load();

        maximumBatchedTextureSize = configuration.getInt("maxBatchedTextureSize", "AnimFix", 128, 16, 512, "The largest width and height animated textures can have to get put into the buffer. Making this higher will batch higher resolution textures too, but will consume more RAM.");

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }
}