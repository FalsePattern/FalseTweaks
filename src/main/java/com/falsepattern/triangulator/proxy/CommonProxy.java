package com.falsepattern.triangulator.proxy;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.triangulator.TriConfig;
import com.falsepattern.triangulator.Triangulator;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        try {
            ConfigurationManager.registerConfig(TriConfig.class);
        } catch (ConfigException ex) {
            Triangulator.triLog.error("Failed to register config", ex);
        }
    }

    public void postInit(FMLPostInitializationEvent e) {

    }
}
