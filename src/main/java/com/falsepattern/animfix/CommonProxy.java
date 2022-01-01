package com.falsepattern.animfix;

import cpw.mods.fml.common.event.*;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.syncronizeConfiguration(event.getSuggestedConfigurationFile());
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public void serverStarted(FMLServerStartedEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {

    }

    public void serverStopped(FMLServerStoppedEvent event) {

    }
}
