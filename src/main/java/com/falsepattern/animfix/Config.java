package com.falsepattern.animfix;

import lombok.val;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    public static int maximumBatchedTextureSize = 32;

    private static File configFile;

    public static void syncronizeConfiguration(File configFile) {
        Config.configFile = configFile;
        val configuration = new Configuration(configFile);
        configuration.load();

        maximumBatchedTextureSize = configuration.getInt("maxBatchedTextureSize", "AnimFix", 32, 16, 512, "The largest width and height animated textures can have to get put into the buffer. Making this too large WILL slow things down. Recommended value is around 32-128.");

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void reload() {
        syncronizeConfiguration(configFile);
    }
}