package com.falsepattern.animfix;

import lombok.val;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    public static void syncronizeConfiguration(File configFile) {
        val configuration = new Configuration(configFile);
        configuration.load();

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }
}