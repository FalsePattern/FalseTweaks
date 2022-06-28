package com.falsepattern.animfix.proxy;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        throw new Error("DO NOT USE ANIMFIX IN A SERVER, IT IS A CLIENTSIDE MOD.");
    }
}
