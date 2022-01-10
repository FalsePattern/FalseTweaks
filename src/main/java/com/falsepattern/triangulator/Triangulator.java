package com.falsepattern.triangulator;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.ModMetadata;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.actors.threadpool.Arrays;

public class Triangulator extends DummyModContainer {
    public static Logger triLog = LogManager.getLogger(ModInfo.MODNAME);

    public Triangulator() {
        super(new ModMetadata());
        val meta = getMetadata();
        meta.modId = ModInfo.MODID;
        meta.name = ModInfo.MODNAME;
        meta.version = ModInfo.VERSION;
        meta.url = ModInfo.URL;
        meta.credits = ModInfo.CREDITS;
        meta.authorList = Arrays.asList(ModInfo.AUTHORS);
        meta.description = ModInfo.DESCRIPTION;
        meta.useDependencyInformation = true;
        triLog.info("Skidaddle skidoodle, your quad is now a noodle!");
    }
}
