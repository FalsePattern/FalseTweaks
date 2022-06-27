package com.falsepattern.triangulator.proxy;

import cpw.mods.fml.common.event.FMLConstructionEvent;

public class ServerConfig extends CommonProxy {
    @Override
    public void construct(FMLConstructionEvent e) {
        throw new Error("DO NOT USE TRIANGULATOR IN A SERVER, IT IS A CLIENTSIDE MOD.");
    }
}
