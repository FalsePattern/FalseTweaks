package com.falsepattern.triangulator.proxy;

import com.falsepattern.triangulator.ItemRenderListManager;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

public class ClientProxy extends CommonProxy {
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ItemRenderListManager.INSTANCE);
    }
}
