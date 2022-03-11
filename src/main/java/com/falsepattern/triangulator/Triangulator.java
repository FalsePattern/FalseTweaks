package com.falsepattern.triangulator;

import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModInfo.MODID,
     version = ModInfo.VERSION,
     name = ModInfo.MODNAME,
     dependencies = "required-after:spongemixins@[1.2.0,);")
public class Triangulator {
    public static Logger triLog = LogManager.getLogger(ModInfo.MODNAME);

    public Triangulator() {
        triLog.info("Skidaddle skidoodle, your quad is now a noodle!");
    }
}
