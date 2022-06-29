package com.falsepattern.animfix.proxy;

import com.falsepattern.animfix.AnimFix;
import com.falsepattern.animfix.config.AnimConfig;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        try {
            ConfigurationManager.registerConfig(AnimConfig.class);
        } catch (ConfigException ex) {
            AnimFix.LOG.error("Failed to register config", ex);
        }
    }
}