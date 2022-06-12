package com.falsepattern.triangulator;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.triangulator.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.8.0,);")
public class Triangulator {
    public static Logger triLog = LogManager.getLogger(Tags.MODNAME);

    @SidedProxy(clientSide = Tags.GROUPNAME + ".proxy.ClientProxy", serverSide = Tags.GROUPNAME + ".proxy.CommonProxy")
    private static CommonProxy proxy;

    public Triangulator() {
        triLog.info("Skidaddle skidoodle, your quad is now a noodle!");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
}
