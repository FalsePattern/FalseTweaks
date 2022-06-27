package com.falsepattern.triangulator.proxy;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.triangulator.*;
import com.falsepattern.triangulator.calibration.Calibration;
import com.falsepattern.triangulator.calibration.CalibrationConfig;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.ClientCommandHandler;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        try {
            ConfigurationManager.registerConfig(CalibrationConfig.class);
        } catch (ConfigException ex) {
            Triangulator.triLog.error("Failed to register config", ex);
        }
        LeakFix.registerBus();
        Calibration.registerBus();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ItemRenderListManager.INSTANCE);
        LeakFix.gc();
        ClientCommandHandler.instance.registerCommand(new Calibration.CalibrationCommand());
    }
}
