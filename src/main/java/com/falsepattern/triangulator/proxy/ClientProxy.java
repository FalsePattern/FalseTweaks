package com.falsepattern.triangulator.proxy;

import com.falsepattern.triangulator.ItemRenderListManager;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(LeakFix.INSTANCE);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ItemRenderListManager.INSTANCE);
        LeakFix.gc();
    }
}
